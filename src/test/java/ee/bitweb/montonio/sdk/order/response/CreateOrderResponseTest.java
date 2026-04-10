package ee.bitweb.montonio.sdk.order.response;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateOrderResponseTest {

    private final ObjectMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @Test
    void constructorSetsFields() {
        CreateOrderResponse response = new CreateOrderResponse(
                "550e8400-e29b-41d4-a716-446655440000",
                "https://sandbox-stargate.montonio.com/pay"
        );

        assertEquals("550e8400-e29b-41d4-a716-446655440000", response.getUuid());
        assertEquals("https://sandbox-stargate.montonio.com/pay", response.getPaymentUrl());
    }

    @Test
    void deserializesFromJson() throws Exception {
        String json = """
                {
                    "uuid": "550e8400-e29b-41d4-a716-446655440000",
                    "paymentUrl": "https://sandbox-stargate.montonio.com/pay"
                }
                """;

        CreateOrderResponse response = mapper.readValue(json, CreateOrderResponse.class);

        assertEquals("550e8400-e29b-41d4-a716-446655440000", response.getUuid());
        assertEquals("https://sandbox-stargate.montonio.com/pay", response.getPaymentUrl());
    }

    @Test
    void deserializesWithUnknownFieldsIgnored() throws Exception {
        String json = """
                {
                    "uuid": "550e8400-e29b-41d4-a716-446655440000",
                    "paymentUrl": "https://example.com/pay",
                    "extraField": "ignored"
                }
                """;

        CreateOrderResponse response = mapper.readValue(json, CreateOrderResponse.class);

        assertEquals("550e8400-e29b-41d4-a716-446655440000", response.getUuid());
    }

    @Test
    void serializationRoundTrip() throws Exception {
        CreateOrderResponse original = new CreateOrderResponse(
                "550e8400-e29b-41d4-a716-446655440000",
                "https://example.com/pay"
        );

        String json = mapper.writeValueAsString(original);
        CreateOrderResponse deserialized = mapper.readValue(json, CreateOrderResponse.class);

        assertEquals(original.getUuid(), deserialized.getUuid());
        assertEquals(original.getPaymentUrl(), deserialized.getPaymentUrl());
    }
}
