package ee.bitweb.montonio.sdk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MontonioSdkConfigurationTest {

    @Test
    void defaultBaseUrlIsSandbox() {
        MontonioSdkConfiguration configuration = new MontonioSdkConfiguration();

        assertEquals("https://sandbox-stargate.montonio.com/api", configuration.getBaseUrl());
    }
}
