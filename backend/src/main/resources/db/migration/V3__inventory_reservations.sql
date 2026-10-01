-- Inventory holds reserve sellable availability only. product.quantity remains physical stock
-- and is decremented exactly once when an active reservation is consumed at payment.
ALTER TABLE product
    ADD COLUMN reserved_quantity INT NOT NULL DEFAULT 0;

ALTER TABLE marketplace_order
    ADD COLUMN reservation_active BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN reservation_expires_at TIMESTAMP(6) NULL;

CREATE TABLE inventory_reservation (
    id BINARY(16) NOT NULL,
    order_id BINARY(16) NOT NULL,
    product_id BINARY(16) NOT NULL,
    quantity INT NOT NULL,
    reservation_status VARCHAR(20) NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_inventory_reservation_order_product UNIQUE (order_id, product_id),
    INDEX idx_inventory_reservation_expiry (reservation_status, expires_at),
    INDEX idx_inventory_reservation_order (order_id, reservation_status),
    INDEX idx_inventory_reservation_product (product_id, reservation_status)
);
