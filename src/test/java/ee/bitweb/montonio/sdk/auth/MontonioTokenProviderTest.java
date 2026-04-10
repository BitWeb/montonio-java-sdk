package ee.bitweb.montonio.sdk.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import ee.bitweb.montonio.sdk.MontonioSdkConfiguration;
import ee.bitweb.montonio.sdk.exception.MontonioAuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class MontonioTokenProviderTest {

    private static final String ACCESS_KEY = "test-access-key";
    private static final String SECRET_KEY = "test-secret-key-that-is-long-enough";
    private static final Instant FIXED_NOW = Instant.parse("2026-01-15T10:00:00Z");
    private static final Duration TOKEN_EXPIRATION = Duration.ofMinutes(5);

    private MontonioSdkConfiguration configuration;
    private ObjectMapper objectMapper;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        configuration = MontonioSdkConfiguration.builder()
                .accessKey(ACCESS_KEY)
                .secretKey(SECRET_KEY)
                .tokenExpirationTime(TOKEN_EXPIRATION)
                .build();

        objectMapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        fixedClock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
    }

    @Test
    void authTokenHasCorrectJwtHeaders() {
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper, fixedClock);

        String token = provider.getAuthToken();

        DecodedJWT decoded = JWT.decode(token);
        assertEquals("HS256", decoded.getAlgorithm());
        assertEquals("JWT", decoded.getType());
    }

    @Test
    void authTokenContainsAccessKeyClaim() {
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper, fixedClock);

        String token = provider.getAuthToken();

        DecodedJWT decoded = JWT.decode(token);
        assertEquals(ACCESS_KEY, decoded.getClaim("accessKey").asString());
    }

    @Test
    void authTokenHasCorrectIssuedAtAndExpiration() {
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper, fixedClock);

        String token = provider.getAuthToken();

        DecodedJWT decoded = JWT.decode(token);
        assertEquals(FIXED_NOW.getEpochSecond(), decoded.getIssuedAt().toInstant().getEpochSecond());
        assertEquals(
                FIXED_NOW.plus(TOKEN_EXPIRATION).getEpochSecond(),
                decoded.getExpiresAt().toInstant().getEpochSecond()
        );
    }

    @Test
    void authTokenSignatureIsVerifiable() {
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper, fixedClock);

        String token = provider.getAuthToken();

        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        DecodedJWT verified = JWT.require(algorithm)
                .acceptExpiresAt(365 * 24 * 3600)
                .build()
                .verify(token);
        assertEquals(ACCESS_KEY, verified.getClaim("accessKey").asString());
    }

    @Test
    void authTokenIsCachedWhenNotExpired() {
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper, fixedClock);

        String first = provider.getAuthToken();
        String second = provider.getAuthToken();

        assertSame(first, second);
    }

    @Test
    void authTokenIsRenewedWhenWithinBufferWindow() {
        Instant initialTime = FIXED_NOW;
        // Advance clock to 4 minutes 35 seconds later — within 30s of the 5-minute expiry
        Instant nearExpiry = initialTime.plus(Duration.ofMinutes(4)).plus(Duration.ofSeconds(35));

        MutableClock clock = new MutableClock(initialTime);
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper, clock);

        String first = provider.getAuthToken();

        clock.setInstant(nearExpiry);
        String second = provider.getAuthToken();

        assertNotEquals(first, second);
    }

    @Test
    void authTokenIsNotRenewedWhenOutsideBufferWindow() {
        Instant initialTime = FIXED_NOW;
        // Advance clock to 4 minutes — still well before the 30s buffer
        Instant withinValidity = initialTime.plus(Duration.ofMinutes(4));

        MutableClock clock = new MutableClock(initialTime);
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper, clock);

        String first = provider.getAuthToken();

        clock.setInstant(withinValidity);
        String second = provider.getAuthToken();

        assertSame(first, second);
    }

    @Test
    void dataTokenContainsRequestBodyFieldsAsTopLevelClaims() {
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper, fixedClock);

        Map<String, Object> body = Map.of(
                "merchantReference", "ORDER-123",
                "grandTotal", 99.99,
                "currency", "EUR"
        );

        String token = provider.getDataToken(body);

        DecodedJWT decoded = JWT.decode(token);
        assertEquals("ORDER-123", decoded.getClaim("merchantReference").asString());
        assertEquals(99.99, decoded.getClaim("grandTotal").asDouble());
        assertEquals("EUR", decoded.getClaim("currency").asString());
    }

    @Test
    void dataTokenContainsAccessKeyAndTimestamps() {
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper, fixedClock);

        String token = provider.getDataToken(Map.of("key", "value"));

        DecodedJWT decoded = JWT.decode(token);
        assertEquals(ACCESS_KEY, decoded.getClaim("accessKey").asString());
        assertEquals(FIXED_NOW.getEpochSecond(), decoded.getIssuedAt().toInstant().getEpochSecond());
        assertEquals(
                FIXED_NOW.plus(TOKEN_EXPIRATION).getEpochSecond(),
                decoded.getExpiresAt().toInstant().getEpochSecond()
        );
    }

    @Test
    void dataTokenHandlesNestedObjects() {
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper, fixedClock);

        Map<String, Object> body = Map.of(
                "merchantReference", "ORDER-123",
                "billingAddress", Map.of(
                        "firstName", "John",
                        "lastName", "Doe",
                        "country", "EE"
                )
        );

        String token = provider.getDataToken(body);

        DecodedJWT decoded = JWT.decode(token);
        assertEquals("ORDER-123", decoded.getClaim("merchantReference").asString());
        Map<String, Object> address = decoded.getClaim("billingAddress").asMap();
        assertEquals("John", address.get("firstName"));
        assertEquals("Doe", address.get("lastName"));
        assertEquals("EE", address.get("country"));
    }

    @Test
    void dataTokenHandlesListClaims() {
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper, fixedClock);

        Map<String, Object> body = Map.of(
                "lineItems", List.of(
                        Map.of("name", "Hoverboard", "quantity", 1, "finalPrice", 99.99)
                )
        );

        String token = provider.getDataToken(body);

        DecodedJWT decoded = JWT.decode(token);
        List<Object> items = decoded.getClaim("lineItems").asList(Object.class);
        assertNotNull(items);
        assertEquals(1, items.size());
    }

    @Test
    void dataTokenSignatureIsVerifiable() {
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper, fixedClock);

        String token = provider.getDataToken(Map.of("key", "value"));

        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        assertDoesNotThrow(() -> JWT.require(algorithm)
                .acceptExpiresAt(365 * 24 * 3600)
                .build()
                .verify(token));
    }

    @Test
    void dataTokenIsNotCached() {
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper, fixedClock);

        String first = provider.getDataToken(Map.of("key", "value1"));
        String second = provider.getDataToken(Map.of("key", "value2"));

        assertNotEquals(first, second);
    }

    @Test
    void dataTokenWithTypedObjectConvertsToClaimsCorrectly() {
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper, fixedClock);

        TestOrderRequest order = new TestOrderRequest();
        order.merchantReference = "ORDER-456";
        order.grandTotal = 150.0;
        order.currency = "EUR";

        String token = provider.getDataToken(order);

        DecodedJWT decoded = JWT.decode(token);
        assertEquals("ORDER-456", decoded.getClaim("merchantReference").asString());
        assertEquals(150.0, decoded.getClaim("grandTotal").asDouble());
        assertEquals("EUR", decoded.getClaim("currency").asString());
        assertEquals(ACCESS_KEY, decoded.getClaim("accessKey").asString());
    }

    @Test
    void nonSerializableBodyThrowsAuthenticationException() {
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper, fixedClock);

        Object nonSerializable = new Object() {
            @SuppressWarnings("unused")
            public Object getSelf() { return this; }
        };

        MontonioAuthenticationException exception = assertThrows(
                MontonioAuthenticationException.class,
                () -> provider.getDataToken(nonSerializable)
        );

        assertTrue(exception.getMessage().contains("Failed to serialize request body for signing"));
    }

    @Test
    void concurrentAuthTokenAccessProducesValidTokens() throws InterruptedException {
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper, fixedClock);
        int threadCount = 20;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        CopyOnWriteArrayList<String> tokens = new CopyOnWriteArrayList<>();
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    String token = provider.getAuthToken();
                    tokens.add(token);
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertTrue(errors.isEmpty(), "Concurrent access produced errors: " + errors);
        assertEquals(threadCount, tokens.size());

        // All threads should get the same cached token
        String firstToken = tokens.get(0);
        for (String token : tokens) {
            assertEquals(firstToken, token);
        }

        // All tokens should be verifiable
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        assertDoesNotThrow(() -> JWT.require(algorithm)
                .acceptExpiresAt(365 * 24 * 3600)
                .build()
                .verify(firstToken));
    }

    @Test
    void constructorWithoutClockUsesSystemClock() {
        MontonioTokenProvider provider = new MontonioTokenProvider(configuration, objectMapper);

        String token = provider.getAuthToken();

        DecodedJWT decoded = JWT.decode(token);
        long iat = decoded.getIssuedAt().toInstant().getEpochSecond();
        long now = Instant.now().getEpochSecond();
        assertTrue(Math.abs(now - iat) < 5, "Token iat should be close to current time");
    }

    // --- Helpers ---

    static class TestOrderRequest {
        public String merchantReference;
        public Double grandTotal;
        public String currency;
    }

    private static class MutableClock extends Clock {
        private Instant instant;

        MutableClock(Instant instant) {
            this.instant = instant;
        }

        void setInstant(Instant instant) {
            this.instant = instant;
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
