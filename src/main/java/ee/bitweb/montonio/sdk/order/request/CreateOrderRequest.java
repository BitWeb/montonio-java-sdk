package ee.bitweb.montonio.sdk.order.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import ee.bitweb.montonio.sdk.exception.MontonioValidationException;
import ee.bitweb.montonio.sdk.model.Currency;
import ee.bitweb.montonio.sdk.model.Locale;
import ee.bitweb.montonio.sdk.order.model.Address;
import ee.bitweb.montonio.sdk.order.model.LineItem;
import ee.bitweb.montonio.sdk.order.model.Payment;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CreateOrderRequest {

    private final String merchantReference;
    private final String returnUrl;
    private final String notificationUrl;
    private final BigDecimal grandTotal;
    private final Currency currency;
    private final Payment payment;

    @Nullable
    private final Locale locale;

    @Nullable
    private final Address billingAddress;

    @Nullable
    private final Address shippingAddress;

    @Nullable
    private final List<LineItem> lineItems;

    @JsonCreator
    CreateOrderRequest(
            String merchantReference,
            String returnUrl,
            String notificationUrl,
            BigDecimal grandTotal,
            Currency currency,
            Payment payment,
            Locale locale,
            Address billingAddress,
            Address shippingAddress,
            List<LineItem> lineItems
    ) {
        if (merchantReference == null || merchantReference.isBlank()) {
            throw new MontonioValidationException("merchantReference", "must not be null or blank");
        }
        if (returnUrl == null || returnUrl.isBlank()) {
            throw new MontonioValidationException("returnUrl", "must not be null or blank");
        }
        if (notificationUrl == null || notificationUrl.isBlank()) {
            throw new MontonioValidationException("notificationUrl", "must not be null or blank");
        }
        if (grandTotal == null) {
            throw new MontonioValidationException("grandTotal", "must not be null");
        }
        if (currency == null) {
            throw new MontonioValidationException("currency", "must not be null");
        }
        if (payment == null) {
            throw new MontonioValidationException("payment", "must not be null");
        }
        this.merchantReference = merchantReference;
        this.returnUrl = returnUrl;
        this.notificationUrl = notificationUrl;
        this.grandTotal = grandTotal;
        this.currency = currency;
        this.payment = payment;
        this.locale = locale;
        this.billingAddress = billingAddress;
        this.shippingAddress = shippingAddress;
        this.lineItems = lineItems;
    }
}
