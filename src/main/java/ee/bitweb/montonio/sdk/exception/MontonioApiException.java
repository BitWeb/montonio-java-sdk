package ee.bitweb.montonio.sdk.exception;

import lombok.Getter;

@Getter
public final class MontonioApiException extends MontonioException {

    private final int statusCode;
    private final String errorCode;
    private final String errorMessage;

    public MontonioApiException(int statusCode, String errorCode, String errorMessage) {
        super(formatMessage(statusCode, errorCode, errorMessage));
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    private static String formatMessage(int statusCode, String errorCode, String errorMessage) {
        StringBuilder sb = new StringBuilder("Montonio API error (HTTP ").append(statusCode).append(")");

        if (errorCode != null || errorMessage != null) {
            sb.append(": ");
        }

        if (errorCode != null) {
            sb.append("[").append(errorCode).append("]");
            if (errorMessage != null) {
                sb.append(" ");
            }
        }

        if (errorMessage != null) {
            sb.append(errorMessage);
        }

        return sb.toString();
    }
}
