package ee.bitweb.montonio.sdk.exception;

import java.net.SocketTimeoutException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class MontonioNetworkExceptionTest {

    @Test
    void constructWithMessageAndCause() {
        Throwable cause = new SocketTimeoutException("Connection timed out");

        MontonioNetworkException exception = new MontonioNetworkException("Failed to connect to Montonio API", cause);

        assertEquals("Failed to connect to Montonio API", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void causeIsAccessible() {
        SocketTimeoutException cause = new SocketTimeoutException("Read timed out");

        MontonioNetworkException exception = new MontonioNetworkException("Request timed out", cause);

        assertEquals(true, exception.getCause() instanceof SocketTimeoutException);
    }

    @Test
    void isMontonioException() {
        MontonioNetworkException exception = new MontonioNetworkException("error", new RuntimeException());

        assertEquals(true, exception instanceof MontonioException);
    }
}
