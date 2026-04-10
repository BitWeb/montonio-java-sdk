package ee.bitweb.montonio.sdk.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class MontonioExceptionTest {

    @Test
    void constructWithMessage() {
        MontonioException exception = new MontonioException("something went wrong");

        assertEquals("something went wrong", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructWithMessageAndCause() {
        Throwable cause = new RuntimeException("root cause");

        MontonioException exception = new MontonioException("something went wrong", cause);

        assertEquals("something went wrong", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void isRuntimeException() {
        MontonioException exception = new MontonioException("test");

        assertEquals(true, exception instanceof RuntimeException);
    }
}
