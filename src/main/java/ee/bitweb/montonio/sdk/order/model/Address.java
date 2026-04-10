package ee.bitweb.montonio.sdk.order.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Address {

    @Nullable
    private final String firstName;

    @Nullable
    private final String lastName;

    @Nullable
    private final String email;

    @Nullable
    private final String phoneNumber;

    @Nullable
    private final String phoneCountry;

    @Nullable
    private final String addressLine1;

    @Nullable
    private final String addressLine2;

    @Nullable
    private final String locality;

    @Nullable
    private final String region;

    @Nullable
    private final String postalCode;

    @Nullable
    private final String country;

    @Nullable
    private final String companyName;

    @Nullable
    private final String companyLegalName;

    @Nullable
    private final String companyRegCode;

    @Nullable
    private final String companyVatNumber;

    @JsonCreator
    Address(
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String phoneCountry,
            String addressLine1,
            String addressLine2,
            String locality,
            String region,
            String postalCode,
            String country,
            String companyName,
            String companyLegalName,
            String companyRegCode,
            String companyVatNumber
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.phoneCountry = phoneCountry;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.locality = locality;
        this.region = region;
        this.postalCode = postalCode;
        this.country = country;
        this.companyName = companyName;
        this.companyLegalName = companyLegalName;
        this.companyRegCode = companyRegCode;
        this.companyVatNumber = companyVatNumber;
    }
}
