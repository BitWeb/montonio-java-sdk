package ee.bitweb.montonio.sdk.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class MontonioAuthenticationExceptionTest {

    @Test
    void constructWithMessage() {
        MontonioAuthenticationException exception = new MontonioAuthenticationException("Secret key is not configured");

        assertEquals("Secret key is not configured", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void constructWithMessageAndCause() {
        Throwable cause = new RuntimeException("JWT parse error");

        MontonioAuthenticationException exception = new MontonioAuthenticationException("JWT signature verification failed", cause);

        assertEquals("JWT signature verification failed", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void isMontonioException() {
        MontonioAuthenticationException exception = new MontonioAuthenticationException("test");

        assertEquals(true, exception instanceof MontonioException);
    }
}
