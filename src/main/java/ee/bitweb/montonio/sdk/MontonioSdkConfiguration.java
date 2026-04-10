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

    private final String accessKey;
    private final String secretKey;

    @Builder.Default
    private final String baseUrl = SANDBOX_BASE_URL;

    @Builder.Default
    private final Duration connectTimeout = Duration.ofSeconds(10);

    @Builder.Default
    private final Duration requestTimeout = Duration.ofSeconds(30);

    @Builder.Default
    private final Duration tokenExpirationTime = Duration.ofMinutes(5);

    MontonioSdkConfiguration(
            String accessKey,
            String secretKey,
            String baseUrl,
            Duration connectTimeout,
            Duration requestTimeout,
            Duration tokenExpirationTime
    ) {
        if (accessKey == null || accessKey.isBlank()) {
            throw new MontonioValidationException("accessKey", "must not be null or blank");
        }
        if (secretKey == null || secretKey.isBlank()) {
            throw new MontonioValidationException("secretKey", "must not be null or blank");
        }
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.baseUrl = baseUrl;
        this.connectTimeout = connectTimeout;
        this.requestTimeout = requestTimeout;
        this.tokenExpirationTime = tokenExpirationTime;
    }
}
