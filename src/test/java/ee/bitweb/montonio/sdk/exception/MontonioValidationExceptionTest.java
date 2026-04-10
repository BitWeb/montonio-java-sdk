package ee.bitweb.montonio.sdk.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class MontonioValidationExceptionTest {

    @Test
    void constructWithFieldAndMessage() {
        MontonioValidationException exception = new MontonioValidationException("amount", "must not be null");

        assertEquals("amount", exception.getField());
        assertEquals("Validation failed on field 'amount': must not be null", exception.getMessage());
    }

    @Test
    void constructWithMessageOnly() {
        MontonioValidationException exception = new MontonioValidationException("at least one line item is required");

        assertNull(exception.getField());
        assertEquals("Validation failed: at least one line item is required", exception.getMessage());
    }

    @Test
    void constructWithFieldMessageAndCause() {
        Throwable cause = new NumberFormatException("not a number");

        MontonioValidationException exception = new MontonioValidationException("amount", "must be a valid number", cause);

        assertEquals("amount", exception.getField());
        assertEquals("Validation failed on field 'amount': must be a valid number", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void constructWithMessageAndCause() {
        Throwable cause = new IllegalArgumentException("parse error");

        MontonioValidationException exception = new MontonioValidationException("invalid payload", cause);

        assertNull(exception.getField());
        assertEquals("Validation failed: invalid payload", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void isMontonioException() {
        MontonioValidationException exception = new MontonioValidationException("test");

        assertEquals(true, exception instanceof MontonioException);
    }
}
