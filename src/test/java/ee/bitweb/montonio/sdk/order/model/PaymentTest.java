package ee.bitweb.montonio.sdk.order.model;

import ee.bitweb.montonio.sdk.exception.MontonioValidationException;
import ee.bitweb.montonio.sdk.model.Currency;
import ee.bitweb.montonio.sdk.model.Locale;
import ee.bitweb.montonio.sdk.model.PaymentMethodType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentTest {

    private final ObjectMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @Test
    void buildWithRequiredFieldsOnly() {
        Payment payment = Payment.builder()
                .method(PaymentMethodType.PAYMENT_INITIATION)
                .currency(Currency.EUR)
                .amount(new BigDecimal("100.00"))
                .build();

        assertEquals(PaymentMethodType.PAYMENT_INITIATION, payment.getMethod());
        assertEquals(Currency.EUR, payment.getCurrency());
        assertEquals(new BigDecimal("100.00"), payment.getAmount());
        assertNull(payment.getMethodOptions());
        assertNull(payment.getMethodDisplay());
    }

    @Test
    void buildWithAllFields() {
        PaymentMethodOptions options = PaymentMethodOptions.builder()
                .preferredCountry("EE")
                .preferredLocale(Locale.ET)
                .build();

        Payment payment = Payment.builder()
                .method(PaymentMethodType.PAYMENT_INITIATION)
                .currency(Currency.EUR)
                .amount(new BigDecimal("100.00"))
                .methodOptions(options)
                .methodDisplay("Bank transfer")
                .build();

        assertEquals(PaymentMethodType.PAYMENT_INITIATION, payment.getMethod());
        assertEquals(Currency.EUR, payment.getCurrency());
        assertEquals(new BigDecimal("100.00"), payment.getAmount());
        assertEquals(options, payment.getMethodOptions());
        assertEquals("Bank transfer", payment.getMethodDisplay());
    }

    @Test
    void buildWithNullMethodThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> Payment.builder()
                        .currency(Currency.EUR)
                        .amount(BigDecimal.TEN)
                        .build()
        );

        assertEquals("method", exception.getField());
    }

    @Test
    void buildWithNullCurrencyThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> Payment.builder()
                        .method(PaymentMethodType.CARD_PAYMENTS)
                        .amount(BigDecimal.TEN)
                        .build()
        );

        assertEquals("currency", exception.getField());
    }

    @Test
    void buildWithNullAmountThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> Payment.builder()
                        .method(PaymentMethodType.CARD_PAYMENTS)
                        .currency(Currency.EUR)
                        .build()
        );

        assertEquals("amount", exception.getField());
    }

    @Test
    void buildWithZeroAmountThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> Payment.builder()
                        .method(PaymentMethodType.CARD_PAYMENTS)
                        .currency(Currency.EUR)
                        .amount(BigDecimal.ZERO)
                        .build()
        );

        assertEquals("amount", exception.getField());
    }

    @Test
    void buildWithNegativeAmountThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> Payment.builder()
                        .method(PaymentMethodType.CARD_PAYMENTS)
                        .currency(Currency.EUR)
                        .amount(new BigDecimal("-1"))
                        .build()
        );

        assertEquals("amount", exception.getField());
    }

    @Test
    void serializationRoundTrip() throws Exception {
        Payment payment = Payment.builder()
                .method(PaymentMethodType.CARD_PAYMENTS)
                .currency(Currency.EUR)
                .amount(new BigDecimal("50.00"))
                .methodDisplay("Card")
                .build();

        String json = mapper.writeValueAsString(payment);
        Payment deserialized = mapper.readValue(json, Payment.class);

        assertEquals(payment.getMethod(), deserialized.getMethod());
        assertEquals(payment.getCurrency(), deserialized.getCurrency());
        assertEquals(0, payment.getAmount().compareTo(deserialized.getAmount()));
        assertEquals(payment.getMethodDisplay(), deserialized.getMethodDisplay());
    }
}
