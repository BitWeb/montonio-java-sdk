package ee.bitweb.montonio.sdk;

import ee.bitweb.montonio.sdk.order.OrderService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MontonioClientTest {

    @Test
    void constructorRejectsNullConfiguration() {
        MontonioSdkConfiguration nullConfig = null;
        assertThrows(NullPointerException.class, () -> new MontonioClient(nullConfig));
    }

    @Test
    void ordersReturnsNonNull() {
        MontonioClient client = createClient();

        assertNotNull(client.orders());
    }

    @Test
    void ordersReturnsSameInstanceOnRepeatedCalls() {
        MontonioClient client = createClient();

        OrderService first = client.orders();
        OrderService second = client.orders();

        assertSame(first, second);
    }

    private MontonioClient createClient() {
        MontonioSdkConfiguration configuration = MontonioSdkConfiguration.builder()
                .accessKey("test-access-key")
                .secretKey("test-secret-key-that-is-long-enough")
                .build();
        return new MontonioClient(configuration);
    }
}
