package ee.bitweb.montonio.sdk.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Locale {

    DE("de"),
    EN("en"),
    ET("et"),
    FI("fi"),
    LT("lt"),
    LV("lv"),
    PL("pl"),
    RU("ru");

    private final String value;

    Locale(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
