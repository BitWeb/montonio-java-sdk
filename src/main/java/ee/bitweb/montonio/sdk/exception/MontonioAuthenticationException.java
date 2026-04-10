package ee.bitweb.montonio.sdk.exception;

public final class MontonioAuthenticationException extends MontonioException {

    public MontonioAuthenticationException(String message) {
        super(message);
    }

    public MontonioAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
