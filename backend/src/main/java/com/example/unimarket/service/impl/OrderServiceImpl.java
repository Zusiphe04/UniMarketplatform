package com.example.unimarket.service.impl;

import com.example.unimarket.domain.CartItem;
import com.example.unimarket.domain.MarketplaceOrder;
import com.example.unimarket.domain.OrderItem;
import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.UserAccount;
import com.example.unimarket.domain.enums.AccountStatus;
import com.example.unimarket.domain.enums.NotificationType;
import com.example.unimarket.domain.enums.ProductStatus;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.MarketplaceOrderFactory;
import com.example.unimarket.factory.OrderItemFactory;
import com.example.unimarket.repository.ICartItemRepository;
import com.example.unimarket.repository.IMarketplaceOrderRepository;
import com.example.unimarket.repository.IOrderItemRepository;
import com.example.unimarket.repository.IProductRepository;
import com.example.unimarket.repository.IUserAccountRepository;
import com.example.unimarket.request.CheckoutRequest;
import com.example.unimarket.response.OrderResponse;
import com.example.unimarket.response.SellerOrderResponse;
import com.example.unimarket.service.IActorRolePolicy;
import com.example.unimarket.service.IInventoryReservationService;
import com.example.unimarket.service.INotificationService;
import com.example.unimarket.service.IOrderService;
import com.example.unimarket.util.CheckoutRequestFingerprint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements IOrderService {
    private static final int LAZY_EXPIRY_BATCH_SIZE = 25;
    private final ICartItemRepository cartRepository;
    private final IProductRepository productRepository;
    private final IMarketplaceOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final IUserAccountRepository accountRepository;
    private final INotificationService notificationService;
    private final IInventoryReservationService reservationService;
    private final IActorRolePolicy actorRolePolicy;

    public OrderServiceImpl(ICartItemRepository cartRepository, IProductRepository productRepository,
                            IMarketplaceOrderRepository orderRepository, IOrderItemRepository orderItemRepository,
                            IUserAccountRepository accountRepository, INotificationService notificationService,
                            IInventoryReservationService reservationService, IActorRolePolicy actorRolePolicy) {
        this.cartRepository = cartRepository; this.productRepository = productRepository; this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository; this.accountRepository = accountRepository;
        this.notificationService = notificationService; this.reservationService = reservationService;
        this.actorRolePolicy = actorRolePolicy;
    }

    @Override
    @Transactional
    public OrderResponse checkout(UUID buyerId, String idempotencyKey, CheckoutRequest request) {
        actorRolePolicy.requireBuyerOnly(buyerId);
        String key = normalizedIdempotencyKey(idempotencyKey);
        String fingerprint = CheckoutRequestFingerprint.from(request);
        if (fingerprint == null) throw new ValidationException("The checkout details are invalid.");
        UserAccount account = accountRepository.read(buyerId);
        if (account == null || account.getStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException("Only an active account can check out.");
        }
        // Expire a bounded set before locking this cart/products, so stale holds do not block availability.
        reservationService.expireDueReservations(LAZY_EXPIRY_BATCH_SIZE);
        OrderResponse replay = replayIfMatching(buyerId, key, fingerprint);
        if (replay != null) return replay;

        List<CartItem> cart = new ArrayList<>(cartRepository.readByBuyerIdForUpdate(buyerId));
        replay = replayIfMatching(buyerId, key, fingerprint);
        if (replay != null) return replay;
        if (cart.isEmpty()) throw new ValidationException("Your cart is empty.");
        cart.sort(Comparator.comparing(CartItem::getProductId));

        Map<CartItem, Product> lockedProducts = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        int itemCount = 0;
        for (CartItem cartItem : cart) {
            Product product = productRepository.readByIdForUpdate(cartItem.getProductId());
            if (product == null || product.getStatus() != ProductStatus.PUBLISHED) {
                throw new ValidationException("A product in your cart is no longer available.");
            }
            if (product.getSellerId().equals(buyerId)) throw new ValidationException("You cannot purchase your own product.");
            if (cartItem.getQuantity() > product.getAvailableQuantity()) {
                throw new ValidationException("Insufficient stock for " + product.getTitle() + ".");
            }
            lockedProducts.put(cartItem, product);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            itemCount += cartItem.getQuantity();
        }
        MarketplaceOrder order = MarketplaceOrderFactory.create(buyerId, total, itemCount,
                request.fulfillmentMethod(), request.deliveryAddress(), key, fingerprint);
        if (order == null) throw new ValidationException("The checkout details are invalid.");
        order = orderRepository.create(order);

        List<OrderItem> items = new ArrayList<>();
        for (Map.Entry<CartItem, Product> entry : lockedProducts.entrySet()) {
            OrderItem item = OrderItemFactory.create(order.getId(), entry.getValue(), entry.getKey().getQuantity());
            if (item == null) throw new ValidationException("An order item could not be created.");
            items.add(orderItemRepository.create(item));
        }
        // Existing product write locks and this sorted list prevent competing checkouts from over-reserving.
        reservationService.reserve(order, items);
        cartRepository.deleteByIds(cart.stream().map(CartItem::getId).toList());
        sendSellerCheckoutNotifications(order, items);
        return OrderResponse.from(order, items);
    }

    @Override
    @Transactional
    public OrderResponse cancelBuyerOrder(UUID buyerId, UUID orderId) {
        actorRolePolicy.requireBuyerOnly(buyerId);
        MarketplaceOrder order = orderRepository.readByIdForUpdate(orderId);
        if (order == null || !order.getBuyerId().equals(buyerId)) throw ResourceNotFoundException.of("Order");
        if (!order.cancel()) throw new ValidationException("Only an order awaiting payment can be cancelled.");
        reservationService.releaseForCancellation(order);
        orderRepository.update(order);
        return OrderResponse.from(order, orderItemRepository.readByOrderId(orderId));
    }

    @Override @Transactional(readOnly = true)
    public List<OrderResponse> listBuyerOrders(UUID buyerId) {
        actorRolePolicy.requireBuyerOnly(buyerId);
        List<MarketplaceOrder> orders = orderRepository.readByBuyerId(buyerId);
        Map<UUID, List<OrderItem>> items = orderItemRepository.readByOrderIds(orders.stream().map(MarketplaceOrder::getId).toList())
                .stream().collect(Collectors.groupingBy(OrderItem::getOrderId));
        return orders.stream().map(order -> OrderResponse.from(order, items.getOrDefault(order.getId(), List.of()))).toList();
    }
    @Override @Transactional(readOnly = true)
    public OrderResponse getBuyerOrder(UUID buyerId, UUID orderId) {
        actorRolePolicy.requireBuyerOnly(buyerId);
        MarketplaceOrder order = orderRepository.read(orderId);
        if (order == null || !order.getBuyerId().equals(buyerId)) throw ResourceNotFoundException.of("Order");
        return OrderResponse.from(order, orderItemRepository.readByOrderId(orderId));
    }
    @Override @Transactional(readOnly = true)
    public List<SellerOrderResponse> listSellerOrders(UUID sellerId) {
        actorRolePolicy.requireSellerOnly(sellerId);
        Map<UUID, List<OrderItem>> groups = orderItemRepository.readBySellerId(sellerId).stream()
                .collect(Collectors.groupingBy(OrderItem::getOrderId, LinkedHashMap::new, Collectors.toList()));
        return groups.entrySet().stream().map(entry -> SellerOrderResponse.from(orderRepository.read(entry.getKey()), entry.getValue())).toList();
    }
    private String normalizedIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null) throw new ValidationException("Idempotency-Key is required.");
        String key = idempotencyKey.trim();
        if (key.isEmpty() || key.length() > 120) throw new ValidationException("Idempotency-Key must contain between 1 and 120 characters.");
        return key;
    }
    private OrderResponse replayIfMatching(UUID buyerId, String key, String fingerprint) {
        MarketplaceOrder existing = orderRepository.readByBuyerIdAndIdempotencyKey(buyerId, key);
        if (existing == null) return null;
        if (!fingerprint.equals(existing.getCheckoutRequestFingerprint())) {
            throw new ValidationException("Idempotency-Key has already been used with different checkout details.");
        }
        return OrderResponse.from(existing, orderItemRepository.readByOrderId(existing.getId()));
    }
    private void sendSellerCheckoutNotifications(MarketplaceOrder order, List<OrderItem> items) {
        items.stream().map(OrderItem::getSellerId).distinct().forEach(sellerId -> notificationService.send(sellerId,
                NotificationType.SELLER_ORDER_CREATED, "New order awaiting payment",
                "Order " + order.getReference() + " contains one or more of your products and is awaiting payment.",
                "ORDER", order.getId(), "order:" + order.getId() + ":created:seller:" + sellerId));
    }
}
