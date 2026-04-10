package ee.bitweb.montonio.sdk.exception;

import lombok.Getter;

@Getter
public final class MontonioValidationException extends MontonioException {

    private final String field;

    public MontonioValidationException(String field, String message) {
        this(field, message, null);
    }

    public MontonioValidationException(String message) {
        this(null, message, null);
    }

    public MontonioValidationException(String field, String message, Throwable cause) {
        super(formatMessage(field, message), cause);
        this.field = field;
    }

    public MontonioValidationException(String message, Throwable cause) {
        this(null, message, cause);
    }

    private static String formatMessage(String field, String message) {
        if (field == null) {
            return "Validation failed: " + message;
        }
        return "Validation failed on field '" + field + "': " + message;
    }
}
