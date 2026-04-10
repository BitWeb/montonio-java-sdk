package ee.bitweb.montonio.sdk.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CurrencyTest {

    private final ObjectMapper mapper = new JsonMapper();

    @Test
    void enumContainsExpectedValues() {
        assertEquals(2, Currency.values().length);
        assertNotNull(Currency.valueOf("EUR"));
        assertNotNull(Currency.valueOf("PLN"));
    }

    @Test
    void eurHasCorrectWireValue() {
        assertEquals("EUR", Currency.EUR.getValue());
    }

    @Test
    void plnHasCorrectWireValue() {
        assertEquals("PLN", Currency.PLN.getValue());
    }

    @ParameterizedTest
    @EnumSource(Currency.class)
    void serializesToWireValue(Currency currency) throws Exception {
        String json = mapper.writeValueAsString(currency);
        assertEquals("\"" + currency.getValue() + "\"", json);
    }

    @ParameterizedTest
    @EnumSource(Currency.class)
    void deserializesFromWireValue(Currency currency) throws Exception {
        String json = "\"" + currency.getValue() + "\"";
        Currency result = mapper.readValue(json, Currency.class);
        assertEquals(currency, result);
    }
}
