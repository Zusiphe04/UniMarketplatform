package com.example.unimarket;

import com.example.unimarket.domain.Product;
import com.example.unimarket.domain.enums.ProductStatus;
import com.example.unimarket.factory.ProductFactory;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ProductReservationStockTest {
    @Test
    void reservationOnlyChangesAvailableStockUntilPaymentConsumesIt() {
        Product product = new Product.Builder().setId(UUID.randomUUID()).setQuantity(5)
                .setReservedQuantity(0).setStatus(ProductStatus.PUBLISHED).build();

        assertSame(product, ProductFactory.reserveStock(product, 3));
        assertEquals(5, product.getQuantity());
        assertEquals(3, product.getReservedQuantity());
        assertEquals(2, product.getAvailableQuantity());
        assertNull(ProductFactory.reserveStock(product, 3));

        assertSame(product, ProductFactory.consumeReservedStock(product, 3));
        assertEquals(2, product.getQuantity());
        assertEquals(0, product.getReservedQuantity());
        assertEquals(2, product.getAvailableQuantity());
    }

    @Test
    void cancellationOrExpiryReleaseOnlyTheHeldQuantity() {
        Product product = new Product.Builder().setId(UUID.randomUUID()).setQuantity(4)
                .setReservedQuantity(0).setStatus(ProductStatus.PUBLISHED).build();

        assertSame(product, ProductFactory.reserveStock(product, 4));
        assertSame(product, ProductFactory.releaseReservedStock(product, 4));
        assertEquals(4, product.getQuantity());
        assertEquals(0, product.getReservedQuantity());
        assertEquals(4, product.getAvailableQuantity());
        assertFalse(product.getStatus() == ProductStatus.SOLD_OUT);
    }
}
