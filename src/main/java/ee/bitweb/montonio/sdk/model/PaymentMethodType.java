package ee.bitweb.montonio.sdk.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethodType {

    PAYMENT_INITIATION("paymentInitiation"),
    CARD_PAYMENTS("cardPayments"),
    BLIK("blik"),
    BNPL("bnpl"),
    HIRE_PURCHASE("hirePurchase");

    private final String value;

    PaymentMethodType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
