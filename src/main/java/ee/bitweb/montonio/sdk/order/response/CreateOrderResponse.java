package ee.bitweb.montonio.sdk.order.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public final class CreateOrderResponse {

    private final String uuid;
    private final String paymentUrl;

    @JsonCreator
    public CreateOrderResponse(String uuid, String paymentUrl) {
        this.uuid = uuid;
        this.paymentUrl = paymentUrl;
    }
}
