package ee.bitweb.montonio.sdk.exception;

import lombok.Getter;

@Getter
public final class MontonioValidationException extends MontonioException {

    private final String field;

    public MontonioValidationException(String field, String message) {
        super(formatMessage(field, message));
        this.field = field;
    }

    public MontonioValidationException(String message) {
        super("Validation failed: " + message);
        this.field = null;
    }

    private static String formatMessage(String field, String message) {
        return "Validation failed on field '" + field + "': " + message;
    }
}
