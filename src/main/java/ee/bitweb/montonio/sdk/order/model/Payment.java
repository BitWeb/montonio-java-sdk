package ee.bitweb.montonio.sdk.order.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import ee.bitweb.montonio.sdk.exception.MontonioValidationException;
import ee.bitweb.montonio.sdk.model.Currency;
import ee.bitweb.montonio.sdk.model.PaymentMethodType;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class Payment {

    private final PaymentMethodType method;
    private final Currency currency;
    private final BigDecimal amount;

    @Nullable
    private final PaymentMethodOptions methodOptions;

    @Nullable
    private final String methodDisplay;

    @JsonCreator
    Payment(
            PaymentMethodType method,
            Currency currency,
            BigDecimal amount,
            PaymentMethodOptions methodOptions,
            String methodDisplay
    ) {
        if (method == null) {
            throw new MontonioValidationException("method", "must not be null");
        }
        if (currency == null) {
            throw new MontonioValidationException("currency", "must not be null");
        }
        if (amount == null) {
            throw new MontonioValidationException("amount", "must not be null");
        }
        if (amount.signum() <= 0) {
            throw new MontonioValidationException("amount", "must be greater than zero");
        }
        this.method = method;
        this.currency = currency;
        this.amount = amount;
        this.methodOptions = methodOptions;
        this.methodDisplay = methodDisplay;
    }
}
