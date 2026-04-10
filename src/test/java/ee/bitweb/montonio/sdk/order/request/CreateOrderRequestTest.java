package ee.bitweb.montonio.sdk.order.request;

import ee.bitweb.montonio.sdk.exception.MontonioValidationException;
import ee.bitweb.montonio.sdk.model.Currency;
import ee.bitweb.montonio.sdk.model.Locale;
import ee.bitweb.montonio.sdk.model.PaymentMethodType;
import ee.bitweb.montonio.sdk.order.model.Address;
import ee.bitweb.montonio.sdk.order.model.LineItem;
import ee.bitweb.montonio.sdk.order.model.Payment;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateOrderRequestTest {

    private final ObjectMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    private Payment validPayment() {
        return Payment.builder()
                .method(PaymentMethodType.PAYMENT_INITIATION)
                .currency(Currency.EUR)
                .amount(new BigDecimal("100.00"))
                .build();
    }

    @Test
    void buildWithRequiredFieldsOnly() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .merchantReference("order-123")
                .returnUrl("https://example.com/return")
                .notificationUrl("https://example.com/notify")
                .grandTotal(new BigDecimal("100.00"))
                .currency(Currency.EUR)
                .payment(validPayment())
                .build();

        assertEquals("order-123", request.getMerchantReference());
        assertEquals("https://example.com/return", request.getReturnUrl());
        assertEquals("https://example.com/notify", request.getNotificationUrl());
        assertEquals(new BigDecimal("100.00"), request.getGrandTotal());
        assertEquals(Currency.EUR, request.getCurrency());
        assertNotNull(request.getPayment());
        assertNull(request.getLocale());
        assertNull(request.getBillingAddress());
        assertNull(request.getShippingAddress());
        assertNull(request.getLineItems());
    }

    @Test
    void buildWithAllFields() {
        Address address = Address.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        LineItem item = LineItem.builder()
                .name("Hoverboard")
                .quantity(BigDecimal.ONE)
                .finalPrice(new BigDecimal("100.00"))
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .merchantReference("order-123")
                .returnUrl("https://example.com/return")
                .notificationUrl("https://example.com/notify")
                .grandTotal(new BigDecimal("100.00"))
                .currency(Currency.EUR)
                .payment(validPayment())
                .locale(Locale.ET)
                .billingAddress(address)
                .shippingAddress(address)
                .lineItems(List.of(item))
                .build();

        assertEquals(Locale.ET, request.getLocale());
        assertNotNull(request.getBillingAddress());
        assertNotNull(request.getShippingAddress());
        assertEquals(1, request.getLineItems().size());
    }

    @Test
    void buildWithNullMerchantReferenceThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> CreateOrderRequest.builder()
                        .returnUrl("https://example.com/return")
                        .notificationUrl("https://example.com/notify")
                        .grandTotal(BigDecimal.TEN)
                        .currency(Currency.EUR)
                        .payment(validPayment())
                        .build()
        );

        assertEquals("merchantReference", exception.getField());
    }

    @Test
    void buildWithBlankMerchantReferenceThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> CreateOrderRequest.builder()
                        .merchantReference("   ")
                        .returnUrl("https://example.com/return")
                        .notificationUrl("https://example.com/notify")
                        .grandTotal(BigDecimal.TEN)
                        .currency(Currency.EUR)
                        .payment(validPayment())
                        .build()
        );

        assertEquals("merchantReference", exception.getField());
    }

    @Test
    void buildWithNullReturnUrlThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> CreateOrderRequest.builder()
                        .merchantReference("order-123")
                        .notificationUrl("https://example.com/notify")
                        .grandTotal(BigDecimal.TEN)
                        .currency(Currency.EUR)
                        .payment(validPayment())
                        .build()
        );

        assertEquals("returnUrl", exception.getField());
    }

    @Test
    void buildWithNullNotificationUrlThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> CreateOrderRequest.builder()
                        .merchantReference("order-123")
                        .returnUrl("https://example.com/return")
                        .grandTotal(BigDecimal.TEN)
                        .currency(Currency.EUR)
                        .payment(validPayment())
                        .build()
        );

        assertEquals("notificationUrl", exception.getField());
    }

    @Test
    void buildWithBlankReturnUrlThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> CreateOrderRequest.builder()
                        .merchantReference("order-123")
                        .returnUrl("   ")
                        .notificationUrl("https://example.com/notify")
                        .grandTotal(BigDecimal.TEN)
                        .currency(Currency.EUR)
                        .payment(validPayment())
                        .build()
        );

        assertEquals("returnUrl", exception.getField());
    }

    @Test
    void buildWithBlankNotificationUrlThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> CreateOrderRequest.builder()
                        .merchantReference("order-123")
                        .returnUrl("https://example.com/return")
                        .notificationUrl("   ")
                        .grandTotal(BigDecimal.TEN)
                        .currency(Currency.EUR)
                        .payment(validPayment())
                        .build()
        );

        assertEquals("notificationUrl", exception.getField());
    }

    @Test
    void buildWithNullGrandTotalThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> CreateOrderRequest.builder()
                        .merchantReference("order-123")
                        .returnUrl("https://example.com/return")
                        .notificationUrl("https://example.com/notify")
                        .currency(Currency.EUR)
                        .payment(validPayment())
                        .build()
        );

        assertEquals("grandTotal", exception.getField());
    }

    @Test
    void buildWithNullCurrencyThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> CreateOrderRequest.builder()
                        .merchantReference("order-123")
                        .returnUrl("https://example.com/return")
                        .notificationUrl("https://example.com/notify")
                        .grandTotal(BigDecimal.TEN)
                        .payment(validPayment())
                        .build()
        );

        assertEquals("currency", exception.getField());
    }

    @Test
    void buildWithNullPaymentThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> CreateOrderRequest.builder()
                        .merchantReference("order-123")
                        .returnUrl("https://example.com/return")
                        .notificationUrl("https://example.com/notify")
                        .grandTotal(BigDecimal.TEN)
                        .currency(Currency.EUR)
                        .build()
        );

        assertEquals("payment", exception.getField());
    }

    @Test
    void serializationRoundTrip() throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .merchantReference("order-123")
                .returnUrl("https://example.com/return")
                .notificationUrl("https://example.com/notify")
                .grandTotal(new BigDecimal("100.00"))
                .currency(Currency.EUR)
                .payment(validPayment())
                .locale(Locale.ET)
                .build();

        String json = mapper.writeValueAsString(request);
        CreateOrderRequest deserialized = mapper.readValue(json, CreateOrderRequest.class);

        assertEquals(request.getMerchantReference(), deserialized.getMerchantReference());
        assertEquals(request.getReturnUrl(), deserialized.getReturnUrl());
        assertEquals(request.getNotificationUrl(), deserialized.getNotificationUrl());
        assertEquals(0, request.getGrandTotal().compareTo(deserialized.getGrandTotal()));
        assertEquals(request.getCurrency(), deserialized.getCurrency());
        assertEquals(request.getLocale(), deserialized.getLocale());
        assertEquals(request.getPayment().getMethod(), deserialized.getPayment().getMethod());
    }
}
