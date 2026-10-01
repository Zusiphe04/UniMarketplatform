-- Baseline extension for installations migrating from Hibernate-managed schema.
-- Apply with Flyway after baselining the existing UniMarket schema.

ALTER TABLE bulletin_post ADD COLUMN event_capacity INT NULL;
ALTER TABLE bulletin_post ADD COLUMN event_cancelled_at TIMESTAMP NULL;
ALTER TABLE bulletin_post ADD COLUMN event_cancellation_reason VARCHAR(500) NULL;

CREATE TABLE event_registration (
    id BINARY(16) NOT NULL,
    bulletin_post_id BINARY(16) NOT NULL,
    attendee_id BINARY(16) NOT NULL,
    registration_status VARCHAR(20) NOT NULL,
    queued_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_event_registration_attendee UNIQUE (bulletin_post_id, attendee_id),
    INDEX idx_event_registration_queue (bulletin_post_id, registration_status, queued_at),
    INDEX idx_event_registration_attendee (attendee_id, registration_status, created_at)
);

CREATE TABLE order_item_fulfillment (
    id BINARY(16) NOT NULL,
    order_item_id BINARY(16) NOT NULL,
    order_id BINARY(16) NOT NULL,
    seller_id BINARY(16) NOT NULL,
    fulfillment_status VARCHAR(30) NOT NULL,
    pickup_instructions VARCHAR(500) NULL,
    tracking_reference VARCHAR(160) NULL,
    accepted_at TIMESTAMP NULL,
    ready_at TIMESTAMP NULL,
    dispatched_at TIMESTAMP NULL,
    received_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_order_item_fulfillment_item UNIQUE (order_item_id),
    INDEX idx_fulfillment_order (order_id, fulfillment_status),
    INDEX idx_fulfillment_seller (seller_id, fulfillment_status)
);
