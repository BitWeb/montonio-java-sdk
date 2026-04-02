package ee.bitweb.montonio.sdk;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MontonioSdkConfiguration {

    public static final String SANDBOX_BASE_URL = "https://sandbox-stargate.montonio.com/api";

    private String baseUrl = SANDBOX_BASE_URL;
}
