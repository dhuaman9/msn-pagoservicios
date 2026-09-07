package pe.financiera.bs.pagoservicios.service_payment.infrastructure.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum IncorporateEventTagEnum {
    STARTED("STARTED"),
    IN_PROGRESS("IN_PROGRESS"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED");

    private final String value;
    private static final Map<String, IncorporateEventTagEnum> CONSTANTS = new HashMap<>();

    static {
        for (IncorporateEventTagEnum c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    IncorporateEventTagEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static IncorporateEventTagEnum fromValue(String value) {
        IncorporateEventTagEnum constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException("Unknown value: " + value);
        }
        return constant;
    }
}
