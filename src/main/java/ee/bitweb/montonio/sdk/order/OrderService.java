package ee.bitweb.montonio.sdk.order;

import ee.bitweb.montonio.sdk.exception.MontonioValidationException;
import ee.bitweb.montonio.sdk.http.MontonioHttpClient;
import ee.bitweb.montonio.sdk.order.response.OrderResponse;

public class OrderService {

    private final MontonioHttpClient httpClient;

    public OrderService(MontonioHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public OrderResponse get(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new MontonioValidationException("uuid", "must not be null or blank");
        }
        return httpClient.get("/orders/" + uuid, OrderResponse.class);
    }
}
