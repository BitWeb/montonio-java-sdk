package ee.bitweb.montonio.sdk.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import ee.bitweb.montonio.sdk.MontonioSdkConfiguration;
import ee.bitweb.montonio.sdk.exception.MontonioAuthenticationException;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class MontonioTokenProvider {

    private static final Duration RENEWAL_BUFFER = Duration.ofSeconds(30);

    private final MontonioSdkConfiguration configuration;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Algorithm algorithm;

    private String cachedToken;
    private Instant cachedTokenExpiry;

    public MontonioTokenProvider(MontonioSdkConfiguration configuration, ObjectMapper objectMapper) {
        this(configuration, objectMapper, Clock.systemUTC());
    }

    public MontonioTokenProvider(MontonioSdkConfiguration configuration, ObjectMapper objectMapper, Clock clock) {
        this.configuration = configuration;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.algorithm = Algorithm.HMAC256(configuration.getSecretKey());
    }

    public synchronized String getAuthToken() {
        Instant now = clock.instant();

        if (cachedToken != null && cachedTokenExpiry != null
                && now.plus(RENEWAL_BUFFER).isBefore(cachedTokenExpiry)) {
            return cachedToken;
        }

        Instant exp = now.plus(configuration.getTokenExpirationTime());

        try {
            cachedToken = JWT.create()
                    .withClaim("accessKey", configuration.getAccessKey())
                    .withIssuedAt(now)
                    .withExpiresAt(exp)
                    .sign(algorithm);
            cachedTokenExpiry = exp;
        } catch (JWTCreationException e) {
            throw new MontonioAuthenticationException("Failed to generate auth token", e);
        }

        return cachedToken;
    }

    public String getDataToken(Object body) {
        Map<String, Object> claims = convertToClaimsMap(body);

        Instant now = clock.instant();
        Instant exp = now.plus(configuration.getTokenExpirationTime());

        try {
            var builder = JWT.create()
                    .withClaim("accessKey", configuration.getAccessKey())
                    .withIssuedAt(now)
                    .withExpiresAt(exp);

            addClaimsFromMap(builder, claims);

            return builder.sign(algorithm);
        } catch (JWTCreationException e) {
            throw new MontonioAuthenticationException("Failed to generate data token", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToClaimsMap(Object body) {
        try {
            return objectMapper.convertValue(body, Map.class);
        } catch (Exception e) {
            throw new MontonioAuthenticationException("Failed to serialize request body for signing", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void addClaimsFromMap(com.auth0.jwt.JWTCreator.Builder builder, Map<String, Object> claims) {
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                builder.withNullClaim(key);
            } else if (value instanceof String s) {
                builder.withClaim(key, s);
            } else if (value instanceof Integer i) {
                builder.withClaim(key, i);
            } else if (value instanceof Long l) {
                builder.withClaim(key, l);
            } else if (value instanceof Double d) {
                builder.withClaim(key, d);
            } else if (value instanceof Boolean b) {
                builder.withClaim(key, b);
            } else if (value instanceof Map) {
                builder.withClaim(key, (Map<String, ?>) value);
            } else if (value instanceof List) {
                builder.withClaim(key, (List<?>) value);
            } else {
                builder.withClaim(key, value.toString());
            }
        }
    }
}
