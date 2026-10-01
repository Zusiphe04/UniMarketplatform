package com.example.unimarket.service.impl;

import com.example.unimarket.domain.CartItem;
import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.enums.ProductStatus;
import com.example.unimarket.exception.ResourceNotFoundException;
import com.example.unimarket.exception.ValidationException;
import com.example.unimarket.factory.CartItemFactory;
import com.example.unimarket.repository.ICartItemRepository;
import com.example.unimarket.repository.IProductImageRepository;
import com.example.unimarket.repository.IProductRepository;
import com.example.unimarket.request.AddCartItemRequest;
import com.example.unimarket.request.UpdateCartItemRequest;
import com.example.unimarket.response.CartItemResponse;
import com.example.unimarket.response.CartResponse;
import com.example.unimarket.service.ICartService;
import com.example.unimarket.service.IInventoryReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CartServiceImpl implements ICartService {
    private static final int LAZY_EXPIRY_BATCH_SIZE = 25;
    private final ICartItemRepository cartRepository;
    private final IProductRepository productRepository;
    private final IProductImageRepository imageRepository;
    private final IInventoryReservationService reservationService;
    public CartServiceImpl(ICartItemRepository cartRepository, IProductRepository productRepository,
                           IProductImageRepository imageRepository, IInventoryReservationService reservationService) {
        this.cartRepository = cartRepository; this.productRepository = productRepository; this.imageRepository = imageRepository;
        this.reservationService = reservationService;
    }
    @Override @Transactional(readOnly = true)
    public CartResponse getCart(UUID buyerId) { return response(buyerId); }
    @Override @Transactional
    public CartResponse addItem(UUID buyerId, AddCartItemRequest request) {
        reservationService.expireDueReservations(LAZY_EXPIRY_BATCH_SIZE);
        Product product = purchasableProduct(buyerId, request.productId());
        CartItem existing = cartRepository.readByBuyerIdAndProductId(buyerId, request.productId());
        int quantity = request.quantity() + (existing == null ? 0 : existing.getQuantity());
        validateStock(product, quantity);
        if (existing == null) {
            CartItem item = CartItemFactory.create(buyerId, request.productId(), quantity);
            if (item == null) throw new ValidationException("The cart item is invalid.");
            cartRepository.create(item);
        } else cartRepository.update(CartItemFactory.changeQuantity(existing, quantity));
        return response(buyerId);
    }
    @Override @Transactional
    public CartResponse updateItem(UUID buyerId, UUID productId, UpdateCartItemRequest request) {
        reservationService.expireDueReservations(LAZY_EXPIRY_BATCH_SIZE);
        Product product = purchasableProduct(buyerId, productId);
        validateStock(product, request.quantity());
        CartItem item = cartRepository.readByBuyerIdAndProductId(buyerId, productId);
        if (item == null) throw ResourceNotFoundException.of("Cart item");
        CartItem updated = CartItemFactory.changeQuantity(item, request.quantity());
        if (updated == null) throw new ValidationException("The cart quantity is invalid.");
        cartRepository.update(updated);
        return response(buyerId);
    }
    @Override @Transactional public CartResponse removeItem(UUID buyerId, UUID productId) {
        CartItem item = cartRepository.readByBuyerIdAndProductId(buyerId, productId);
        if (item == null) throw ResourceNotFoundException.of("Cart item");
        cartRepository.delete(item.getId()); return response(buyerId);
    }
    @Override @Transactional public void clear(UUID buyerId) { cartRepository.deleteByBuyerId(buyerId); }
    private Product purchasableProduct(UUID buyerId, UUID productId) {
        Product product = productRepository.readByIdForUpdate(productId);
        if (product == null || product.getStatus() != ProductStatus.PUBLISHED) throw ResourceNotFoundException.of("Published product");
        if (product.getSellerId().equals(buyerId)) throw new ValidationException("You cannot add your own product to your cart.");
        return product;
    }
    private void validateStock(Product product, int quantity) {
        if (quantity < 1 || quantity > 99 || quantity > product.getAvailableQuantity()) {
            throw new ValidationException("The requested quantity is not available.");
        }
    }
    private CartResponse response(UUID buyerId) {
        List<CartItemResponse> items = new ArrayList<>();
        for (CartItem item : cartRepository.readByBuyerId(buyerId)) {
            Product product = productRepository.read(item.getProductId());
            if (product != null) items.add(CartItemResponse.from(item, product, imageRepository.readByProductId(product.getId())));
        }
        return CartResponse.of(items);
    }
}
