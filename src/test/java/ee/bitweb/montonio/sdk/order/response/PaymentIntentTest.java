package ee.bitweb.montonio.sdk.order.response;

import ee.bitweb.montonio.sdk.model.Currency;
import ee.bitweb.montonio.sdk.model.PaymentMethodType;
import ee.bitweb.montonio.sdk.order.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaymentIntentTest {

    private final ObjectMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @Test
    void constructorSetsAllFields() {
        Map<String, String> metadata = Map.of(
                "preferredCountry", "EE",
                "preferredProvider", "LHVBEE22"
        );

        PaymentIntent intent = new PaymentIntent(
                "intent-uuid",
                PaymentMethodType.PAYMENT_INITIATION,
                "100.00",
                Currency.EUR,
                PaymentStatus.PAID,
                "0.50",
                Currency.EUR,
                "2026-04-10T12:00:00Z",
                metadata
        );

        assertEquals("intent-uuid", intent.getUuid());
        assertEquals(PaymentMethodType.PAYMENT_INITIATION, intent.getPaymentMethodType());
        assertEquals("100.00", intent.getAmount());
        assertEquals(Currency.EUR, intent.getCurrency());
        assertEquals(PaymentStatus.PAID, intent.getStatus());
        assertEquals("0.50", intent.getServiceFee());
        assertEquals(Currency.EUR, intent.getServiceFeeCurrency());
        assertEquals("2026-04-10T12:00:00Z", intent.getCreatedAt());
        assertNotNull(intent.getPaymentMethodMetadata());
        assertEquals("EE", intent.getPaymentMethodMetadata().get("preferredCountry"));
    }

    @Test
    void constructorWithNullMetadata() {
        PaymentIntent intent = new PaymentIntent(
                "intent-uuid",
                PaymentMethodType.CARD_PAYMENTS,
                "50.00",
                Currency.EUR,
                PaymentStatus.PENDING,
                "0.00",
                Currency.EUR,
                "2026-04-10T12:00:00Z",
                null
        );

        assertNull(intent.getPaymentMethodMetadata());
    }

    @Test
    void deserializesFromJson() throws Exception {
        String json = """
                {
                    "uuid": "intent-uuid",
                    "paymentMethodType": "cardPayments",
                    "amount": "100.00",
                    "currency": "EUR",
                    "status": "PAID",
                    "serviceFee": "0.50",
                    "serviceFeeCurrency": "EUR",
                    "createdAt": "2026-04-10T12:00:00Z",
                    "paymentMethodMetadata": {
                        "preferredCountry": "EE",
                        "preferredProvider": "Visa",
                        "paymentDescription": "Payment for order"
                    }
                }
                """;

        PaymentIntent intent = mapper.readValue(json, PaymentIntent.class);

        assertEquals("intent-uuid", intent.getUuid());
        assertEquals(PaymentMethodType.CARD_PAYMENTS, intent.getPaymentMethodType());
        assertEquals("100.00", intent.getAmount());
        assertEquals(Currency.EUR, intent.getCurrency());
        assertEquals(PaymentStatus.PAID, intent.getStatus());
        assertEquals("0.50", intent.getServiceFee());
        assertEquals("EE", intent.getPaymentMethodMetadata().get("preferredCountry"));
    }

    @Test
    void serializationRoundTrip() throws Exception {
        PaymentIntent original = new PaymentIntent(
                "intent-uuid",
                PaymentMethodType.PAYMENT_INITIATION,
                "100.00",
                Currency.EUR,
                PaymentStatus.AUTHORIZED,
                "0.25",
                Currency.EUR,
                "2026-04-10T12:00:00Z",
                Map.of("preferredCountry", "EE")
        );

        String json = mapper.writeValueAsString(original);
        PaymentIntent deserialized = mapper.readValue(json, PaymentIntent.class);

        assertEquals(original.getUuid(), deserialized.getUuid());
        assertEquals(original.getPaymentMethodType(), deserialized.getPaymentMethodType());
        assertEquals(original.getAmount(), deserialized.getAmount());
        assertEquals(original.getCurrency(), deserialized.getCurrency());
        assertEquals(original.getStatus(), deserialized.getStatus());
        assertEquals(original.getServiceFee(), deserialized.getServiceFee());
        assertEquals(original.getServiceFeeCurrency(), deserialized.getServiceFeeCurrency());
    }
}
