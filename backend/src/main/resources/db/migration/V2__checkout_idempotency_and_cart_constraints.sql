-- Checkout reliability extension for a Flyway-baselined UniMarket schema.
-- Legacy marketplace orders retain NULL idempotency fields; all new checkout
-- orders supply both fields through the application.
ALTER TABLE marketplace_order
    ADD COLUMN idempotency_key VARCHAR(120) NULL,
    ADD COLUMN checkout_request_fingerprint CHAR(64) NULL,
    ADD CONSTRAINT uk_marketplace_order_buyer_idempotency UNIQUE (buyer_id, idempotency_key);

-- cart_item already has the (buyer_id, added_at) index used by the checkout
-- FOR UPDATE snapshot query; no duplicate index is introduced here.
