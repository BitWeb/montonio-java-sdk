package ee.bitweb.montonio.sdk.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MontonioApiExceptionTest {

    @Test
    void constructWithAllFields() {
        MontonioApiException exception = new MontonioApiException(422, "INVALID_AMOUNT", "The amount must be positive");

        assertEquals(422, exception.getStatusCode());
        assertEquals("INVALID_AMOUNT", exception.getErrorCode());
        assertEquals("The amount must be positive", exception.getErrorMessage());
    }

    @Test
    void messageFormatsWithAllFields() {
        MontonioApiException exception = new MontonioApiException(422, "INVALID_AMOUNT", "The amount must be positive");

        assertEquals("Montonio API error (HTTP 422): [INVALID_AMOUNT] The amount must be positive", exception.getMessage());
    }

    @Test
    void messageFormatsWithNullErrorCode() {
        MontonioApiException exception = new MontonioApiException(500, null, "Internal server error");

        assertEquals("Montonio API error (HTTP 500): Internal server error", exception.getMessage());
        assertNull(exception.getErrorCode());
    }

    @Test
    void messageFormatsWithNullErrorMessage() {
        MontonioApiException exception = new MontonioApiException(400, "BAD_REQUEST", null);

        assertEquals("Montonio API error (HTTP 400): [BAD_REQUEST]", exception.getMessage());
        assertNull(exception.getErrorMessage());
    }

    @Test
    void messageFormatsWithBothNullable() {
        MontonioApiException exception = new MontonioApiException(503, null, null);

        assertEquals("Montonio API error (HTTP 503)", exception.getMessage());
        assertNull(exception.getErrorCode());
        assertNull(exception.getErrorMessage());
    }

    @Test
    void isMontonioException() {
        MontonioApiException exception = new MontonioApiException(400, null, null);

        assertEquals(true, exception instanceof MontonioException);
    }
}
