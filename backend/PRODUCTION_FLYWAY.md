# Production schema procedure

Flyway remains disabled in the default application configuration because the existing V1/V2 scripts are incremental extensions, not a complete create-from-empty baseline for every legacy UniMarket installation.

For production, use a separately controlled release procedure:

1. Take a tested, restorable backup and verify the target schema is the expected current Hibernate-managed UniMarket schema.
2. Set the production application profile to `spring.jpa.hibernate.ddl-auto=validate`; do not start application instances that use `update` against the production database during this change.
3. With the operations-managed Flyway CLI/image, baseline the already-current schema at version `2` (after reviewing V1/V2 applicability for that installation). Record the baseline metadata and checksum in the release evidence.
4. Run only `V3__inventory_reservations.sql` from this release and verify the three new product/order columns, the `inventory_reservation` table, and its indexes.
5. Deploy this backend version with `INVENTORY_RESERVATION_EXPIRY_ENABLED=false`. After one instance passes health checks, set it to `true` and restart/roll the instances that should run the sweep. In a multi-instance deployment, enable only one scheduler instance unless a distributed scheduler lock is added. The sweep is bounded by `unimarket.inventory-reservation.expiry-batch-size` (default 25).
6. Roll back application code only before new checkouts run. After V3 reservations exist, recover by restoring the verified backup or deploy a compatible forward fix; do not drop the reservation table while awaiting-payment orders exist.

This keeps schema ownership explicit and avoids enabling Flyway globally without a complete safe baseline.
