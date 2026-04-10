package ee.bitweb.montonio.sdk.order.model;

import ee.bitweb.montonio.sdk.exception.MontonioValidationException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LineItemTest {

    private final ObjectMapper mapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @Test
    void buildWithAllFields() {
        LineItem item = LineItem.builder()
                .name("Hoverboard")
                .quantity(new BigDecimal("2"))
                .finalPrice(new BigDecimal("49.99"))
                .build();

        assertEquals("Hoverboard", item.getName());
        assertEquals(new BigDecimal("2"), item.getQuantity());
        assertEquals(new BigDecimal("49.99"), item.getFinalPrice());
    }

    @Test
    void buildWithNullNameThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> LineItem.builder()
                        .quantity(BigDecimal.ONE)
                        .finalPrice(BigDecimal.TEN)
                        .build()
        );

        assertEquals("name", exception.getField());
    }

    @Test
    void buildWithBlankNameThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> LineItem.builder()
                        .name("   ")
                        .quantity(BigDecimal.ONE)
                        .finalPrice(BigDecimal.TEN)
                        .build()
        );

        assertEquals("name", exception.getField());
    }

    @Test
    void buildWithNullQuantityThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> LineItem.builder()
                        .name("Item")
                        .finalPrice(BigDecimal.TEN)
                        .build()
        );

        assertEquals("quantity", exception.getField());
    }

    @Test
    void buildWithNullFinalPriceThrows() {
        MontonioValidationException exception = assertThrows(
                MontonioValidationException.class,
                () -> LineItem.builder()
                        .name("Item")
                        .quantity(BigDecimal.ONE)
                        .build()
        );

        assertEquals("finalPrice", exception.getField());
    }

    @Test
    void serializationRoundTrip() throws Exception {
        LineItem item = LineItem.builder()
                .name("Hoverboard")
                .quantity(new BigDecimal("1"))
                .finalPrice(new BigDecimal("100.00"))
                .build();

        String json = mapper.writeValueAsString(item);
        LineItem deserialized = mapper.readValue(json, LineItem.class);

        assertEquals(item.getName(), deserialized.getName());
        assertEquals(0, item.getQuantity().compareTo(deserialized.getQuantity()));
        assertEquals(0, item.getFinalPrice().compareTo(deserialized.getFinalPrice()));
    }
}
