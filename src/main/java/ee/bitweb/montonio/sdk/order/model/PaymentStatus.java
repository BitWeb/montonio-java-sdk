package ee.bitweb.montonio.sdk.order.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentStatus {

    PENDING("PENDING"),
    PAID("PAID"),
    VOIDED("VOIDED"),
    PARTIALLY_REFUNDED("PARTIALLY_REFUNDED"),
    REFUNDED("REFUNDED"),
    CANCELED("CANCELED"),
    ABANDONED("ABANDONED"),
    DECLINED("DECLINED"),
    SETTLED("SETTLED"),
    AUTHORIZED("AUTHORIZED");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
