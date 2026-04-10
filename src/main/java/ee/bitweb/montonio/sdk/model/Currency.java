package ee.bitweb.montonio.sdk.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Currency {

    EUR("EUR"),
    PLN("PLN");

    private final String value;

    Currency(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
