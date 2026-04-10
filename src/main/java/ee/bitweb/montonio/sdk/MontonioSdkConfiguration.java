package ee.bitweb.montonio.sdk;

import ee.bitweb.montonio.sdk.exception.MontonioValidationException;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

@Getter
@Builder
public class MontonioSdkConfiguration {

    public static final String SANDBOX_BASE_URL = "https://sandbox-stargate.montonio.com/api";
    public static final String PRODUCTION_BASE_URL = "https://stargate.montonio.com/api";

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_TOKEN_EXPIRATION_TIME = Duration.ofMinutes(5);

    private final String accessKey;
    private final String secretKey;
    private final String baseUrl;
    private final Duration connectTimeout;
    private final Duration requestTimeout;
    private final Duration tokenExpirationTime;

    public static class MontonioSdkConfigurationBuilder {

        public MontonioSdkConfiguration build() {
            if (accessKey == null || accessKey.isBlank()) {
                throw new MontonioValidationException("accessKey", "must not be null or blank");
            }
            if (secretKey == null || secretKey.isBlank()) {
                throw new MontonioValidationException("secretKey", "must not be null or blank");
            }

            return new MontonioSdkConfiguration(
                    accessKey,
                    secretKey,
                    baseUrl != null ? baseUrl : SANDBOX_BASE_URL,
                    connectTimeout != null ? connectTimeout : DEFAULT_CONNECT_TIMEOUT,
                    requestTimeout != null ? requestTimeout : DEFAULT_REQUEST_TIMEOUT,
                    tokenExpirationTime != null ? tokenExpirationTime : DEFAULT_TOKEN_EXPIRATION_TIME
            );
        }
    }
}
