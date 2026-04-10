# HTTP Client Infrastructure Design

**Issue:** #11
**Date:** 2026-04-10
**Status:** Accepted

## Summary

Internal HTTP client wrapping `java.net.http.HttpClient` for all outgoing Montonio API calls. Synchronous, JSON-based, no framework dependencies. Uses Jackson 3 for serialization.

## Design Decisions

| # | Decision                     | Choice                                              | Rationale                                                     |
|---|------------------------------|-----------------------------------------------------|---------------------------------------------------------------|
| 1 | Consumer-supplied HttpClient | No, internal only                                   | Keep it simple; consumer injection deferred to a future issue |
| 2 | JSON library                 | Jackson 3 (`tools.jackson.core:jackson-databind`)   | Industry standard, Lombok-friendly, modern module system      |
| 3 | HTTP methods                 | GET and POST only                                   | YAGNI; covers all known Montonio endpoints                    |
| 4 | Request/response logging     | Out of scope                                        | Moved to a separate issue to keep this focused                |
| 5 | Method signatures            | Generic type-safe (`<T> T get(path, responseType)`) | Callers get deserialized objects; no boilerplate              |
| 6 | Configuration                | Accept `MontonioSdkConfiguration` directly          | Internal component; simple constructor, no decoupling needed  |
| 7 | Visibility                   | Public class, public constructor                    | Revisit when top-level SDK entry point is designed            |

## Public API

```java
package ee.bitweb.montonio.sdk.http;

public class MontonioHttpClient {

    public MontonioHttpClient(MontonioSdkConfiguration configuration) { ... }

    public <T> T get(String path, Class<T> responseType) { ... }

    public <T> T post(String path, Object body, Class<T> responseType) { ... }
}
```

### Constructor

Creates a `java.net.http.HttpClient` with `connectTimeout` from configuration. Creates a Jackson `ObjectMapper` configured to ignore unknown properties.

### Parameters

- **`path`** — relative path (e.g., `/orders`, `/orders/{uuid}`). Prepended with `baseUrl` from configuration.
- **`body`** — request body object, serialized to JSON via Jackson.
- **`responseType`** — target class for JSON deserialization of the response body.

### Headers

All requests include:
- `Content-Type: application/json`
- `Accept: application/json`

### Timeout

`requestTimeout` from configuration applied per-request via `HttpRequest.Builder.timeout()`.

### Authentication

Out of scope. JWT `Authorization` header will be added when the authentication issue is implemented.

## Internal Implementation

### Fields

```java
private final HttpClient httpClient;
private final ObjectMapper objectMapper;
private final MontonioSdkConfiguration configuration;
```

### HttpClient Construction

```java
this.httpClient = HttpClient.newBuilder()
    .connectTimeout(configuration.getConnectTimeout())
    .build();
```

### Response Handling Flow

1. Send request via `httpClient.send()`, get body as `String`
2. If 2xx — deserialize body to `responseType` using `objectMapper`
3. If 4xx/5xx — best-effort parse of error body, throw `MontonioApiException`
4. `IOException` — wrap in `MontonioNetworkException`
5. `InterruptedException` — restore interrupt flag, wrap in `MontonioNetworkException`
6. Jackson deserialization failure on 2xx — wrap in `MontonioException`

## Error Response Parsing

Best-effort extraction of structured error fields from non-2xx responses:

```java
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
```

If the body is not JSON or lacks the expected fields, falls back to raw body as `errorMessage`. Never throws — always produces a usable exception.

Field names (`errorCode`, `message`) may need adjustment once validated against the real Montonio API.

## Dependencies

```groovy
implementation 'tools.jackson.core:jackson-databind:3.0.1'
```

Jackson 3 uses the `tools.jackson` group ID. The core module pulls in `jackson-core` and `jackson-annotations` transitively.

## Testing Strategy

### Test Infrastructure

Package-private constructor accepts an `HttpClient` instance for testing:

```java
MontonioHttpClient(MontonioSdkConfiguration configuration, HttpClient httpClient) { ... }
```

The public constructor creates its own `HttpClient`; tests use the package-private one to inject stubs.

### Test Cases

| Test                      | Expected                                                          |
|---------------------------|-------------------------------------------------------------------|
| Successful GET            | Deserializes response body to target type                         |
| Successful POST           | Serializes request body, deserializes response                    |
| 4xx with JSON error body  | `MontonioApiException` with parsed `errorCode` and `errorMessage` |
| 5xx with non-JSON body    | `MontonioApiException` with raw body as `errorMessage`            |
| Connection failure        | `MontonioNetworkException`                                        |
| Request timeout           | `MontonioNetworkException`                                        |
| Malformed JSON on 2xx     | `MontonioException`                                               |
| Path appended to base URL | URI = `baseUrl + path`                                            |

All unit tests — no real HTTP calls. Target near-perfect coverage.

## Out of Scope

- Consumer-supplied `HttpClient` injection (future issue)
- Request/response logging (future issue)
- JWT authentication header (future issue)
- PUT, DELETE, PATCH methods (add when needed)
