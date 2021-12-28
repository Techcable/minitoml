package net.techcable.minitoml;

import net.techcable.minitoml.errors.TomlOverflowException;
import net.techcable.minitoml.errors.TomlUnexpectedTypeException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.temporal.Temporal;
import java.util.Objects;

/**
 * A toml primitive value.
 *
 * This corresponds to either a Java primitive type or a string.
 *
 * Dates and times are handled by the {@link TomlDate} subclass.
 */
public class TomlPrimitive extends TomlValue {
    /**
     * The dynamically typed primitive value.
     *
     * It is one of the following types:
     * - String
     * - Boolean
     * - Number (see below)
     * - Temporal (only allowed with the TomlDate subclass)
     *
     * If it is a Number, then it must use the simplest possible representation.
     */
    @NotNull
    protected final Object value;
    /**
     * Create a toml primitive value from the specified string.
     */
    public TomlPrimitive(String value, TomlLocation location) {
        super(TomlType.STRING, location);
        this.value = Objects.requireNonNull(value);
    }

    /**
     * Create
     *
     * For integers, the number must use the smallest possible representation that it can.
     * So never use a `Long` when an `Integer` will do. Likewise for `BigDecimal`.
     *
     * This simplifies overflow checking.
     *
     * For decimals, whether it uses a {@link BigDecimal} or a double depends on the parser options.
     *
     * @param value the numeric value
     * @param location
     */
    private TomlPrimitive(TomlType tomlType, Number value, TomlLocation location) {
        super(tomlType, location);
        assert tomlType.isNumber();
        this.value = Objects.requireNonNull(value);
    }

    protected TomlPrimitive(Temporal rawObject, TomlLocation location) {
        super(TomlType.DATE, location);
        this.value = Objects.requireNonNull(rawObject);
        assert this instanceof TomlDate;
    }

    /**
     * Create a primitive from the specified integer, along with the specified location.
     * @param value the integer value
     * @return a primitive value
     */
    public static TomlPrimitive fromInt(int value, @Nullable TomlLocation location) {
        return new TomlPrimitive(TomlType.INTEGER, value, location);
    }


    /**
     * Create a primitive from the specified long, along with the specified location.
     *
     * @param value the long value
     * @return a primitive value
     */
    public static TomlPrimitive fromLong(long value, @Nullable TomlLocation location) {
        return new TomlPrimitive(TomlType.INTEGER, value, location);
    }

    /**
     * Create a primitive from the specified double, along with the specified location.
     * @param d the double value
     * @param location the location
     * @return a primitive value
     */
    public static TomlPrimitive fromDouble(double d, @Nullable TomlLocation location) {
        return new TomlPrimitive(TomlType.DECIMAL_NUMBER, d, location);
    }

    @Override
    public String asString() {
        this.expectType(TomlType.STRING);
        return (String) this.value;
    }

    @Override
    public int asInt() {
        this.expectType(TomlType.INTEGER);
        if (value instanceof Integer) {
            return (Integer) value;
        } else {
            throw this.handleNumericOverflow(int.class);
        }
    }

    @Contract("_ -> fail")
    private TomlOverflowException handleNumericOverflow(Class<? extends Number> expectedType) {
        final int bits;
        final boolean includeValue;
        if (value instanceof BigInteger) {
            // add one bit for sign
            bits = ((BigInteger) value).bitLength() + 1;
            includeValue = false; // Might be ginormous
        } else if (value instanceof Long) {
            bits = MathUtil.countEffectiveBits((Long) value);
            includeValue = true; // At most 20 characters
        } else {
            // An `int` should be able to fit into anything
            throw new AssertionError(value.getClass().getName() + " for " + expectedType);
        }
        String msg = "Cannot fit a " + bits + " bit integer into a " + expectedType.getSimpleName();
        if (includeValue) {
            msg += ": " + value;
        }
        throw new TomlOverflowException(msg, this.getLocation());
    }

    @Override
    public long asLong() {
        this.expectType(TomlType.INTEGER);
        if (this.value instanceof Integer) {
            return (Integer) this.value;
        } else if (this.value instanceof Long) {
            return (Long) this.value;
        } else {
            throw this.handleNumericOverflow(long.class);
        }
    }

    @Override
    public BigInteger asBigInt() {
        this.expectType(TomlType.INTEGER);
        if (this.value instanceof Long || this.value instanceof Integer) {
            return BigInteger.valueOf(((Number) this.value).intValue());
        } else {
            return (BigInteger) this.value;
        }
    }

    @Override
    public double asDouble() {
        switch (getType()) {
            case DECIMAL_NUMBER -> {
                if (this.value instanceof Double) {
                    return (Double) this.value;
                } else {
                    return ((BigInteger) this.value).doubleValue();
                }
            }
            case INTEGER -> {
                // Handle overflow
                if (this.value instanceof Integer) {
                    // Cannot possibly overflow
                    return ((Integer) this.value);
                } else if (this.value instanceof Long) {
                    if (MathUtil.canFitExactlyInDouble((Long) this.value)) {
                        return (double) (Long) this.value;
                    }
                }
                // fallback
                throw this.handleNumericOverflow(double.class);
            }
            default -> {
                throw new TomlUnexpectedTypeException("number", getType(), this.getLocation());
            }
        }
    }

    @Override
    public BigDecimal asBigDecimal() {
        return super.asBigDecimal();
    }

    @Override
    protected void toJson(StringBuilder builder) {
        switch (getType()) {
            case TABLE, ARRAY, DATE -> throw new IllegalStateException();
            case STRING -> {
                EscapeUtil.writeJsonString((String) this.value, builder);
            }
            case INTEGER, DECIMAL_NUMBER, BOOLEAN -> {
                builder.append(this.value);
            }
        }
    }

    /**
     * Convert this value into a string.
     *
     * For primitive types (except for strings),
     * this should be able to successfully round trip.
     *
     * @return this value as a string
     */
    @Override
    public String toString() {
        return this.value.toString();
    }
}
