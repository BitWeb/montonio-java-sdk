package ee.bitweb.montonio.sdk.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentMethodTypeTest {

    private final ObjectMapper mapper = new JsonMapper();

    @Test
    void enumContainsExpectedValues() {
        assertEquals(5, PaymentMethodType.values().length);
    }

    @Test
    void wireValuesAreCamelCase() {
        assertEquals("paymentInitiation", PaymentMethodType.PAYMENT_INITIATION.getValue());
        assertEquals("cardPayments", PaymentMethodType.CARD_PAYMENTS.getValue());
        assertEquals("blik", PaymentMethodType.BLIK.getValue());
        assertEquals("bnpl", PaymentMethodType.BNPL.getValue());
        assertEquals("hirePurchase", PaymentMethodType.HIRE_PURCHASE.getValue());
    }

    @ParameterizedTest
    @EnumSource(PaymentMethodType.class)
    void serializesToWireValue(PaymentMethodType type) throws Exception {
        String json = mapper.writeValueAsString(type);
        assertEquals("\"" + type.getValue() + "\"", json);
    }

    @ParameterizedTest
    @EnumSource(PaymentMethodType.class)
    void deserializesFromWireValue(PaymentMethodType type) throws Exception {
        String json = "\"" + type.getValue() + "\"";
        PaymentMethodType result = mapper.readValue(json, PaymentMethodType.class);
        assertEquals(type, result);
    }
}
