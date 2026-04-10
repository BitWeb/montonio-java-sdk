package ee.bitweb.montonio.sdk.order.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import ee.bitweb.montonio.sdk.exception.MontonioValidationException;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class LineItem {

    private final String name;
    private final BigDecimal quantity;
    private final BigDecimal finalPrice;

    @JsonCreator
    LineItem(String name, BigDecimal quantity, BigDecimal finalPrice) {
        if (name == null || name.isBlank()) {
            throw new MontonioValidationException("name", "must not be null or blank");
        }
        if (quantity == null) {
            throw new MontonioValidationException("quantity", "must not be null");
        }
        if (finalPrice == null) {
            throw new MontonioValidationException("finalPrice", "must not be null");
        }
        this.name = name;
        this.quantity = quantity;
        this.finalPrice = finalPrice;
    }
}
