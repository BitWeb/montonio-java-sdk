package ee.bitweb.montonio.sdk.order.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import ee.bitweb.montonio.sdk.model.Currency;
import ee.bitweb.montonio.sdk.model.Locale;
import ee.bitweb.montonio.sdk.model.PaymentMethodType;
import ee.bitweb.montonio.sdk.order.model.Address;
import ee.bitweb.montonio.sdk.order.model.LineItem;
import ee.bitweb.montonio.sdk.order.model.PaymentStatus;
import jakarta.annotation.Nullable;
import lombok.Getter;

import java.util.List;

@Getter
public final class OrderResponse {

    private final String uuid;
    private final PaymentStatus paymentStatus;
    private final Locale locale;
    private final String merchantReference;
    private final String merchantReferenceDisplay;
    private final String merchantReturnUrl;
    private final String merchantNotificationUrl;
    private final String grandTotal;
    private final Currency currency;
    private final PaymentMethodType paymentMethodType;
    private final String storeUuid;
    private final List<PaymentIntent> paymentIntents;
    private final List<LineItem> lineItems;
    private final Address billingAddress;
    private final Address shippingAddress;
    private final String expiresAt;
    private final String createdAt;
    private final String storeName;
    private final String businessName;
    private final String paymentUrl;

    @Nullable
    private final Boolean isRefundableType;

    @JsonCreator
    public OrderResponse(
            String uuid,
            PaymentStatus paymentStatus,
            Locale locale,
            String merchantReference,
            String merchantReferenceDisplay,
            String merchantReturnUrl,
            String merchantNotificationUrl,
            String grandTotal,
            Currency currency,
            PaymentMethodType paymentMethodType,
            String storeUuid,
            List<PaymentIntent> paymentIntents,
            List<LineItem> lineItems,
            Address billingAddress,
            Address shippingAddress,
            String expiresAt,
            String createdAt,
            String storeName,
            String businessName,
            String paymentUrl,
            Boolean isRefundableType
    ) {
        this.uuid = uuid;
        this.paymentStatus = paymentStatus;
        this.locale = locale;
        this.merchantReference = merchantReference;
        this.merchantReferenceDisplay = merchantReferenceDisplay;
        this.merchantReturnUrl = merchantReturnUrl;
        this.merchantNotificationUrl = merchantNotificationUrl;
        this.grandTotal = grandTotal;
        this.currency = currency;
        this.paymentMethodType = paymentMethodType;
        this.storeUuid = storeUuid;
        this.paymentIntents = paymentIntents;
        this.lineItems = lineItems;
        this.billingAddress = billingAddress;
        this.shippingAddress = shippingAddress;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.storeName = storeName;
        this.businessName = businessName;
        this.paymentUrl = paymentUrl;
        this.isRefundableType = isRefundableType;
    }
}
