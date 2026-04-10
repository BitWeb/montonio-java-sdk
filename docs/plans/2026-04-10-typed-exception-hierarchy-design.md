# Typed Exception Hierarchy Design

**Issue:** #13 — Typed exception hierarchy
**Date:** 2026-04-10
**Status:** Proposed

## Overview

Define a structured exception hierarchy to distinguish between different failure modes in the Montonio Java SDK. Consumers should be able to catch broadly (`MontonioException`) or handle specific failure types when needed.

## Design Decisions

| Decision                  | Choice                                      | Rationale                                                                      |
|---------------------------|---------------------------------------------|--------------------------------------------------------------------------------|
| Consumer handling pattern | Mix of broad and fine-grained               | Most catch broadly; hierarchy supports specific handling for those who need it |
| API error detail level    | Structured typed fields                     | Cleanest consumer experience; we control the SDK                               |
| Client-side validation    | Fail-fast (single error)                    | Simpler; YAGNI; can expand to collected errors later                           |
| Checked vs unchecked      | All unchecked (RuntimeException)            | Modern Java convention; avoids polluting consumer code                         |
| Auth exception placement  | Sibling of MontonioApiException             | Auth errors warrant fundamentally different handling than generic API errors   |
| Architecture              | Flat hierarchy with context fields          | Idiomatic Java, plays well with catch blocks, self-documenting                 |
| Boilerplate               | Lombok `@Getter`; hand-written constructors | Lombok can't delegate to `super()`, so constructors are manual                 |

## Exception Hierarchy

```
MontonioException (base, extends RuntimeException)
├── MontonioApiException            — API returned a non-success response
├── MontonioNetworkException        — connection/timeout failure
├── MontonioAuthenticationException — credential or token problem
└── MontonioValidationException     — invalid input detected client-side
```

All subtypes extend `MontonioException` directly. No deeper inheritance. All subtypes are `final`.

**Package:** `ee.bitweb.montonio.sdk.exception`

## Class Specifications

### MontonioException

Base exception for all SDK errors.

```java
package ee.bitweb.montonio.sdk.exception;

public class MontonioException extends RuntimeException {
    public MontonioException(String message) {
        super(message);
    }

    public MontonioException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

### MontonioApiException

Thrown when the Montonio API returns a non-success HTTP response.

```java
@Getter
public final class MontonioApiException extends MontonioException {
    private final int statusCode;
    private final String errorCode;    // nullable — API error code, e.g. "INVALID_AMOUNT"
    private final String errorMessage; // nullable — human-readable message from API

    public MontonioApiException(int statusCode, String errorCode, String errorMessage) {
        super(formatMessage(statusCode, errorCode, errorMessage));
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
```

**Message format:** `Montonio API error (HTTP {statusCode}): [{errorCode}] {errorMessage}`
- Omits brackets if `errorCode` is null
- Omits message portion if `errorMessage` is null

### MontonioNetworkException

Thrown on connection failures, timeouts, and other I/O errors.

```java
public final class MontonioNetworkException extends MontonioException {
    public MontonioNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

No extra fields. The `cause` (e.g., `SocketTimeoutException`) tells the story. Constructor always requires a cause.

### MontonioAuthenticationException

Thrown on credential or token problems.

```java
public final class MontonioAuthenticationException extends MontonioException {
    public MontonioAuthenticationException(String message) {
        super(message);
    }

    public MontonioAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

No extra fields. The message is descriptive enough to act on (e.g., `"Secret key is not configured for merchant 'EE'"`, `"JWT signature verification failed"`).

### MontonioValidationException

Thrown when client-side validation detects invalid input before sending a request. Fail-fast: one exception per validation failure.

```java
@Getter
public final class MontonioValidationException extends MontonioException {
    private final String field; // nullable — not all validations are field-specific

    public MontonioValidationException(String field, String message) {
        super(formatMessage(field, message));
        this.field = field;
    }

    public MontonioValidationException(String message) {
        super("Validation failed: " + message);
        this.field = null;
    }
}
```

**Message format:**
- With field: `Validation failed on field '{field}': {message}`
- Without field: `Validation failed: {message}`

## Testing Strategy

Each exception gets a dedicated test class in `src/test/java/ee/bitweb/montonio/sdk/exception/`.

**For each exception type, test:**
- Construction with all arguments — verify getters return correct values
- Message formatting — verify auto-formatted message matches expected pattern
- Cause chaining — verify `getCause()` propagates correctly
- Nullable fields — verify construction with nulls and graceful message formatting

**Specific test cases:**
- `MontonioApiException` — message formatting with all fields, null `errorCode`, null `errorMessage`, both null
- `MontonioNetworkException` — cause is always required
- `MontonioValidationException` — message with and without `field`
- `MontonioException` — both constructors (message-only, message+cause)

No integration tests needed. Target: 100% line coverage.
