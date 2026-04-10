package ee.bitweb.montonio.sdk.order.response;

import ee.bitweb.montonio.sdk.model.Currency;
import ee.bitweb.montonio.sdk.model.Locale;
import ee.bitweb.montonio.sdk.model.PaymentMethodType;
import ee.bitweb.montonio.sdk.order.model.Address;
import ee.bitweb.montonio.sdk.order.model.LineItem;
import ee.bitweb.montonio.sdk.order.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderResponseTest {

    private final ObjectMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @Test
    void deserializesFullOrderResponse() throws Exception {
        String json = """
                {
                    "uuid": "order-uuid",
                    "paymentStatus": "PAID",
                    "locale": "en",
                    "merchantReference": "Order1234567",
                    "merchantReferenceDisplay": "Order 1234567",
                    "merchantReturnUrl": "http://localhost:3000/return",
                    "merchantNotificationUrl": "http://example.com/notify",
                    "grandTotal": "100.00",
                    "currency": "EUR",
                    "paymentMethodType": "cardPayments",
                    "storeUuid": "store-uuid",
                    "paymentIntents": [
                        {
                            "uuid": "intent-uuid",
                            "paymentMethodType": "cardPayments",
                            "paymentMethodMetadata": {
                                "preferredCountry": "US",
                                "preferredProvider": "Visa",
                                "paymentDescription": "Payment for Order 1234567"
                            },
                            "amount": "100.00",
                            "currency": "EUR",
                            "status": "PAID",
                            "serviceFee": "0.00",
                            "serviceFeeCurrency": "EUR",
                            "createdAt": "2026-04-10T12:00:00Z"
                        }
                    ],
                    "lineItems": [
                        {
                            "name": "Test Item",
                            "quantity": 1,
                            "finalPrice": 100
                        }
                    ],
                    "billingAddress": {
                        "firstName": "John",
                        "lastName": "Doe",
                        "email": "john@example.com",
                        "country": "US"
                    },
                    "shippingAddress": {
                        "firstName": "John",
                        "lastName": "Doe",
                        "country": "US"
                    },
                    "expiresAt": "2026-04-10T12:10:00Z",
                    "createdAt": "2026-04-10T12:00:00Z",
                    "storeName": "Test Store",
                    "businessName": "Test Business",
                    "paymentUrl": "https://sandbox-stargate.montonio.com/pay",
                    "isRefundableType": true
                }
                """;

        OrderResponse response = mapper.readValue(json, OrderResponse.class);

        assertEquals("order-uuid", response.getUuid());
        assertEquals(PaymentStatus.PAID, response.getPaymentStatus());
        assertEquals(Locale.EN, response.getLocale());
        assertEquals("Order1234567", response.getMerchantReference());
        assertEquals("Order 1234567", response.getMerchantReferenceDisplay());
        assertEquals("http://localhost:3000/return", response.getMerchantReturnUrl());
        assertEquals("http://example.com/notify", response.getMerchantNotificationUrl());
        assertEquals("100.00", response.getGrandTotal());
        assertEquals(Currency.EUR, response.getCurrency());
        assertEquals(PaymentMethodType.CARD_PAYMENTS, response.getPaymentMethodType());
        assertEquals("store-uuid", response.getStoreUuid());

        assertNotNull(response.getPaymentIntents());
        assertEquals(1, response.getPaymentIntents().size());
        assertEquals("intent-uuid", response.getPaymentIntents().get(0).getUuid());
        assertEquals(PaymentStatus.PAID, response.getPaymentIntents().get(0).getStatus());

        assertNotNull(response.getLineItems());
        assertEquals(1, response.getLineItems().size());
        assertEquals("Test Item", response.getLineItems().get(0).getName());

        assertEquals("John", response.getBillingAddress().getFirstName());
        assertEquals("John", response.getShippingAddress().getFirstName());

        assertEquals("2026-04-10T12:10:00Z", response.getExpiresAt());
        assertEquals("2026-04-10T12:00:00Z", response.getCreatedAt());
        assertEquals("Test Store", response.getStoreName());
        assertEquals("Test Business", response.getBusinessName());
        assertEquals("https://sandbox-stargate.montonio.com/pay", response.getPaymentUrl());
        assertTrue(response.getIsRefundableType());
    }

    @Test
    void deserializesWithUnknownFieldsIgnored() throws Exception {
        String json = """
                {
                    "uuid": "order-uuid",
                    "paymentStatus": "PENDING",
                    "locale": "et",
                    "merchantReference": "ref",
                    "merchantReferenceDisplay": "ref",
                    "merchantReturnUrl": "http://example.com",
                    "merchantNotificationUrl": "http://example.com",
                    "grandTotal": "10.00",
                    "currency": "EUR",
                    "paymentMethodType": "paymentInitiation",
                    "storeUuid": "store",
                    "paymentIntents": [],
                    "lineItems": [],
                    "billingAddress": {},
                    "shippingAddress": {},
                    "expiresAt": "2026-04-10T12:10:00Z",
                    "createdAt": "2026-04-10T12:00:00Z",
                    "storeName": "Store",
                    "businessName": "Business",
                    "paymentUrl": "",
                    "refunds": [],
                    "availableForRefund": 0,
                    "unknownField": "ignored"
                }
                """;

        OrderResponse response = mapper.readValue(json, OrderResponse.class);

        assertEquals("order-uuid", response.getUuid());
        assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
    }

    @Test
    void serializationRoundTrip() throws Exception {
        OrderResponse original = new OrderResponse(
                "order-uuid",
                PaymentStatus.PAID,
                Locale.EN,
                "ref-123",
                "Ref 123",
                "http://example.com/return",
                "http://example.com/notify",
                "100.00",
                Currency.EUR,
                PaymentMethodType.CARD_PAYMENTS,
                "store-uuid",
                List.of(new PaymentIntent(
                        "intent-uuid",
                        PaymentMethodType.CARD_PAYMENTS,
                        "100.00",
                        Currency.EUR,
                        PaymentStatus.PAID,
                        "0.00",
                        Currency.EUR,
                        "2026-04-10T12:00:00Z",
                        Map.of("preferredCountry", "EE")
                )),
                List.of(LineItem.builder()
                        .name("Item")
                        .quantity(BigDecimal.ONE)
                        .finalPrice(new BigDecimal("100.00"))
                        .build()),
                Address.builder().firstName("John").build(),
                Address.builder().firstName("John").build(),
                "2026-04-10T12:10:00Z",
                "2026-04-10T12:00:00Z",
                "Test Store",
                "Test Business",
                "https://example.com/pay",
                true
        );

        String json = mapper.writeValueAsString(original);
        OrderResponse deserialized = mapper.readValue(json, OrderResponse.class);

        assertEquals(original.getUuid(), deserialized.getUuid());
        assertEquals(original.getPaymentStatus(), deserialized.getPaymentStatus());
        assertEquals(original.getLocale(), deserialized.getLocale());
        assertEquals(original.getMerchantReference(), deserialized.getMerchantReference());
        assertEquals(original.getGrandTotal(), deserialized.getGrandTotal());
        assertEquals(original.getCurrency(), deserialized.getCurrency());
        assertEquals(original.getPaymentMethodType(), deserialized.getPaymentMethodType());
        assertEquals(1, deserialized.getPaymentIntents().size());
        assertEquals(1, deserialized.getLineItems().size());
    }
}
