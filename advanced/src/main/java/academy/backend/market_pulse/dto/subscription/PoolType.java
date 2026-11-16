package academy.backend.market_pulse.dto.subscription;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public enum PoolType {
    QUEUE,
    TREIBER;

    public static PoolType parse(String value) {
        for (PoolType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }

        throw new IllegalArgumentException("Неизвестный тип пула: " + value + "; допустимые значения: "
                + Arrays.stream(values())
                .map(type -> type.name().toLowerCase(Locale.ROOT))
                .collect(Collectors.joining("|")));
    }
}
