package ee.bitweb.montonio.sdk;

import ee.bitweb.montonio.sdk.exception.MontonioValidationException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MontonioSdkConfigurationTest {

    @Test
    void buildWithRequiredFieldsOnlyAppliesDefaults() {
        MontonioSdkConfiguration config = MontonioSdkConfiguration.builder()
                .accessKey("test-access-key")
                .secretKey("test-secret-key")
                .build();

        assertEquals("test-access-key", config.getAccessKey());
        assertEquals("test-secret-key", config.getSecretKey());
        assertEquals(MontonioSdkConfiguration.SANDBOX_BASE_URL, config.getBaseUrl());
        assertEquals(Duration.ofSeconds(10), config.getConnectTimeout());
        assertEquals(Duration.ofSeconds(30), config.getRequestTimeout());
        assertEquals(Duration.ofMinutes(5), config.getTokenExpirationTime());
    }

    @Test
    void buildWithAllFieldsOverridden() {
        MontonioSdkConfiguration config = MontonioSdkConfiguration.builder()
                .accessKey("custom-access-key")
                .secretKey("custom-secret-key")
                .baseUrl(MontonioSdkConfiguration.PRODUCTION_BASE_URL)
                .connectTimeout(Duration.ofSeconds(5))
                .requestTimeout(Duration.ofSeconds(15))
                .tokenExpirationTime(Duration.ofMinutes(10))
                .build();

        assertEquals("custom-access-key", config.getAccessKey());
        assertEquals("custom-secret-key", config.getSecretKey());
        assertEquals(MontonioSdkConfiguration.PRODUCTION_BASE_URL, config.getBaseUrl());
        assertEquals(Duration.ofSeconds(5), config.getConnectTimeout());
        assertEquals(Duration.ofSeconds(15), config.getRequestTimeout());
        assertEquals(Duration.ofMinutes(10), config.getTokenExpirationTime());
    }

    @Test
    void sandboxBaseUrlConstant() {
        assertEquals("https://sandbox-stargate.montonio.com/api", MontonioSdkConfiguration.SANDBOX_BASE_URL);
    }

    @Test
    void productionBaseUrlConstant() {
        assertEquals("https://stargate.montonio.com/api", MontonioSdkConfiguration.PRODUCTION_BASE_URL);
    }

    @Test
    void buildWithNullAccessKeyThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> MontonioSdkConfiguration.builder()
                        .secretKey("test-secret-key")
                        .build()
        );

        assertEquals("accessKey", exception.getField());
    }

    @Test
    void buildWithNullSecretKeyThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> MontonioSdkConfiguration.builder()
                        .accessKey("test-access-key")
                        .build()
        );

        assertEquals("secretKey", exception.getField());
    }

    @Test
    void buildWithBlankAccessKeyThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> MontonioSdkConfiguration.builder()
                        .accessKey("   ")
                        .secretKey("test-secret-key")
                        .build()
        );

        assertEquals("accessKey", exception.getField());
    }

    @Test
    void buildWithBlankSecretKeyThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> MontonioSdkConfiguration.builder()
                        .accessKey("test-access-key")
                        .secretKey("   ")
                        .build()
        );

        assertEquals("secretKey", exception.getField());
    }
}
