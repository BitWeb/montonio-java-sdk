package ee.bitweb.montonio.sdk.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocaleTest {

    private final ObjectMapper mapper = new JsonMapper();

    @Test
    void enumContainsExpectedValues() {
        assertEquals(8, Locale.values().length);
    }

    @Test
    void wireValuesAreLowercase() {
        assertEquals("de", Locale.DE.getValue());
        assertEquals("en", Locale.EN.getValue());
        assertEquals("et", Locale.ET.getValue());
        assertEquals("fi", Locale.FI.getValue());
        assertEquals("lt", Locale.LT.getValue());
        assertEquals("lv", Locale.LV.getValue());
        assertEquals("pl", Locale.PL.getValue());
        assertEquals("ru", Locale.RU.getValue());
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    void serializesToWireValue(Locale locale) throws Exception {
        String json = mapper.writeValueAsString(locale);
        assertEquals("\"" + locale.getValue() + "\"", json);
    }

    @ParameterizedTest
    @EnumSource(Locale.class)
    void deserializesFromWireValue(Locale locale) throws Exception {
        String json = "\"" + locale.getValue() + "\"";
        Locale result = mapper.readValue(json, Locale.class);
        assertEquals(locale, result);
    }
}
