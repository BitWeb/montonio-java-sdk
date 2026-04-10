# SDK Configuration Design

**Issue:** #10 — Core configuration and multi-merchant credential support
**Date:** 2026-04-10
**Status:** Proposed

## Overview

Expand `MontonioSdkConfiguration` from a minimal base-URL holder into a complete, immutable configuration object with credentials, timeouts, and token settings. Built via Lombok `@Builder` with validation at construction time.

Multi-merchant support is intentionally deferred — consumers who need multiple merchants simply create multiple configuration instances.

## Design Decisions

| Decision                   | Choice                                       | Rationale                                                                       |
|----------------------------|----------------------------------------------|---------------------------------------------------------------------------------|
| Multi-merchant approach    | Multiple instances, no built-in registry     | YAGNI; consumers manage their own instances                                     |
| Environment selection      | Base URL string + constants                  | Simple, flexible; enum wrapper adds little value for a rarely changed URL       |
| Timeout granularity        | Connect + request (two fields)               | Matches `java.net.http.HttpClient` natively; no separate write timeout needed   |
| Timeout type               | `java.time.Duration`                         | Idiomatic, self-documenting units, native HttpClient compatibility              |
| Construction               | Lombok `@Builder` with custom `build()`      | Fluent API, immutable result, validation at construction time                   |
| Mutability                 | Immutable (`@Getter` only, `final` fields)   | Safer for concurrent use; no reason to mutate after construction                |
| Defaults                   | Sensible defaults for all optional fields    | Works out of the box with just credentials                                      |
| Validation                 | Fail-fast in `build()` via `MontonioValidationException` | Catches missing credentials early; reuses existing exception type     |

## Configuration Fields

| Field                | Type       | Default                          | Required |
|----------------------|------------|----------------------------------|----------|
| `accessKey`          | `String`   | none                             | yes      |
| `secretKey`          | `String`   | none                             | yes      |
| `baseUrl`            | `String`   | `SANDBOX_BASE_URL`               | no       |
| `connectTimeout`     | `Duration` | 10 seconds                       | no       |
| `requestTimeout`     | `Duration` | 30 seconds                       | no       |
| `tokenExpirationTime`| `Duration` | 5 minutes                        | no       |

## Constants

```java
public static final String SANDBOX_BASE_URL = "https://sandbox-stargate.montonio.com/api";
public static final String PRODUCTION_BASE_URL = "https://stargate.montonio.com/api";
```

## Class Specification

```java
package ee.bitweb.montonio.sdk;

import ee.bitweb.montonio.sdk.exception.MontonioValidationException;
import lombok.Builder;
import lombok.Getter;

import java.time.Duration;

@Getter
@Builder
public class MontonioSdkConfiguration {

    public static final String SANDBOX_BASE_URL = "https://sandbox-stargate.montonio.com/api";
    public static final String PRODUCTION_BASE_URL = "https://stargate.montonio.com/api";

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_TOKEN_EXPIRATION_TIME = Duration.ofMinutes(5);

    private final String accessKey;
    private final String secretKey;
    private final String baseUrl;
    private final Duration connectTimeout;
    private final Duration requestTimeout;
    private final Duration tokenExpirationTime;

    public static class MontonioSdkConfigurationBuilder {
        public MontonioSdkConfiguration build() {
            if (accessKey == null || accessKey.isBlank()) {
                throw new MontonioValidationException("accessKey", "must not be null or blank");
            }
            if (secretKey == null || secretKey.isBlank()) {
                throw new MontonioValidationException("secretKey", "must not be null or blank");
            }
            return new MontonioSdkConfiguration(
                    accessKey,
                    secretKey,
                    baseUrl != null ? baseUrl : SANDBOX_BASE_URL,
                    connectTimeout != null ? connectTimeout : DEFAULT_CONNECT_TIMEOUT,
                    requestTimeout != null ? requestTimeout : DEFAULT_REQUEST_TIMEOUT,
                    tokenExpirationTime != null ? tokenExpirationTime : DEFAULT_TOKEN_EXPIRATION_TIME
            );
        }
    }
}
```

**Note:** Defaults are applied in the custom `build()` method rather than via `@Builder.Default`, since Lombok's `@Builder.Default` doesn't initialize `$value` fields until the generated `build()` is called — which we override.

## Usage Examples

### Minimal (sandbox, defaults)

```java
MontonioSdkConfiguration config = MontonioSdkConfiguration.builder()
        .accessKey("your-access-key")
        .secretKey("your-secret-key")
        .build();
```

### Production with custom timeouts

```java
MontonioSdkConfiguration config = MontonioSdkConfiguration.builder()
        .accessKey("your-access-key")
        .secretKey("your-secret-key")
        .baseUrl(MontonioSdkConfiguration.PRODUCTION_BASE_URL)
        .connectTimeout(Duration.ofSeconds(5))
        .requestTimeout(Duration.ofSeconds(15))
        .tokenExpirationTime(Duration.ofMinutes(10))
        .build();
```

### Multi-merchant (consumer-managed)

```java
MontonioSdkConfiguration eeConfig = MontonioSdkConfiguration.builder()
        .accessKey("ee-access-key")
        .secretKey("ee-secret-key")
        .build();

MontonioSdkConfiguration lvConfig = MontonioSdkConfiguration.builder()
        .accessKey("lv-access-key")
        .secretKey("lv-secret-key")
        .build();
```

## Testing Strategy

**Test class:** `MontonioSdkConfigurationTest` (replaces existing minimal version)

**Test cases:**

| Test | What it verifies |
|------|------------------|
| Builder with required fields only | All defaults applied: sandbox URL, 10s connect, 30s request, 5min token |
| Builder with all fields overridden | Each custom value returned by getter |
| Production URL constant | `PRODUCTION_BASE_URL` equals `https://stargate.montonio.com/api` |
| Sandbox URL constant | `SANDBOX_BASE_URL` equals `https://sandbox-stargate.montonio.com/api` |
| Missing accessKey (null) | Throws `MontonioValidationException` with field `"accessKey"` |
| Missing secretKey (null) | Throws `MontonioValidationException` with field `"secretKey"` |
| Blank accessKey | Throws `MontonioValidationException` with field `"accessKey"` |
| Blank secretKey | Throws `MontonioValidationException` with field `"secretKey"` |

No integration tests needed. Target: 100% line coverage.
