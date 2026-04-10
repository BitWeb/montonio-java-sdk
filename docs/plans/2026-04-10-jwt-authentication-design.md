# JWT Authentication Design

**Issue:** #12 — JWT authentication: token generation and request signing
**Date:** 2026-04-10
**Status:** Accepted

## Overview

Implement JWT (HS256) authentication for the Montonio Stargate API. The API uses two distinct authentication modes:

- **GET requests** — JWT sent as a Bearer token in the `Authorization` header. Token contains only `accessKey`, `iat`, and `exp`. Cacheable and reusable across requests.
- **POST requests** — the request payload is serialized into JWT claims alongside `accessKey`, `iat`, and `exp`. The signed JWT is sent as the HTTP body wrapped in `{"data": "<token>"}`. Unique per request.

## Dependencies

**New dependency:**

```groovy
implementation 'com.auth0:java-jwt:4.4.0'
```

Chosen over JJWT because it is a single artifact with no Jackson version conflicts (the SDK uses Jackson 3.x; JJWT's Jackson module requires Jackson 2.x).

## Architecture

### New class: `MontonioTokenProvider`

**Package:** `ee.bitweb.montonio.sdk.auth`

Responsible for all JWT generation. Thread-safe via `synchronized` blocks. Injected into `MontonioHttpClient`.

```java
package ee.bitweb.montonio.sdk.auth;

public class MontonioTokenProvider {

    public MontonioTokenProvider(
        MontonioSdkConfiguration configuration,
        ObjectMapper objectMapper
    );

    // For testing — injectable clock
    public MontonioTokenProvider(
        MontonioSdkConfiguration configuration,
        ObjectMapper objectMapper,
        Clock clock
    );

    // GET requests — cached, regenerates when expiring within 30s
    public String getAuthToken();

    // POST requests — serializes body into JWT claims, no caching
    public String getDataToken(Object body);
}
```

**Internal state (guarded by `synchronized`):**
- `cachedToken` — current GET auth token string
- `cachedTokenExpiry` — `Instant` when the cached token expires

**`getAuthToken()` flow:**
1. Acquire lock (`synchronized(this)`)
2. If `cachedToken` is non-null and `cachedTokenExpiry` is more than 30 seconds from now, return `cachedToken`
3. Otherwise: compute `iat` (now), `exp` (now + `tokenExpirationTime`), sign with HS256 using `secretKey`
4. Cache token and expiry, return

**`getDataToken(Object body)` flow:**
1. Use `objectMapper.convertValue(body, Map)` to flatten the request object
2. Add `accessKey`, `iat`, `exp` to the claims map
3. Sign with HS256 using `secretKey`
4. Return the JWT string (no caching)

### Changes to `MontonioHttpClient`

**Constructors:**

```java
// Public — creates its own TokenProvider
public MontonioHttpClient(MontonioSdkConfiguration configuration);

// Package-private — for testing with injected dependencies
MontonioHttpClient(
    MontonioSdkConfiguration configuration,
    HttpClient httpClient,
    MontonioTokenProvider tokenProvider
);
```

The `ObjectMapper` is created once in `MontonioHttpClient` and shared with `MontonioTokenProvider`.

**GET flow:**

```java
public <T> T get(String path, Class<T> responseType) {
    HttpRequest request = newRequestBuilder(path)
            .header("Authorization", "Bearer " + tokenProvider.getAuthToken())
            .GET()
            .build();
    return execute(request, responseType);
}
```

**POST flow:**

```java
public <T> T post(String path, Object body, Class<T> responseType) {
    String token = tokenProvider.getDataToken(body);
    String json = serialize(Map.of("data", token));

    HttpRequest request = newRequestBuilder(path)
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
    return execute(request, responseType);
}
```

The public API (`get()` and `post()`) remains unchanged for SDK consumers — JWT handling is fully internal.

## Error Handling

Token generation failures throw `MontonioAuthenticationException` (already exists):

- `getAuthToken()` — signing failure: `"Failed to generate auth token"`
- `getDataToken()` — body conversion failure: `"Failed to serialize request body for signing"`, signing failure: `"Failed to generate data token"`

No retry logic — these are local CPU operations, so failure indicates a configuration problem (bad key), not a transient issue. No new exception classes needed.

## Thread Safety

- `synchronized(this)` guards all reads/writes to `cachedToken` and `cachedTokenExpiry`
- Token generation (HMAC-SHA256) takes microseconds, so lock contention is negligible
- `getDataToken()` requires no synchronisation as it does not access shared state

## Testing Strategy

### `MontonioTokenProviderTest`

- **Token structure** — decode generated JWT, verify `alg: HS256`, `typ: JWT` headers
- **Auth token claims** — verify `accessKey`, `iat`, `exp` present and correct
- **Data token claims** — verify request body fields as top-level claims alongside `accessKey`, `iat`, `exp`
- **Nested objects** — verify complex bodies are correctly represented in claims
- **Expiration** — use injected `Clock` to verify `exp` = `iat` + configured `tokenExpirationTime`
- **Caching** — call `getAuthToken()` twice quickly, verify same string returned
- **Cache renewal** — advance `Clock` past buffer window, verify new token generated
- **Thread safety** — concurrent threads calling `getAuthToken()`, verify no exceptions and all receive valid tokens
- **Invalid secret key** — verify `MontonioAuthenticationException` thrown
- **Signature verification** — decode token with same secret using auth0 verifier, confirm valid

### `MontonioHttpClientTest` updates

- **GET requests** — verify `Authorization: Bearer <token>` header present
- **POST requests** — verify body is `{"data": "<jwt>"}`, decode JWT and confirm original request fields plus `accessKey`/`exp`/`iat`
- Uses injected `MontonioTokenProvider` with test `Clock` for deterministic assertions

No integration tests in this scope — JWT generation is entirely local.

## Decisions log

| Decision | Choice | Alternatives considered |
|---|---|---|
| JWT library | `com.auth0:java-jwt` | JJWT (Jackson 3.x conflict), manual JDK implementation (maintenance burden) |
| Architecture | Dedicated `MontonioTokenProvider` in `auth` package | Inline in HttpClient (mixed concerns), interceptor pattern (over-engineered) |
| POST body serialization | Jackson `convertValue` to flat map, merge auth claims | Nested `data` wrapper claim (doesn't match API expectations) |
| GET token caching | Lazy with 30s buffer | Background `ScheduledExecutorService` renewal (unnecessary complexity) |
| Thread safety | `synchronized` block | `ReadWriteLock`, volatile + DCL, `AtomicReference` + CAS (all overkill for microsecond operations) |
