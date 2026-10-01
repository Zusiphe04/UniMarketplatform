package com.example.unimarket.domain.enums;

public enum PaymentOption {
    PAYFAST(
            "PayFast",
            PaymentOptionCategory.PAYMENT_GATEWAY,
            "Simulate checkout through the PayFast payment gateway."),
    SNAPSCAN(
            "SnapScan",
            PaymentOptionCategory.QR_PAYMENT,
            "Simulate a SnapScan QR payment."),
    PAYSHAP(
            "PayShap",
            PaymentOptionCategory.INSTANT_PAYMENT,
            "Simulate a South African PayShap instant payment."),
    VISA(
            "Visa",
            PaymentOptionCategory.CARD_NETWORK,
            "Simulate a Visa payment without collecting card credentials."),
    MASTERCARD(
            "Mastercard",
            PaymentOptionCategory.CARD_NETWORK,
            "Simulate a Mastercard payment without collecting card credentials.");

    private final String displayName;
    private final PaymentOptionCategory category;
    private final String description;

    PaymentOption(String displayName, PaymentOptionCategory category, String description) {
        this.displayName = displayName;
        this.category = category;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public PaymentOptionCategory getCategory() { return category; }
    public String getDescription() { return description; }
}
