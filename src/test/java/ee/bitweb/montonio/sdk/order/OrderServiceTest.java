package ee.bitweb.montonio.sdk.order;

import ee.bitweb.montonio.sdk.MontonioSdkConfiguration;
import ee.bitweb.montonio.sdk.auth.MontonioTokenProvider;
import ee.bitweb.montonio.sdk.exception.MontonioApiException;
import ee.bitweb.montonio.sdk.exception.MontonioValidationException;
import ee.bitweb.montonio.sdk.http.MontonioHttpClient;
import ee.bitweb.montonio.sdk.model.Currency;
import ee.bitweb.montonio.sdk.model.PaymentMethodType;
import ee.bitweb.montonio.sdk.order.model.PaymentStatus;
import ee.bitweb.montonio.sdk.order.response.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private static final String BASE_URL = "https://api.example.com";
    private static final String ACCESS_KEY = "test-access-key";
    private static final String SECRET_KEY = "test-secret-key-that-is-long-enough";

    private MontonioSdkConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = MontonioSdkConfiguration.builder()
                .accessKey(ACCESS_KEY)
                .secretKey(SECRET_KEY)
                .baseUrl(BASE_URL)
                .connectTimeout(Duration.ofSeconds(5))
                .requestTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Test
    void getReturnsDeserializedOrderResponse() {
        String json = fullOrderJson("PAID", "paymentInitiation");
        OrderService service = createServiceWithStub(200, json);

        OrderResponse response = service.get("order-uuid-123");

        assertEquals("order-uuid-123", response.getUuid());
        assertEquals(PaymentStatus.PAID, response.getPaymentStatus());
        assertEquals("Order1234567", response.getMerchantReference());
        assertEquals("100.00", response.getGrandTotal());
        assertEquals(Currency.EUR, response.getCurrency());
        assertEquals(PaymentMethodType.PAYMENT_INITIATION, response.getPaymentMethodType());
    }

    @Test
    void getBuildsCorrectPath() {
        StubHttpClient stubClient = new StubHttpClient(200, fullOrderJson("PENDING", "paymentInitiation"));
        OrderService service = createServiceWithStub(stubClient);

        service.get("my-order-uuid");

        assertEquals(
                URI.create(BASE_URL + "/orders/my-order-uuid"),
                stubClient.capturedRequest.uri()
        );
    }

    @Test
    void getTrimsUuidBeforeBuildingPath() {
        StubHttpClient stubClient = new StubHttpClient(200, fullOrderJson("PAID", "paymentInitiation"));
        OrderService service = createServiceWithStub(stubClient);

        service.get("  order-uuid  ");

        assertEquals(
                URI.create(BASE_URL + "/orders/order-uuid"),
                stubClient.capturedRequest.uri()
        );
    }

    @Test
    void getWithNullUuidThrowsValidationException() {
        OrderService service = createServiceWithStub(200, "{}");

        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> service.get(null)
        );

        assertEquals("uuid", exception.getField());
        assertTrue(exception.getMessage().contains("must not be null or blank"));
    }

    @Test
    void getWithBlankUuidThrowsValidationException() {
        OrderService service = createServiceWithStub(200, "{}");

        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> service.get("   ")
        );

        assertEquals("uuid", exception.getField());
    }

    @Test
    void getWithEmptyUuidThrowsValidationException() {
        OrderService service = createServiceWithStub(200, "{}");

        assertThrows(MontonioValidationException.class, () -> service.get(""));
    }

    @Test
    void getWithNotFoundOrderThrowsApiException() {
        String errorJson = "{\"errorCode\":\"ORDER_NOT_FOUND\",\"message\":\"Order does not exist\"}";
        OrderService service = createServiceWithStub(404, errorJson);

        MontonioApiException exception = assertThrows(
                MontonioApiException.class,
                () -> service.get("nonexistent-uuid")
        );

        assertEquals(404, exception.getStatusCode());
        assertEquals("ORDER_NOT_FOUND", exception.getErrorCode());
        assertEquals("Order does not exist", exception.getErrorMessage());
    }

    @Test
    void getDeserializesMultiplePaymentIntents() {
        String json = """
                {
                    "uuid": "order-uuid",
                    "paymentStatus": "PAID",
                    "locale": "en",
                    "merchantReference": "ref",
                    "merchantReferenceDisplay": "ref",
                    "merchantReturnUrl": "http://example.com",
                    "merchantNotificationUrl": "http://example.com",
                    "grandTotal": "200.00",
                    "currency": "EUR",
                    "paymentMethodType": "cardPayments",
                    "storeUuid": "store-uuid",
                    "paymentIntents": [
                        {
                            "uuid": "intent-1",
                            "paymentMethodType": "paymentInitiation",
                            "amount": "100.00",
                            "currency": "EUR",
                            "status": "PAID",
                            "serviceFee": "0.50",
                            "serviceFeeCurrency": "EUR",
                            "createdAt": "2026-04-10T12:00:00Z"
                        },
                        {
                            "uuid": "intent-2",
                            "paymentMethodType": "cardPayments",
                            "amount": "100.00",
                            "currency": "EUR",
                            "status": "AUTHORIZED",
                            "serviceFee": "1.00",
                            "serviceFeeCurrency": "EUR",
                            "createdAt": "2026-04-10T12:01:00Z",
                            "paymentMethodMetadata": {
                                "preferredCountry": "EE"
                            }
                        }
                    ],
                    "lineItems": [],
                    "billingAddress": {},
                    "shippingAddress": {},
                    "expiresAt": "2026-04-10T13:00:00Z",
                    "createdAt": "2026-04-10T12:00:00Z",
                    "storeName": "Store",
                    "businessName": "Business",
                    "paymentUrl": "https://example.com/pay"
                }
                """;
        OrderService service = createServiceWithStub(200, json);

        OrderResponse response = service.get("order-uuid");

        assertEquals(2, response.getPaymentIntents().size());
        assertEquals("intent-1", response.getPaymentIntents().get(0).getUuid());
        assertEquals(PaymentMethodType.PAYMENT_INITIATION, response.getPaymentIntents().get(0).getPaymentMethodType());
        assertEquals(PaymentStatus.PAID, response.getPaymentIntents().get(0).getStatus());
        assertEquals("intent-2", response.getPaymentIntents().get(1).getUuid());
        assertEquals(PaymentMethodType.CARD_PAYMENTS, response.getPaymentIntents().get(1).getPaymentMethodType());
        assertEquals(PaymentStatus.AUTHORIZED, response.getPaymentIntents().get(1).getStatus());
        assertEquals("EE", response.getPaymentIntents().get(1).getPaymentMethodMetadata().get("preferredCountry"));
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus.class)
    void getDeserializesAllPaymentStatuses(PaymentStatus status) {
        String json = fullOrderJson(status.getValue(), "paymentInitiation");
        OrderService service = createServiceWithStub(200, json);

        OrderResponse response = service.get("order-uuid");

        assertEquals(status, response.getPaymentStatus());
    }

    // --- Helpers ---

    private OrderService createServiceWithStub(int statusCode, String responseBody) {
        return createServiceWithStub(new StubHttpClient(statusCode, responseBody));
    }

    private OrderService createServiceWithStub(StubHttpClient stubClient) {
        ObjectMapper objectMapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        MontonioTokenProvider tokenProvider = new MontonioTokenProvider(
                configuration, objectMapper,
                Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC)
        );
        MontonioHttpClient httpClient = new MontonioHttpClient(configuration, stubClient, tokenProvider);
        return new OrderService(httpClient);
    }

    private static String fullOrderJson(String paymentStatus, String paymentMethodType) {
        return """
                {
                    "uuid": "order-uuid-123",
                    "paymentStatus": "%s",
                    "locale": "en",
                    "merchantReference": "Order1234567",
                    "merchantReferenceDisplay": "Order 1234567",
                    "merchantReturnUrl": "http://localhost:3000/return",
                    "merchantNotificationUrl": "http://example.com/notify",
                    "grandTotal": "100.00",
                    "currency": "EUR",
                    "paymentMethodType": "%s",
                    "storeUuid": "store-uuid",
                    "paymentIntents": [
                        {
                            "uuid": "intent-uuid",
                            "paymentMethodType": "paymentInitiation",
                            "amount": "100.00",
                            "currency": "EUR",
                            "status": "PAID",
                            "serviceFee": "0.00",
                            "serviceFeeCurrency": "EUR",
                            "createdAt": "2026-04-10T12:00:00Z"
                        }
                    ],
                    "lineItems": [],
                    "billingAddress": {},
                    "shippingAddress": {},
                    "expiresAt": "2026-04-10T12:10:00Z",
                    "createdAt": "2026-04-10T12:00:00Z",
                    "storeName": "Test Store",
                    "businessName": "Test Business",
                    "paymentUrl": "https://example.com/pay"
                }
                """.formatted(paymentStatus, paymentMethodType);
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
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
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
