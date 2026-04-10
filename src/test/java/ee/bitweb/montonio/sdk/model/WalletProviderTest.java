package ee.bitweb.montonio.sdk.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WalletProviderTest {

    private final ObjectMapper mapper = new JsonMapper();

    @Test
    void enumContainsExpectedValues() {
        assertEquals(2, WalletProvider.values().length);
    }

    @Test
    void wireValues() {
        assertEquals("applePay", WalletProvider.APPLE_PAY.getValue());
        assertEquals("googlePay", WalletProvider.GOOGLE_PAY.getValue());
    }

    @ParameterizedTest
    @EnumSource(WalletProvider.class)
    void serializesToWireValue(WalletProvider provider) throws Exception {
        String json = mapper.writeValueAsString(provider);
        assertEquals("\"" + provider.getValue() + "\"", json);
    }

    @ParameterizedTest
    @EnumSource(WalletProvider.class)
    void deserializesFromWireValue(WalletProvider provider) throws Exception {
        String json = "\"" + provider.getValue() + "\"";
        WalletProvider result = mapper.readValue(json, WalletProvider.class);
        assertEquals(provider, result);
    }
}
