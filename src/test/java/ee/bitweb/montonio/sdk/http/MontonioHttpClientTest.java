package ee.bitweb.montonio.sdk.http;

import ee.bitweb.montonio.sdk.MontonioSdkConfiguration;
import ee.bitweb.montonio.sdk.exception.MontonioApiException;
import ee.bitweb.montonio.sdk.exception.MontonioException;
import ee.bitweb.montonio.sdk.exception.MontonioNetworkException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MontonioHttpClientTest {

    private static final String BASE_URL = "https://api.example.com";

    private MontonioSdkConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = MontonioSdkConfiguration.builder()
                .accessKey("test-access-key")
                .secretKey("test-secret-key")
                .baseUrl(BASE_URL)
                .connectTimeout(Duration.ofSeconds(5))
                .requestTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Test
    void getDeserializesSuccessfulResponse() {
        HttpClient stubClient = new StubHttpClient(200, "{\"name\":\"test\",\"value\":42}");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient);

        TestResponse response = client.get("/test", TestResponse.class);

        assertEquals("test", response.name);
        assertEquals(42, response.value);
    }

    @Test
    void getBuildsCorrectUri() {
        StubHttpClient stubClient = new StubHttpClient(200, "{\"name\":\"test\",\"value\":1}");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient);

        client.get("/orders/123", TestResponse.class);

        assertEquals(URI.create(BASE_URL + "/orders/123"), stubClient.capturedRequest.uri());
    }

    @Test
    void getSetsJsonHeaders() {
        StubHttpClient stubClient = new StubHttpClient(200, "{\"name\":\"test\",\"value\":1}");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient);

        client.get("/test", TestResponse.class);

        HttpHeaders headers = stubClient.capturedRequest.headers();
        assertEquals(List.of("application/json"), headers.allValues("Content-Type"));
        assertEquals(List.of("application/json"), headers.allValues("Accept"));
    }

    @Test
    void getSetsRequestTimeout() {
        StubHttpClient stubClient = new StubHttpClient(200, "{\"name\":\"test\",\"value\":1}");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient);

        client.get("/test", TestResponse.class);

        assertEquals(
                Optional.of(Duration.ofSeconds(10)),
                stubClient.capturedRequest.timeout()
        );
    }

    @Test
    void getUsesGetMethod() {
        StubHttpClient stubClient = new StubHttpClient(200, "{\"name\":\"test\",\"value\":1}");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient);

        client.get("/test", TestResponse.class);

        assertEquals("GET", stubClient.capturedRequest.method());
    }

    @Test
    void postSerializesBodyAndDeserializesResponse() {
        StubHttpClient stubClient = new StubHttpClient(200, "{\"name\":\"created\",\"value\":99}");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient);

        TestResponse response = client.post("/test", new TestRequest("hello"), TestResponse.class);

        assertEquals("created", response.name);
        assertEquals(99, response.value);
    }

    @Test
    void postUsesPostMethod() {
        StubHttpClient stubClient = new StubHttpClient(200, "{\"name\":\"test\",\"value\":1}");
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient);

        client.post("/test", new TestRequest("hello"), TestResponse.class);

        assertEquals("POST", stubClient.capturedRequest.method());
    }

    @Test
    void errorResponseWithJsonBodyThrowsApiExceptionWithParsedFields() {
        String errorBody = "{\"errorCode\":\"ORDER_NOT_FOUND\",\"message\":\"Order does not exist\"}";
        HttpClient stubClient = new StubHttpClient(404, errorBody);
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient);

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
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient);

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
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient);

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
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient);

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
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient);

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
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient);

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
        MontonioHttpClient client = new MontonioHttpClient(configuration, stubClient);

        MontonioApiException exception = assertThrows(
                MontonioApiException.class,
                () -> client.get("/test", TestResponse.class)
        );

        assertEquals(422, exception.getStatusCode());
        assertNull(exception.getErrorCode());
        assertNull(exception.getErrorMessage());
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
