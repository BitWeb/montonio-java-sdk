package ee.bitweb.montonio.sdk.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum WalletProvider {

    APPLE_PAY("applePay"),
    GOOGLE_PAY("googlePay");

    private final String value;

    WalletProvider(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
