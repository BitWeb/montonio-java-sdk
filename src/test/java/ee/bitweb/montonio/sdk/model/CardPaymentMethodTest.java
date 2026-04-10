package ee.bitweb.montonio.sdk.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardPaymentMethodTest {

    private final ObjectMapper mapper = new JsonMapper();

    @Test
    void enumContainsExpectedValues() {
        assertEquals(2, CardPaymentMethod.values().length);
    }

    @Test
    void wireValues() {
        assertEquals("card", CardPaymentMethod.CARD.getValue());
        assertEquals("wallet", CardPaymentMethod.WALLET.getValue());
    }

    @ParameterizedTest
    @EnumSource(CardPaymentMethod.class)
    void serializesToWireValue(CardPaymentMethod method) throws Exception {
        String json = mapper.writeValueAsString(method);
        assertEquals("\"" + method.getValue() + "\"", json);
    }

    @ParameterizedTest
    @EnumSource(CardPaymentMethod.class)
    void deserializesFromWireValue(CardPaymentMethod method) throws Exception {
        String json = "\"" + method.getValue() + "\"";
        CardPaymentMethod result = mapper.readValue(json, CardPaymentMethod.class);
        assertEquals(method, result);
    }
}
