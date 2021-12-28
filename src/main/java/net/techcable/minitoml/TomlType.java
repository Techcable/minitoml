package net.techcable.minitoml;

import java.util.Locale;

/**
 * A builtin toml type.
 */
public enum TomlType {
    /**
     * A toml table.
     *
     * This is the root type at the start of a file.
     */
    TABLE,
    /**
     * A toml string.
     */
    STRING,
    /**
     * A toml array.
     */
    ARRAY,
    /**
     * A toml integer
     */
    INTEGER,
    /**
     * A toml decimal number.
     */
    DECIMAL_NUMBER,
    /**
     * A toml boolean
     */
    BOOLEAN,
    /**
     * A toml date.
     */
    DATE;

    /**
     * Check if this value is a number (either a decimal or an integer).
     *
     * @return if this is a number.
     */
    public boolean isNumber() {
        return switch (this) {
            case DECIMAL_NUMBER, INTEGER -> true;
            default -> false;
        };
    }

    /**
     * Check if this value is a primitive value, as represented by {@link TomlPrimitive}
     * @return true if this is a primitive.
     */
    public boolean isPrimitive() {
        return switch (this) {
            case TABLE, ARRAY -> false;
            case STRING, INTEGER, DECIMAL_NUMBER, BOOLEAN, DATE -> true;
        };
    }
    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    }
}
