package ee.bitweb.montonio.sdk.http;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import ee.bitweb.montonio.sdk.MontonioSdkConfiguration;
import ee.bitweb.montonio.sdk.auth.MontonioTokenProvider;
import ee.bitweb.montonio.sdk.exception.MontonioApiException;
import ee.bitweb.montonio.sdk.exception.MontonioException;
import ee.bitweb.montonio.sdk.exception.MontonioNetworkException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MontonioHttpClientTest {

    private static final String BASE_URL = "https://api.example.com";
    private static final String ACCESS_KEY = "test-access-key";
    private static final String SECRET_KEY = "test-secret-key-that-is-long-enough";
    private static final Instant FIXED_NOW = Instant.parse("2026-01-15T10:00:00Z");

    private MontonioSdkConfiguration configuration;
    private MontonioTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        configuration = MontonioSdkConfiguration.builder()
                .accessKey(ACCESS_KEY)
                .secretKey(SECRET_KEY)
                .baseUrl(BASE_URL)
                .connectTimeout(Duration.ofSeconds(5))
                .requestTimeout(Duration.ofSeconds(10))
                .build();

        ObjectMapper objectMapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        tokenProvider = new MontonioTokenProvider(
                configuration, objectMapper, Clock.fixed(FIXED_NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void getDeserializesSuccessfulResponse() {
        HttpClient stubClient = new StubHttpClient(200, "{\"name\":\"test\",\"value\":42}");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient, tokenProvider);

        TestResponse response = client.get("/test", TestResponse.class);

        assertEquals("test", response.name);
        assertEquals(42, response.value);
    }

    @Test
    void getBuildsCorrectUri() {
        StubHttpClient stubClient = new StubHttpClient(200, "{\"name\":\"test\",\"value\":1}");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient, tokenProvider);

        client.get("/orders/123", TestResponse.class);

        assertEquals(URI.create(BASE_URL + "/orders/123"), stubClient.capturedRequest.uri());
    }

    @Test
    void getSetsJsonHeaders() {
        StubHttpClient stubClient = new StubHttpClient(200, "{\"name\":\"test\",\"value\":1}");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient, tokenProvider);

        client.get("/test", TestResponse.class);

        HttpHeaders headers = stubClient.capturedRequest.headers();
        assertEquals(List.of("application/json"), headers.allValues("Content-Type"));
        assertEquals(List.of("application/json"), headers.allValues("Accept"));
    }

    @Test
    void getSetsBearerAuthorizationHeader() {
        StubHttpClient stubClient = new StubHttpClient(200, "{\"name\":\"test\",\"value\":1}");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient, tokenProvider);

        client.get("/test", TestResponse.class);

        HttpHeaders headers = stubClient.capturedRequest.headers();
        List<String> authHeaders = headers.allValues("Authorization");
        assertEquals(1, authHeaders.size());
        assertTrue(authHeaders.get(0).startsWith("Bearer "));

        // Verify the JWT in the header
        String jwt = authHeaders.get(0).substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        DecodedJWT decoded = JWT.require(algorithm)
                .acceptExpiresAt(365 * 24 * 3600)
                .build()
                .verify(jwt);
        assertEquals(ACCESS_KEY, decoded.getClaim("accessKey").asString());
    }

    @Test
    void postDoesNotSetAuthorizationHeader() {
        StubHttpClient stubClient = new StubHttpClient(200, "{\"name\":\"test\",\"value\":1}");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient, tokenProvider);

        client.post("/test", new TestRequest("hello"), TestResponse.class);

        HttpHeaders headers = stubClient.capturedRequest.headers();
        assertTrue(headers.allValues("Authorization").isEmpty());
    }

    @Test
    void getSetsRequestTimeout() {
        StubHttpClient stubClient = new StubHttpClient(200, "{\"name\":\"test\",\"value\":1}");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient, tokenProvider);

        client.get("/test", TestResponse.class);

        assertEquals(
                Optional.of(Duration.ofSeconds(10)),
                stubClient.capturedRequest.timeout()
        );
    }

    @Test
    void getUsesGetMethod() {
        StubHttpClient stubClient = new StubHttpClient(200, "{\"name\":\"test\",\"value\":1}");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient, tokenProvider);

        client.get("/test", TestResponse.class);

        assertEquals("GET", stubClient.capturedRequest.method());
    }

    @Test
    void postWrapsBodyAsJwtInDataField() {
        StubHttpClient stubClient = new StubHttpClient(200, "{\"name\":\"created\",\"value\":99}");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient, tokenProvider);

        TestResponse response = client.post("/test", new TestRequest("hello"), TestResponse.class);

        assertEquals("created", response.name);
        assertEquals(99, response.value);

        String requestBody = extractRequestBody(stubClient.capturedRequest);
        assertTrue(requestBody.startsWith("{\"data\":\""), "POST body should be {\"data\":\"<jwt>\"}");
        assertTrue(requestBody.endsWith("\"}"), "POST body should end with \"}");

        // Extract and verify the JWT
        String jwt = requestBody.substring("{\"data\":\"".length(), requestBody.length() - 2);
        Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
        DecodedJWT decoded = JWT.require(algorithm)
                .acceptExpiresAt(365 * 24 * 3600)
                .build()
                .verify(jwt);
        assertEquals(ACCESS_KEY, decoded.getClaim("accessKey").asString());
        assertEquals("hello", decoded.getClaim("data").asString());
    }

    @Test
    void postUsesPostMethod() {
        StubHttpClient stubClient = new StubHttpClient(200, "{\"name\":\"test\",\"value\":1}");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient, tokenProvider);

        client.post("/test", new TestRequest("hello"), TestResponse.class);

        assertEquals("POST", stubClient.capturedRequest.method());
    }

    @Test
    void errorResponseWithJsonBodyThrowsApiExceptionWithParsedFields() {
        String errorBody = "{\"errorCode\":\"ORDER_NOT_FOUND\",\"message\":\"Order does not exist\"}";
        HttpClient stubClient = new StubHttpClient(404, errorBody);
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient, tokenProvider);

        MontonioApiException exception = assertThrows(
                MontonioApiException.class,
                () -> client.get("/orders/missing", TestResponse.class)
        );

        assertEquals(404, exception.getStatusCode());
        assertEquals("ORDER_NOT_FOUND", exception.getErrorCode());
        assertEquals("Order does not exist", exception.getErrorMessage());
    }

    @Test
    void errorResponseWithNonJsonBodyThrowsApiExceptionWithRawBody() {
        HttpClient stubClient = new StubHttpClient(500, "Internal Server Error");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient, tokenProvider);

        MontonioApiException exception = assertThrows(
                MontonioApiException.class,
                () -> client.get("/test", TestResponse.class)
        );

        assertEquals(500, exception.getStatusCode());
        assertNull(exception.getErrorCode());
        assertEquals("Internal Server Error", exception.getErrorMessage());
    }

    @Test
    void errorResponseWithPartialJsonFieldsThrowsApiExceptionWithAvailableFields() {
        String errorBody = "{\"message\":\"Something went wrong\"}";
        HttpClient stubClient = new StubHttpClient(400, errorBody);
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient, tokenProvider);

        MontonioApiException exception = assertThrows(
                MontonioApiException.class,
                () -> client.get("/test", TestResponse.class)
        );

        assertEquals(400, exception.getStatusCode());
        assertNull(exception.getErrorCode());
        assertEquals("Something went wrong", exception.getErrorMessage());
    }

    @Test
    void connectionFailureThrowsNetworkException() {
        HttpClient stubClient = new IoExceptionHttpClient(new IOException("Connection refused"));
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient, tokenProvider);

        MontonioNetworkException exception = assertThrows(
                MontonioNetworkException.class,
                () -> client.get("/test", TestResponse.class)
        );

        assertTrue(exception.getMessage().contains("Connection refused"));
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void interruptedRequestThrowsNetworkExceptionAndRestoresInterruptFlag() {
        HttpClient stubClient = new InterruptedHttpClient();
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient, tokenProvider);

        MontonioNetworkException exception = assertThrows(
                MontonioNetworkException.class,
                () -> client.get("/test", TestResponse.class)
        );

        assertEquals("Request interrupted", exception.getMessage());
        assertInstanceOf(InterruptedException.class, exception.getCause());
        assertTrue(Thread.currentThread().isInterrupted());

        // Clear the interrupt flag for test cleanup
        Thread.interrupted();
    }

    @Test
    void malformedJsonResponseOnSuccessThrowsMontonioException() {
        HttpClient stubClient = new StubHttpClient(200, "not json at all");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient, tokenProvider);

        MontonioException exception = assertThrows(
                MontonioException.class,
                () -> client.get("/test", TestResponse.class)
        );

        assertTrue(exception.getMessage().contains("Failed to deserialize response body"));
    }

    @Test
    void errorResponseWithNullJsonFieldsThrowsApiExceptionWithNulls() {
        String errorBody = "{\"errorCode\":null,\"message\":null}";
        HttpClient stubClient = new StubHttpClient(422, errorBody);
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient, tokenProvider);

        MontonioApiException exception = assertThrows(
                MontonioApiException.class,
                () -> client.get("/test", TestResponse.class)
        );

        assertEquals(422, exception.getStatusCode());
        assertNull(exception.getErrorCode());
        assertNull(exception.getErrorMessage());
    }

    // --- Helpers ---

    private static String extractRequestBody(HttpRequest request) {
        return request.bodyPublisher()
                .map(publisher -> {
                    var subscriber = HttpResponse.BodySubscribers.ofString(java.nio.charset.StandardCharsets.UTF_8);
                    publisher.subscribe(new java.util.concurrent.Flow.Subscriber<>() {
                        @Override public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
                            subscriber.onSubscribe(subscription);
                        }
                        @Override public void onNext(java.nio.ByteBuffer item) { subscriber.onNext(List.of(item)); }
                        @Override public void onError(Throwable throwable) { subscriber.onError(throwable); }
                        @Override public void onComplete() { subscriber.onComplete(); }
                    });
                    return subscriber.getBody().toCompletableFuture().join();
                })
                .orElse("");
    }

    // --- Test DTOs ---

    static class TestResponse {
        public String name;
        public int value;
    }

    static class TestRequest {
        public String data;

        TestRequest(String data) {
            this.data = data;
        }
    }

    // --- Stub HttpClient implementations ---

    private static class StubHttpClient extends HttpClient {

        private final int statusCode;
        private final String responseBody;
        HttpRequest capturedRequest;

        StubHttpClient(int statusCode, String responseBody) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
                throws IOException, InterruptedException {
            this.capturedRequest = request;
            return (HttpResponse<T>) new StubHttpResponse(statusCode, responseBody, request);
        }

        @Override public Optional<java.net.CookieHandler> cookieHandler() { return Optional.empty(); }
        @Override public Optional<Duration> connectTimeout() { return Optional.empty(); }
        @Override public Redirect followRedirects() { return Redirect.NEVER; }
        @Override public Optional<java.net.ProxySelector> proxy() { return Optional.empty(); }
        @Override public javax.net.ssl.SSLContext sslContext() { return null; }
        @Override public javax.net.ssl.SSLParameters sslParameters() { return null; }
        @Override public Optional<java.net.Authenticator> authenticator() { return Optional.empty(); }
        @Override public Version version() { return Version.HTTP_2; }
        @Override public Optional<java.util.concurrent.Executor> executor() { return Optional.empty(); }

        @Override
        public <T> java.util.concurrent.CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> java.util.concurrent.CompletableFuture<HttpResponse<T>> sendAsync(
                HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler,
                HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            throw new UnsupportedOperationException();
        }
    }

    private static class IoExceptionHttpClient extends StubHttpClient {

        private final IOException exception;

        IoExceptionHttpClient(IOException exception) {
            super(0, "");
            this.exception = exception;
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
                throws IOException {
            throw exception;
        }
    }

    private static class InterruptedHttpClient extends StubHttpClient {

        InterruptedHttpClient() {
            super(0, "");
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
                throws InterruptedException {
            throw new InterruptedException("interrupted");
        }
    }

    private static class StubHttpResponse implements HttpResponse<String> {

        private final int statusCode;
        private final String body;
        private final HttpRequest request;

        StubHttpResponse(int statusCode, String body, HttpRequest request) {
            this.statusCode = statusCode;
            this.body = body;
            this.request = request;
        }

        @Override public int statusCode() { return statusCode; }
        @Override public String body() { return body; }
        @Override public HttpRequest request() { return request; }
        @Override public Optional<HttpResponse<String>> previousResponse() { return Optional.empty(); }
        @Override public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (a, b) -> true); }
        @Override public URI uri() { return request.uri(); }
        @Override public HttpClient.Version version() { return HttpClient.Version.HTTP_2; }
        @Override public Optional<SSLSession> sslSession() { return Optional.empty(); }
    }
}
