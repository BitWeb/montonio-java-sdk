package ee.bitweb.montonio.sdk.exception;

public class MontonioException extends RuntimeException {

    public MontonioException(String message) {
        super(message);
    }

    public MontonioException(String message, Throwable cause) {
        super(message, cause);
    }
}
