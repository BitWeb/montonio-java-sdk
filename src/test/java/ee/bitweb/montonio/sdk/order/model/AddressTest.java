package ee.bitweb.montonio.sdk.order.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AddressTest {

    private final ObjectMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @Test
    void buildWithAllFields() {
        Address address = Address.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("123456789")
                .phoneCountry("EE")
                .addressLine1("Kai 1")
                .addressLine2("Apt 2")
                .locality("Tallinn")
                .region("Harjumaa")
                .postalCode("10111")
                .country("EE")
                .companyName("Acme")
                .companyLegalName("Acme OÜ")
                .companyRegCode("12345678")
                .companyVatNumber("EE123456789")
                .build();

        assertEquals("John", address.getFirstName());
        assertEquals("Doe", address.getLastName());
        assertEquals("john@example.com", address.getEmail());
        assertEquals("123456789", address.getPhoneNumber());
        assertEquals("EE", address.getPhoneCountry());
        assertEquals("Kai 1", address.getAddressLine1());
        assertEquals("Apt 2", address.getAddressLine2());
        assertEquals("Tallinn", address.getLocality());
        assertEquals("Harjumaa", address.getRegion());
        assertEquals("10111", address.getPostalCode());
        assertEquals("EE", address.getCountry());
        assertEquals("Acme", address.getCompanyName());
        assertEquals("Acme OÜ", address.getCompanyLegalName());
        assertEquals("12345678", address.getCompanyRegCode());
        assertEquals("EE123456789", address.getCompanyVatNumber());
    }

    @Test
    void buildWithNoFieldsDefaultsToNull() {
        Address address = Address.builder().build();

        assertNull(address.getFirstName());
        assertNull(address.getLastName());
        assertNull(address.getEmail());
        assertNull(address.getPhoneNumber());
        assertNull(address.getPhoneCountry());
        assertNull(address.getAddressLine1());
        assertNull(address.getAddressLine2());
        assertNull(address.getLocality());
        assertNull(address.getRegion());
        assertNull(address.getPostalCode());
        assertNull(address.getCountry());
        assertNull(address.getCompanyName());
        assertNull(address.getCompanyLegalName());
        assertNull(address.getCompanyRegCode());
        assertNull(address.getCompanyVatNumber());
    }

    @Test
    void serializationRoundTrip() throws Exception {
        Address address = Address.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .addressLine1("Kai 1")
                .locality("Tallinn")
                .region("Harjumaa")
                .postalCode("10111")
                .country("EE")
                .build();

        String json = mapper.writeValueAsString(address);
        Address deserialized = mapper.readValue(json, Address.class);

        assertEquals(address.getFirstName(), deserialized.getFirstName());
        assertEquals(address.getLastName(), deserialized.getLastName());
        assertEquals(address.getEmail(), deserialized.getEmail());
        assertEquals(address.getAddressLine1(), deserialized.getAddressLine1());
        assertEquals(address.getLocality(), deserialized.getLocality());
        assertEquals(address.getRegion(), deserialized.getRegion());
        assertEquals(address.getPostalCode(), deserialized.getPostalCode());
        assertEquals(address.getCountry(), deserialized.getCountry());
    }

    @Test
    void deserializesWithUnknownFieldsIgnored() throws Exception {
        String json = """
                {"firstName":"John","unknownField":"value"}
                """;

        Address address = mapper.readValue(json, Address.class);

        assertEquals("John", address.getFirstName());
    }
}
