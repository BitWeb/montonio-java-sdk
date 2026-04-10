# Get Order Status — Design

**Issue:** #16
**Date:** 2026-04-10

## Summary

Add a `GET /orders/{uuid}` operation exposed through a domain-scoped facade:
`MontonioClient(config).orders().get(uuid)` returning the existing `OrderResponse`.

## Public API

```java
MontonioClient client = new MontonioClient(configuration);
OrderResponse order = client.orders().get("order-uuid");
```

### Classes Introduced

| Class | Package | Role |
|-------|---------|------|
| `MontonioClient` | `ee.bitweb.montonio.sdk` | Top-level facade, owns `MontonioHttpClient`, lazy-creates domain services |
| `OrderService` | `ee.bitweb.montonio.sdk.order` | Validates input, delegates `GET /orders/{uuid}` to HTTP client |

No new models — reuses `OrderResponse`, `PaymentIntent`, `PaymentStatus`, and all supporting types from #14.

### Dependency Flow

```
MontonioClient → MontonioHttpClient → HttpClient (java.net)
       ↓
  OrderService (receives MontonioHttpClient)
```

## MontonioClient

- Constructor takes `MontonioSdkConfiguration`, creates `MontonioHttpClient` internally.
- Package-private constructor overload accepts `MontonioHttpClient` for testing.
- `orders()` uses lazy-cached initialization: creates `OrderService` on first call, reuses it after.
- Not thread-safe on `orders()` — benign race at worst. Matches typical single-threaded SDK usage.

## OrderService

- Package-private constructor receives `MontonioHttpClient` — users go through `MontonioClient`.
- `get(String uuid)` validates null/blank (throws `MontonioValidationException` with field `"uuid"`), then delegates to `httpClient.get("/orders/" + uuid, OrderResponse.class)`.
- No additional error handling — `MontonioHttpClient` already maps 4xx/5xx to `MontonioApiException` and network failures to `MontonioNetworkException`.

## Testing

### OrderServiceTest

Unit tests with stubbed HTTP client (same pattern as `MontonioHttpClientTest`):

| Test case | Setup | Assertion |
|-----------|-------|-----------|
| Successful retrieval | Stub 200, full JSON | All fields deserialized, including nested paymentIntents |
| Null UUID | `get(null)` | `MontonioValidationException` with field `"uuid"` |
| Blank UUID | `get("  ")` | `MontonioValidationException` with field `"uuid"` |
| Order not found | Stub 404 with error JSON | `MontonioApiException` with status 404 |
| Multiple payment intents | Stub 200, 2+ intents | List size and individual fields correct |
| Various payment statuses | Stub 200, PAID/PENDING/VOIDED | `PaymentStatus` enum deserialized correctly |

### MontonioClientTest

| Test case | Assertion |
|-----------|-----------|
| `orders()` returns non-null | Basic wiring |
| `orders()` returns same instance | Lazy-caching works |
| Constructor rejects null config | Validation error |

## Decisions

- **Domain-scoped facade** (`client.orders().get()`) over flat methods or standalone services — scales as more domains are added.
- **Lazy-cached domain services** — avoids allocating services never used, avoids re-allocating on every call.
- **Full `OrderResponse` return** — no slimmer DTO; callers pick fields they need.
- **Null/blank validation only** — no UUID format regex; let the API decide validity beyond that.
- **Package-private constructors** — testability without exposing internals to SDK consumers.
