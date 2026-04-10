package ee.bitweb.montonio.sdk.order.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import ee.bitweb.montonio.sdk.model.Currency;
import ee.bitweb.montonio.sdk.model.PaymentMethodType;
import ee.bitweb.montonio.sdk.order.model.PaymentStatus;
import jakarta.annotation.Nullable;
import lombok.Getter;

import java.util.Map;

@Getter
public final class PaymentIntent {

    private final String uuid;
    private final PaymentMethodType paymentMethodType;
    private final String amount;
    private final Currency currency;
    private final PaymentStatus status;
    private final String serviceFee;
    private final Currency serviceFeeCurrency;
    private final String createdAt;

    @Nullable
    private final Map<String, String> paymentMethodMetadata;

    @JsonCreator
    public PaymentIntent(
            String uuid,
            PaymentMethodType paymentMethodType,
            String amount,
            Currency currency,
            PaymentStatus status,
            String serviceFee,
            Currency serviceFeeCurrency,
            String createdAt,
            Map<String, String> paymentMethodMetadata
    ) {
        this.uuid = uuid;
        this.paymentMethodType = paymentMethodType;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.serviceFee = serviceFee;
        this.serviceFeeCurrency = serviceFeeCurrency;
        this.createdAt = createdAt;
        this.paymentMethodMetadata = paymentMethodMetadata == null ? null : Map.copyOf(paymentMethodMetadata);
    }
}
