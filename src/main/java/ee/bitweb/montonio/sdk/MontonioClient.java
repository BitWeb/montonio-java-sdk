package ee.bitweb.montonio.sdk;

import ee.bitweb.montonio.sdk.http.MontonioHttpClient;
import ee.bitweb.montonio.sdk.order.OrderService;

public class MontonioClient {

    private final MontonioHttpClient httpClient;
    private OrderService orderService;

    public MontonioClient(MontonioSdkConfiguration configuration) {
        if (configuration == null) {
            throw new NullPointerException("configuration must not be null");
        }
        this.httpClient = new MontonioHttpClient(configuration);
    }

    MontonioClient(MontonioHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public OrderService orders() {
        if (orderService == null) {
            orderService = new OrderService(httpClient);
        }
        return orderService;
    }
}
