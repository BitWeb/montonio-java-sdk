package ee.bitweb.montonio.sdk.order.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentStatusTest {

    private final ObjectMapper mapper = new JsonMapper();

    @Test
    void enumContainsExpectedValues() {
        assertEquals(10, PaymentStatus.values().length);
    }

    @Test
    void wireValuesAreUpperCase() {
        assertEquals("PENDING", PaymentStatus.PENDING.getValue());
        assertEquals("PAID", PaymentStatus.PAID.getValue());
        assertEquals("VOIDED", PaymentStatus.VOIDED.getValue());
        assertEquals("PARTIALLY_REFUNDED", PaymentStatus.PARTIALLY_REFUNDED.getValue());
        assertEquals("REFUNDED", PaymentStatus.REFUNDED.getValue());
        assertEquals("CANCELED", PaymentStatus.CANCELED.getValue());
        assertEquals("ABANDONED", PaymentStatus.ABANDONED.getValue());
        assertEquals("DECLINED", PaymentStatus.DECLINED.getValue());
        assertEquals("SETTLED", PaymentStatus.SETTLED.getValue());
        assertEquals("AUTHORIZED", PaymentStatus.AUTHORIZED.getValue());
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus.class)
    void serializesToWireValue(PaymentStatus status) throws Exception {
        String json = mapper.writeValueAsString(status);
        assertEquals("\"" + status.getValue() + "\"", json);
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus.class)
    void deserializesFromWireValue(PaymentStatus status) throws Exception {
        String json = "\"" + status.getValue() + "\"";
        PaymentStatus result = mapper.readValue(json, PaymentStatus.class);
        assertEquals(status, result);
    }
}
