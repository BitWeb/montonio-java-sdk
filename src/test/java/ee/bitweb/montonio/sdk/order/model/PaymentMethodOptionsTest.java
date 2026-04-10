package ee.bitweb.montonio.sdk.order.model;

import ee.bitweb.montonio.sdk.model.CardPaymentMethod;
import ee.bitweb.montonio.sdk.model.Locale;
import ee.bitweb.montonio.sdk.model.WalletProvider;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PaymentMethodOptionsTest {

    private final ObjectMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @Test
    void buildWithNoFieldsDefaultsToNull() {
        PaymentMethodOptions options = PaymentMethodOptions.builder().build();

        assertNull(options.getPreferredProvider());
        assertNull(options.getPreferredCountry());
        assertNull(options.getPreferredLocale());
        assertNull(options.getPreferredMethod());
        assertNull(options.getPreferredWallet());
        assertNull(options.getPaymentDescription());
        assertNull(options.getPaymentReference());
        assertNull(options.getPeriod());
    }

    @Test
    void buildPaymentInitiationOptions() {
        PaymentMethodOptions options = PaymentMethodOptions.builder()
                .preferredProvider("LHVBEE22")
                .preferredCountry("EE")
                .preferredLocale(Locale.ET)
                .paymentDescription("Payment for order 123")
                .paymentReference("RF123456")
                .build();

        assertEquals("LHVBEE22", options.getPreferredProvider());
        assertEquals("EE", options.getPreferredCountry());
        assertEquals(Locale.ET, options.getPreferredLocale());
        assertEquals("Payment for order 123", options.getPaymentDescription());
        assertEquals("RF123456", options.getPaymentReference());
    }

    @Test
    void buildCardPaymentOptions() {
        PaymentMethodOptions options = PaymentMethodOptions.builder()
                .preferredMethod(CardPaymentMethod.CARD)
                .preferredLocale(Locale.EN)
                .build();

        assertEquals(CardPaymentMethod.CARD, options.getPreferredMethod());
        assertEquals(Locale.EN, options.getPreferredLocale());
    }

    @Test
    void buildWalletOptions() {
        PaymentMethodOptions options = PaymentMethodOptions.builder()
                .preferredMethod(CardPaymentMethod.WALLET)
                .preferredWallet(WalletProvider.APPLE_PAY)
                .preferredLocale(Locale.EN)
                .build();

        assertEquals(CardPaymentMethod.WALLET, options.getPreferredMethod());
        assertEquals(WalletProvider.APPLE_PAY, options.getPreferredWallet());
    }

    @Test
    void buildBnplOptions() {
        PaymentMethodOptions options = PaymentMethodOptions.builder()
                .period(3)
                .build();

        assertEquals(3, options.getPeriod());
    }

    @Test
    void serializationRoundTrip() throws Exception {
        PaymentMethodOptions options = PaymentMethodOptions.builder()
                .preferredProvider("LHVBEE22")
                .preferredCountry("EE")
                .preferredLocale(Locale.ET)
                .paymentDescription("Payment for order 123")
                .build();

        String json = mapper.writeValueAsString(options);
        PaymentMethodOptions deserialized = mapper.readValue(json, PaymentMethodOptions.class);

        assertEquals(options.getPreferredProvider(), deserialized.getPreferredProvider());
        assertEquals(options.getPreferredCountry(), deserialized.getPreferredCountry());
        assertEquals(options.getPreferredLocale(), deserialized.getPreferredLocale());
        assertEquals(options.getPaymentDescription(), deserialized.getPaymentDescription());
    }
}
