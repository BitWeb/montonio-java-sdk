package ee.bitweb.montonio.sdk.http;

import ee.bitweb.montonio.sdk.MontonioSdkConfiguration;
import ee.bitweb.montonio.sdk.auth.MontonioTokenProvider;
import ee.bitweb.montonio.sdk.exception.MontonioApiException;
import ee.bitweb.montonio.sdk.exception.MontonioException;
import ee.bitweb.montonio.sdk.exception.MontonioNetworkException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class MontonioHttpClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final MontonioSdkConfiguration configuration;
    private final MontonioTokenProvider tokenProvider;

    public MontonioHttpClient(MontonioSdkConfiguration configuration) {
        this(
                configuration,
                HttpClient.newBuilder()
                        .connectTimeout(configuration.getConnectTimeout())
                        .build()
        );
    }

    MontonioHttpClient(MontonioSdkConfiguration configuration, HttpClient httpClient) {
        this.configuration = configuration;
        this.httpClient = httpClient;
        this.objectMapper = createObjectMapper();
        this.tokenProvider = new MontonioTokenProvider(configuration, objectMapper);
    }

    public MontonioHttpClient(MontonioSdkConfiguration configuration, HttpClient httpClient,
                              MontonioTokenProvider tokenProvider) {
        this.configuration = configuration;
        this.httpClient = httpClient;
        this.objectMapper = createObjectMapper();
        this.tokenProvider = tokenProvider;
    }

    private static ObjectMapper createObjectMapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    public <T> T get(String path, Class<T> responseType) {
        HttpRequest request = newRequestBuilder(path)
                .header("Authorization", "Bearer " + tokenProvider.getAuthToken())
                .GET()
                .build();

        return execute(request, responseType);
    }

    public <T> T post(String path, Object body, Class<T> responseType) {
        String token = tokenProvider.getDataToken(body);
        String json = serialize(Map.of("data", token));

        HttpRequest request = newRequestBuilder(path)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return execute(request, responseType);
    }

    private HttpRequest.Builder newRequestBuilder(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(configuration.getBaseUrl() + path))
                .timeout(configuration.getRequestTimeout())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }

    private <T> T execute(HttpRequest request, Class<T> responseType) {
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new MontonioNetworkException("Request failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MontonioNetworkException("Request interrupted", e);
        }

        int statusCode = response.statusCode();
        String responseBody = response.body();

        if (statusCode >= 200 && statusCode < 300) {
            return deserialize(responseBody, responseType);
        }

        throw buildApiException(statusCode, responseBody);
    }

    private String serialize(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new MontonioException("Failed to serialize request body", e);
        }
    }

    private <T> T deserialize(String json, Class<T> responseType) {
        try {
            return objectMapper.readValue(json, responseType);
        } catch (Exception e) {
            throw new MontonioException("Failed to deserialize response body", e);
        }
    }

    private MontonioApiException buildApiException(int statusCode, String responseBody) {
        try {
            JsonNode node = objectMapper.readTree(responseBody);
            String errorCode = optionalText(node, "errorCode");
            String errorMessage = optionalText(node, "message");
            return new MontonioApiException(statusCode, errorCode, errorMessage);
        } catch (Exception e) {
            return new MontonioApiException(statusCode, null, responseBody);
        }
    }

    private static String optionalText(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.stringValue();
    }
}
