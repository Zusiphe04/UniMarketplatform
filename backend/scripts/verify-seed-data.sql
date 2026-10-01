-- UniMarket read-only MySQL verification for the numbered files in http/seeding/
-- Run after all 11 workflow files complete successfully.
-- This script performs no INSERT, UPDATE, DELETE, DROP, or schema mutation.

USE unimarket;

-- 1. Confirm all 19 mapped persistence tables exist.
SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
    'user_account',
    'user_profile',
    'user_role_assignment',
    'auth_session',
    'user_persona_assignment',
    'vendor_profile',
    'product',
    'product_image',
    'cart_item',
    'marketplace_order',
    'order_item',
    'order_payment_simulation',
    'product_review',
    'bulletin_post',
    'user_notification',
    'loyalty_point_entry',
    'badge_award',
    'simulated_escrow',
    'suspicious_activity_report'
  )
ORDER BY table_name;

-- 2. Row count for every mapped table. cart_item should contain the deliberately
-- retained textbook row; auth_session retains revoked-session audit history.
SELECT 'user_account' AS table_name, COUNT(*) AS row_count FROM user_account
UNION ALL SELECT 'user_profile', COUNT(*) FROM user_profile
UNION ALL SELECT 'user_role_assignment', COUNT(*) FROM user_role_assignment
UNION ALL SELECT 'auth_session', COUNT(*) FROM auth_session
UNION ALL SELECT 'user_persona_assignment', COUNT(*) FROM user_persona_assignment
UNION ALL SELECT 'vendor_profile', COUNT(*) FROM vendor_profile
UNION ALL SELECT 'product', COUNT(*) FROM product
UNION ALL SELECT 'product_image', COUNT(*) FROM product_image
UNION ALL SELECT 'cart_item', COUNT(*) FROM cart_item
UNION ALL SELECT 'marketplace_order', COUNT(*) FROM marketplace_order
UNION ALL SELECT 'order_item', COUNT(*) FROM order_item
UNION ALL SELECT 'order_payment_simulation', COUNT(*) FROM order_payment_simulation
UNION ALL SELECT 'product_review', COUNT(*) FROM product_review
UNION ALL SELECT 'bulletin_post', COUNT(*) FROM bulletin_post
UNION ALL SELECT 'user_notification', COUNT(*) FROM user_notification
UNION ALL SELECT 'loyalty_point_entry', COUNT(*) FROM loyalty_point_entry
UNION ALL SELECT 'badge_award', COUNT(*) FROM badge_award
UNION ALL SELECT 'simulated_escrow', COUNT(*) FROM simulated_escrow
UNION ALL SELECT 'suspicious_activity_report', COUNT(*) FROM suspicious_activity_report
ORDER BY table_name;

-- 3. Seeded identities and profiles. Password hashes and token hashes are
-- intentionally excluded from all verification output.
SELECT
    ua.id AS account_id,
    ua.email,
    ua.status AS account_status,
    ua.email_verified_at,
    ua.last_login_at,
    up.id AS profile_id,
    up.first_name,
    up.last_name,
    up.display_name,
    up.phone_number,
    up.bio,
    up.preferred_language
FROM user_account ua
LEFT JOIN user_profile up ON up.user_id = ua.id
WHERE ua.email IN (
    'admin.seed@unimarket.local',
    'seller.seed@unimarket.local',
    '240000001@mycput.ac.za',
    'faculty.seed@cput.ac.za',
    'resident.seed@unimarket.local',
    'buyer.seed@unimarket.local'
)
ORDER BY ua.email;

-- 4. Authorization roles. Expected active roles include BUYER for every
-- registered member, SELLER for the approved vendor, and ADMIN/MODERATOR for
-- the bootstrap administrator.
SELECT
    ua.email,
    ura.role,
    ura.granted_at,
    grantor.email AS granted_by,
    ura.revoked_at
FROM user_role_assignment ura
JOIN user_account ua ON ua.id = ura.user_id
LEFT JOIN user_account grantor ON grantor.id = ura.granted_by_user_id
WHERE ua.email IN (
    'admin.seed@unimarket.local',
    'seller.seed@unimarket.local',
    '240000001@mycput.ac.za',
    'faculty.seed@cput.ac.za',
    'resident.seed@unimarket.local',
    'buyer.seed@unimarket.local'
)
ORDER BY ua.email, ura.role;

-- 5. Community personas are labels, not authorization roles. Student and
-- faculty should be VERIFIED by the selected institution's email policy;
-- vendor approval also creates a verified VENDOR persona.
SELECT
    ua.email,
    upa.persona,
    upa.verification_status,
    upa.organization_name,
    upa.qualification,
    upa.year_of_study,
    upa.verified_email_domain,
    upa.assigned_at,
    upa.verified_at,
    verifier.email AS verified_by
FROM user_persona_assignment upa
JOIN user_account ua ON ua.id = upa.user_id
LEFT JOIN user_account verifier ON verifier.id = upa.verified_by_user_id
WHERE ua.email IN (
    'seller.seed@unimarket.local',
    '240000001@mycput.ac.za',
    'faculty.seed@cput.ac.za',
    'resident.seed@unimarket.local',
    'seller.platform@unimarket.local',
    '240000002@mycput.ac.za',
    'faculty.platform@cput.ac.za',
    'resident.platform@unimarket.local'
)
ORDER BY ua.email, upa.persona;

-- 6. Vendor approval and administrator audit trail.
SELECT
    seller.email AS seller_email,
    vp.vendor_type,
    vp.business_name,
    vp.verification_status,
    vp.submitted_at,
    vp.reviewed_at,
    reviewer.email AS reviewed_by,
    vp.review_note
FROM vendor_profile vp
JOIN user_account seller ON seller.id = vp.user_id
LEFT JOIN user_account reviewer ON reviewer.id = vp.reviewed_by_user_id
WHERE seller.email = 'seller.seed@unimarket.local';

-- 7. Product catalogue and image metadata. Expected final inventory:
-- textbook PUBLISHED with quantity 4; laptop SOLD_OUT with quantity 0.
SELECT
    seller.email AS seller_email,
    p.id AS product_id,
    p.title,
    p.category AS persisted_legacy_category,
    p.product_condition,
    p.brand,
    p.product_model AS model,
    p.storage_specification AS storage,
    p.memory_specification AS memory,
    p.processor,
    p.screen_size,
    p.color,
    p.item_size AS size,
    p.price,
    p.currency,
    p.quantity AS remaining_quantity,
    p.status AS product_status,
    p.published_at,
    pi.id AS image_id,
    pi.image_url,
    pi.alt_text,
    pi.display_order,
    pi.primary_image
FROM product p
JOIN user_account seller ON seller.id = p.seller_id
LEFT JOIN product_image pi ON pi.product_id = p.id
WHERE seller.email = 'seller.seed@unimarket.local'
ORDER BY p.title, pi.display_order;

-- 7a. Optional frontend catalogue image coverage. After completing
-- 03_frontend_catalogue.http on a fresh database, the first query should report
-- 19/19/19/19 and the second query should return no rows.
SELECT
    COUNT(DISTINCT p.id) AS published_product_count,
    COUNT(DISTINCT CASE WHEN pi.id IS NOT NULL THEN p.id END) AS products_with_images,
    COUNT(DISTINCT CASE WHEN pi.primary_image = TRUE THEN p.id END) AS products_with_primary_images,
    COUNT(pi.id) AS image_row_count
FROM product p
JOIN user_account seller ON seller.id = p.seller_id
LEFT JOIN product_image pi ON pi.product_id = p.id
WHERE seller.email = 'seller.platform@unimarket.local'
  AND p.status = 'PUBLISHED';

SELECT
    p.id AS product_id,
    p.title,
    COUNT(pi.id) AS image_count,
    SUM(CASE WHEN pi.primary_image = TRUE THEN 1 ELSE 0 END) AS primary_image_count
FROM product p
JOIN user_account seller ON seller.id = p.seller_id
LEFT JOIN product_image pi ON pi.product_id = p.id
WHERE seller.email = 'seller.platform@unimarket.local'
  AND p.status = 'PUBLISHED'
GROUP BY p.id, p.title
HAVING COUNT(pi.id) <> 1
    OR SUM(CASE WHEN pi.primary_image = TRUE THEN 1 ELSE 0 END) <> 1;

-- 8. Retained cart row. Checkout deleted earlier cart rows; the seed flow adds
-- one textbook back after all orders so cart_item can be inspected directly.
SELECT
    buyer.email AS buyer_email,
    ci.id AS cart_item_id,
    p.title,
    ci.quantity,
    ci.added_at
FROM cart_item ci
JOIN user_account buyer ON buyer.id = ci.buyer_id
JOIN product p ON p.id = ci.product_id
WHERE buyer.email = 'buyer.seed@unimarket.local';

-- 9. Complete order, item, payment, and escrow proof. Expected order outcomes:
-- textbook PAID, first laptop RELEASED, second laptop REFUNDED. The textbook
-- order has a retained DECLINED attempt followed by SUCCEEDED; each laptop has
-- a SUCCEEDED payment whose immutable resulting_order_status remains HELD.
SELECT
    buyer.email AS buyer_email,
    mo.id AS order_id,
    mo.reference AS order_reference,
    mo.status AS current_order_status,
    mo.total_amount,
    mo.currency,
    mo.fulfillment_method,
    mo.delivery_address,
    mo.placed_at,
    mo.paid_at,
    mo.held_at,
    mo.disputed_at,
    mo.released_at,
    mo.refunded_at,
    mo.payment_failure_code,
    oi.id AS order_item_id,
    oi.product_title,
    oi.quantity,
    oi.unit_price,
    oi.subtotal,
    seller.email AS seller_email,
    pay.id AS payment_id,
    pay.payment_option,
    pay.scenario AS payment_scenario,
    pay.status AS payment_status,
    pay.resulting_order_status,
    pay.failure_code,
    pay.idempotency_key,
    pay.reference AS payment_reference,
    pay.processed_at,
    esc.id AS escrow_id,
    esc.status AS escrow_status,
    esc.dispute_reason,
    esc.resolution AS escrow_resolution,
    resolver.email AS escrow_resolved_by,
    esc.resolution_note,
    esc.resolved_at
FROM marketplace_order mo
JOIN user_account buyer ON buyer.id = mo.buyer_id
JOIN order_item oi ON oi.order_id = mo.id
JOIN user_account seller ON seller.id = oi.seller_id
LEFT JOIN order_payment_simulation pay ON pay.order_id = mo.id
LEFT JOIN simulated_escrow esc ON esc.order_id = mo.id
LEFT JOIN user_account resolver ON resolver.id = esc.resolved_by_user_id
WHERE buyer.email = 'buyer.seed@unimarket.local'
ORDER BY mo.placed_at, pay.processed_at;

-- 10. Compact order-state totals should be PAID=1, RELEASED=1, REFUNDED=1.
SELECT mo.status, COUNT(*) AS order_count
FROM marketplace_order mo
JOIN user_account buyer ON buyer.id = mo.buyer_id
WHERE buyer.email = 'buyer.seed@unimarket.local'
GROUP BY mo.status
ORDER BY mo.status;

-- 11. Review and its qualifying paid/released purchase. verifiedPurchase is a
-- derived API fact, so there is deliberately no redundant database column.
SELECT
    pr.id AS review_id,
    reviewer.email AS reviewer_email,
    p.title AS product_title,
    pr.rating,
    pr.comment,
    pr.seller_response,
    pr.seller_responded_at,
    qualifying_order.reference AS qualifying_order_reference,
    qualifying_order.status AS qualifying_order_status
FROM product_review pr
JOIN user_account reviewer ON reviewer.id = pr.reviewer_id
JOIN product p ON p.id = pr.product_id
JOIN order_item qualifying_item ON qualifying_item.product_id = pr.product_id
JOIN marketplace_order qualifying_order
  ON qualifying_order.id = qualifying_item.order_id
 AND qualifying_order.buyer_id = pr.reviewer_id
 AND qualifying_order.status IN ('PAID', 'RELEASED')
WHERE reviewer.email = 'buyer.seed@unimarket.local'
ORDER BY pr.created_at;

-- 12. Published event and author.
SELECT
    bp.id AS bulletin_id,
    author.email AS author_email,
    bp.post_type,
    bp.status,
    bp.title,
    bp.location,
    bp.event_starts_at,
    bp.expires_at,
    bp.published_at
FROM bulletin_post bp
JOIN user_account author ON author.id = bp.author_id
WHERE bp.title = 'UniMarket Campus Exchange Day 2035';

-- 13. Immutable loyalty ledger. Expected totals from this flow are buyer=23,
-- seller=24, and resident=1. REFUNDED escrow awards no completion points.
SELECT
    ua.email,
    lpe.track,
    lpe.event_type,
    lpe.points,
    lpe.source_type,
    lpe.source_id,
    lpe.event_key,
    lpe.occurred_at
FROM loyalty_point_entry lpe
JOIN user_account ua ON ua.id = lpe.user_id
WHERE ua.email IN (
    'buyer.seed@unimarket.local',
    'seller.seed@unimarket.local',
    'resident.seed@unimarket.local'
)
ORDER BY ua.email, lpe.occurred_at;

SELECT
    ua.email,
    lpe.track,
    SUM(lpe.points) AS total_points
FROM loyalty_point_entry lpe
JOIN user_account ua ON ua.id = lpe.user_id
WHERE ua.email IN (
    'buyer.seed@unimarket.local',
    'seller.seed@unimarket.local',
    'resident.seed@unimarket.local'
)
GROUP BY ua.email, lpe.track
ORDER BY ua.email, lpe.track;

-- 14. Idempotent badges. Expected buyer badges include FIRST_PURCHASE and
-- VERIFIED_REVIEWER; seller badges include FIRST_LISTING and FIRST_SALE.
SELECT
    ua.email,
    ba.track,
    ba.badge_code,
    ba.awarded_at
FROM badge_award ba
JOIN user_account ua ON ua.id = ba.user_id
WHERE ua.email IN (
    'buyer.seed@unimarket.local',
    'seller.seed@unimarket.local',
    'resident.seed@unimarket.local'
)
ORDER BY ua.email, ba.track, ba.badge_code;

-- 15. Suspicious-activity evidence and moderation audit. The expected row is
-- RESOLVED/ESCALATED/ESCALATED and its active dedupe key is cleared.
SELECT
    reporter.email AS reporter_email,
    sar.id AS report_id,
    sar.target_type,
    sar.target_id,
    owner.email AS target_owner_email,
    sar.reason,
    sar.details,
    sar.target_snapshot,
    sar.status,
    sar.active_dedupe_key,
    sar.resolution,
    sar.moderation_action,
    moderator.email AS resolved_by,
    sar.resolution_note,
    sar.reporter_message,
    sar.resolved_at
FROM suspicious_activity_report sar
JOIN user_account reporter ON reporter.id = sar.reporter_id
LEFT JOIN user_account owner ON owner.id = sar.target_owner_id
LEFT JOIN user_account moderator ON moderator.id = sar.resolved_by_user_id
WHERE reporter.email = 'buyer.seed@unimarket.local';

-- 16. Notifications generated by cross-feature events. The HTTP flow marks all
-- buyer notifications read, while seller/admin notifications may remain unread.
SELECT
    recipient.email AS recipient_email,
    un.id AS notification_id,
    un.notification_type,
    un.title,
    un.resource_type,
    un.resource_id,
    un.read_at,
    un.event_key,
    un.created_at
FROM user_notification un
JOIN user_account recipient ON recipient.id = un.recipient_id
WHERE recipient.email IN (
    'buyer.seed@unimarket.local',
    'seller.seed@unimarket.local',
    'admin.seed@unimarket.local'
)
ORDER BY recipient.email, un.created_at;

-- 17. Session audit without exposing refresh-token hashes. Expected buyer rows
-- include LOGOUT, ROTATED, PASSWORD_CHANGE, and LOGOUT_ALL reasons; selected
-- session revocation intentionally uses LOGOUT. No buyer refresh session should
-- remain active after the final logout-all request.
SELECT
    ua.email,
    aus.id AS session_id,
    aus.token_family_id,
    aus.issued_at,
    aus.expires_at,
    aus.last_used_at,
    aus.revoked_at,
    aus.revocation_reason,
    aus.replaced_by_session_id,
    aus.user_agent
FROM auth_session aus
JOIN user_account ua ON ua.id = aus.user_id
WHERE ua.email = 'buyer.seed@unimarket.local'
ORDER BY aus.issued_at;

-- 18. One-row academic acceptance checklist. On a fresh database after the
-- complete HTTP flow, each actual_* value should equal its expected_* value.
SELECT
    (SELECT COUNT(*)
       FROM user_account
      WHERE email IN (
        'admin.seed@unimarket.local',
        'seller.seed@unimarket.local',
        '240000001@mycput.ac.za',
        'faculty.seed@cput.ac.za',
        'resident.seed@unimarket.local',
        'buyer.seed@unimarket.local'
      )) AS actual_seed_accounts,
    6 AS expected_seed_accounts,

    (SELECT COUNT(*)
       FROM product p
       JOIN user_account seller ON seller.id = p.seller_id
      WHERE seller.email = 'seller.seed@unimarket.local') AS actual_products,
    2 AS expected_products,

    (SELECT COUNT(*)
       FROM product_image pi
       JOIN product p ON p.id = pi.product_id
       JOIN user_account seller ON seller.id = p.seller_id
      WHERE seller.email = 'seller.seed@unimarket.local') AS actual_product_images,
    2 AS expected_product_images,

    (SELECT COUNT(DISTINCT pi.image_url)
       FROM product_image pi
       JOIN product p ON p.id = pi.product_id
       JOIN user_account seller ON seller.id = p.seller_id
      WHERE seller.email = 'seller.seed@unimarket.local'
        AND pi.image_url IN (
          'https://placehold.co/1200x800/1E3A8A/FFFFFF/png?text=Data+Structures+Textbook',
          'https://placehold.co/1200x800/334155/FFFFFF/png?text=Engineering+Student+Laptop'
        )
        AND pi.display_order = 0
        AND pi.primary_image = TRUE) AS actual_expected_image_urls,
    2 AS expected_image_urls,

    (SELECT COUNT(*)
       FROM marketplace_order mo
       JOIN user_account buyer ON buyer.id = mo.buyer_id
      WHERE buyer.email = 'buyer.seed@unimarket.local') AS actual_orders,
    3 AS expected_orders,

    (SELECT COUNT(*)
       FROM order_payment_simulation ops
       JOIN user_account buyer ON buyer.id = ops.buyer_id
      WHERE buyer.email = 'buyer.seed@unimarket.local') AS actual_payment_attempts,
    4 AS expected_payment_attempts,

    (SELECT COUNT(*)
       FROM simulated_escrow se
       JOIN user_account buyer ON buyer.id = se.buyer_id
      WHERE buyer.email = 'buyer.seed@unimarket.local') AS actual_escrows,
    2 AS expected_escrows,

    (SELECT COUNT(*)
       FROM product_review pr
       JOIN user_account reviewer ON reviewer.id = pr.reviewer_id
      WHERE reviewer.email = 'buyer.seed@unimarket.local') AS actual_reviews,
    1 AS expected_reviews,

    (SELECT COUNT(*)
       FROM suspicious_activity_report sar
       JOIN user_account reporter ON reporter.id = sar.reporter_id
      WHERE reporter.email = 'buyer.seed@unimarket.local') AS actual_reports,
    1 AS expected_reports,

    (SELECT COUNT(*)
       FROM cart_item ci
       JOIN user_account buyer ON buyer.id = ci.buyer_id
      WHERE buyer.email = 'buyer.seed@unimarket.local') AS actual_retained_cart_items,
    1 AS expected_retained_cart_items,

    (SELECT COALESCE(SUM(lpe.points), 0)
       FROM loyalty_point_entry lpe
       JOIN user_account ua ON ua.id = lpe.user_id
      WHERE ua.email = 'buyer.seed@unimarket.local'
        AND lpe.track = 'BUYER') AS actual_buyer_points,
    23 AS expected_buyer_points,

    (SELECT COALESCE(SUM(lpe.points), 0)
       FROM loyalty_point_entry lpe
       JOIN user_account ua ON ua.id = lpe.user_id
      WHERE ua.email = 'seller.seed@unimarket.local'
        AND lpe.track = 'SELLER') AS actual_seller_points,
    24 AS expected_seller_points,

    (SELECT COUNT(*)
       FROM auth_session aus
       JOIN user_account ua ON ua.id = aus.user_id
      WHERE ua.email = 'buyer.seed@unimarket.local'
        AND aus.revoked_at IS NULL
        AND aus.expires_at > UTC_TIMESTAMP()) AS actual_active_buyer_sessions,
    0 AS expected_active_buyer_sessions;
