package ee.bitweb.montonio.sdk.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CardPaymentMethod {

    CARD("card"),
    WALLET("wallet");

    private final String value;

    CardPaymentMethod(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
