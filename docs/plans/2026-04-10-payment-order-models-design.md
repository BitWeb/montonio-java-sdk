# Payment Order Models Design

**Date:** 2026-04-10
**Issue:** #14 — Payment order models — requests, responses, and enums
**Status:** Implemented

## Overview

Define all DTOs and enums needed for the Montonio payment order API. This covers the
`POST /orders` request/response and the `GET /orders/{uuid}` response, including nested
models for payment details, addresses, line items, and payment intents.

## Sources

Field definitions were derived by cross-referencing:

- [montonio/montonio-js](https://github.com/montonio/montonio-js) — official front-end SDK (enums, payment statuses)
- [maitkaa/montonio-client](https://github.com/maitkaa/montonio-client) — community TypeScript client (full Order, OrderResponse, PaymentIntent types)
- [montonio/montonio-shopware-php-sdk](https://github.com/montonio/montonio-shopware-php-sdk) — official PHP SDK (PaymentData, Address, LineItem, Payment structs)
- Issue #14 requirements

## Design Decisions

### Package layout — feature-based

```text
ee.bitweb.montonio.sdk.model/           — shared enums (Currency, Locale, etc.)
ee.bitweb.montonio.sdk.order.model/     — order-scoped models and enums
ee.bitweb.montonio.sdk.order.request/   — request DTOs
ee.bitweb.montonio.sdk.order.response/  — response DTOs
```

Reusable types (`Currency`, `Locale`, `PaymentMethodType`, `CardPaymentMethod`,
`WalletProvider`) live in `sdk.model`. Order-specific types (`PaymentStatus`, `Address`,
`LineItem`, `Payment`, `PaymentMethodOptions`) live under `sdk.order.model`.

### Enum serialization — `@JsonValue` with wire-value field

Each enum constant stores its API wire value in a `String value` field, exposed via
`@JsonValue`. This decouples Java naming conventions (UPPER_CASE) from the API's mixed
casing (e.g., `paymentInitiation`, `cardPayments`, `EUR`, `en`).

### Request DTOs — Lombok `@Builder` with constructor validation

Request DTOs use `@Builder` + `@Getter` with fail-fast validation in a `@JsonCreator`
constructor, consistent with `MontonioSdkConfiguration`. Required fields throw
`MontonioValidationException` if null/blank.

### Response DTOs — immutable with `@JsonCreator`

Response DTOs are `final` classes with all-args `@JsonCreator` constructors, `@Getter`,
and final fields. The `-parameters` compiler flag lets Jackson resolve constructor
parameter names without `@JsonProperty` annotations.

### `@JsonCreator` over `@Jacksonized`

Lombok's `@Jacksonized` generates `@com.fasterxml.jackson.databind.annotation.JsonDeserialize`,
but Jackson 3 moved that annotation to `tools.jackson.databind.annotation`. To avoid
compatibility issues, all classes use explicit `@JsonCreator` constructors.

### Monetary amounts — `BigDecimal` for requests, `String` for responses

Request DTOs use `BigDecimal` to avoid floating-point precision issues. Response DTOs
use `String` for `grandTotal`, `amount`, and `serviceFee` because that is what the API
returns on the wire.

### Nullability — `@jakarta.annotation.Nullable`

Optional fields are annotated with `@Nullable`. Absence of the annotation implies
non-null by contract.

### `PaymentMethodOptions` — single flat class

Rather than a type hierarchy mirroring the TypeScript union type
(`PaymentInitiationOptions | CardPaymentsOptions | ...`), all method-specific options
are merged into one class with all fields nullable. This avoids polymorphic
deserialization complexity for no real gain — the fields don't conflict.

### Refund fields — omitted

`OrderResponse` in the TypeScript client includes `refunds`, `availableForRefund`, and
`isRefundableType`. Refund-related fields are mostly omitted (only `isRefundableType`
is included) pending follow-up issue #30 for refund models. Jackson ignores unknown fields.

## File Inventory

### Shared enums (`sdk.model`)

| File | Wire values |
|------|-------------|
| `Currency.java` | `EUR`, `PLN` |
| `Locale.java` | `de`, `en`, `et`, `fi`, `lt`, `lv`, `pl`, `ru` |
| `PaymentMethodType.java` | `paymentInitiation`, `cardPayments`, `blik`, `bnpl`, `hirePurchase` |
| `CardPaymentMethod.java` | `card`, `wallet` |
| `WalletProvider.java` | `applePay`, `googlePay` |

### Order models (`order.model`)

| File | Description |
|------|-------------|
| `PaymentStatus.java` | 10 statuses: PENDING through AUTHORIZED |
| `Address.java` | 15 optional fields (name, email, phone, address, company) |
| `LineItem.java` | `name`, `quantity`, `finalPrice` (all required) |
| `Payment.java` | `method`, `currency`, `amount` (required) + `methodOptions`, `methodDisplay` |
| `PaymentMethodOptions.java` | 8 optional fields covering all payment method types |

### Request DTOs (`order.request`)

| File | Required fields |
|------|----------------|
| `CreateOrderRequest.java` | `merchantReference`, `returnUrl`, `notificationUrl`, `grandTotal`, `currency`, `payment` |

### Response DTOs (`order.response`)

| File | Key fields |
|------|------------|
| `CreateOrderResponse.java` | `uuid`, `paymentUrl` |
| `OrderResponse.java` | 21 fields including nested `paymentIntents`, `lineItems`, addresses |
| `PaymentIntent.java` | `uuid`, `paymentMethodType`, `amount`, `status`, `serviceFee`, etc. |

## Testing

Each class has a corresponding test class covering:

- Enum constant count and wire value assertions
- Serialization/deserialization round-trips via Jackson `ObjectMapper`
- Builder construction with all fields and required-only fields
- Validation — `MontonioValidationException` for null/blank required fields
- Unknown JSON field tolerance (deserialization with `FAIL_ON_UNKNOWN_PROPERTIES` disabled)
