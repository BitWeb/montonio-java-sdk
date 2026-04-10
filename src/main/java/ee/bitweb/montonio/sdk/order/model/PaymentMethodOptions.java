package ee.bitweb.montonio.sdk.order.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import ee.bitweb.montonio.sdk.model.CardPaymentMethod;
import ee.bitweb.montonio.sdk.model.Locale;
import ee.bitweb.montonio.sdk.model.WalletProvider;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentMethodOptions {

    @Nullable
    private final String preferredProvider;

    @Nullable
    private final String preferredCountry;

    @Nullable
    private final Locale preferredLocale;

    @Nullable
    private final CardPaymentMethod preferredMethod;

    @Nullable
    private final WalletProvider preferredWallet;

    @Nullable
    private final String paymentDescription;

    @Nullable
    private final String paymentReference;

    @Nullable
    private final Integer period;

    @JsonCreator
    PaymentMethodOptions(
            String preferredProvider,
            String preferredCountry,
            Locale preferredLocale,
            CardPaymentMethod preferredMethod,
            WalletProvider preferredWallet,
            String paymentDescription,
            String paymentReference,
            Integer period
    ) {
        this.preferredProvider = preferredProvider;
        this.preferredCountry = preferredCountry;
        this.preferredLocale = preferredLocale;
        this.preferredMethod = preferredMethod;
        this.preferredWallet = preferredWallet;
        this.paymentDescription = paymentDescription;
        this.paymentReference = paymentReference;
        this.period = period;
    }
}
