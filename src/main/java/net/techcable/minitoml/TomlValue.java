package net.techcable.minitoml;

import net.techcable.minitoml.errors.TomlUnexpectedTypeException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public abstract class TomlValue {
    @Nullable
    private final TomlLocation location;
    @NotNull
    private final TomlType type;

    /* package */ TomlValue(TomlType type, TomlLocation location) {
        this.type = Objects.requireNonNull(type);
        this.location = location;
    }

    /**
     * Get the location this value starts at.
     *
     * If this is null, then the location is unknown.
     *
     * @return the location.
     */
    @Nullable
    public TomlLocation getLocation() {
        return location;
    }

    /**
     * Return the type of this value.
     *
     * @return the type
     */
    @NotNull
    public TomlType getType() {
        return this.type;
    }

    /**
     * Verify that the value has the specified type.
     *
     * @throws TomlUnexpectedTypeException if the type doesn't match
     * @param expectedType the type to expect
     * @return the original object
     */
    @Contract("_ -> this")
    public TomlValue expectType(TomlType expectedType) {
        if (getType() != expectedType) {
            throw new TomlUnexpectedTypeException(expectedType, getType(), this.location);
        } else {
            return this;
        }
    }

    //
    // Casting functions
    //

    /**
     * Convert this value to a {@link TomlPrimitive} throwing an appropriate error if it is not.
     *
     * @throws TomlUnexpectedTypeException if the type isn't a primitive
     * @return this value, cast into a primitive
     */
    public TomlPrimitive asPrimitive() {
        if (this instanceof TomlPrimitive) {
            return (TomlPrimitive) this;
        } else {
            throw new TomlUnexpectedTypeException("primitive", getType(), this.location);
        }
    }

    /**
     * Convert this value to a string, throwing an appropriate error if it is not one.
     *
     * @throws TomlUnexpectedTypeException if the type isn't actually a string
     * @return this value, cast into a string
     */
    public String asString() {
        this.expectType(TomlType.STRING);
        return this.asPrimitive().asString();
    }

    /**
     * Convert this value into an integer, throwing an appropriate error if it isn't one (or if it's too large).
     *
     * @throws TomlUnexpectedTypeException if the type isn't actually an integer
     * @throws net.techcable.minitoml.errors.TomlOverflowException if the value is too large to fit into an 32 bit `int`
     * @return this value, cast into an integer
     */
    public int asInt() {
        this.expectType(TomlType.INTEGER);
        return this.asPrimitive().asInt();
    }

    /**
     * Convert this value into a long, throwing an appropriate error if it isn't an integer (or if it overflows)
     *
     * @throws TomlUnexpectedTypeException if the type isn't actually an integer
     * @throws net.techcable.minitoml.errors.TomlOverflowException if the value is too large to fit into an 32 bit `int`
     * @return this value, cast into a long
     */
    public long asLong() {
        this.expectType(TomlType.INTEGER);
        return this.asPrimitive().asLong();
    }

    /**
     * Convert this value into an arbitrary precision integer, throwing an appropriate error if it isn't an integer.
     *
     * @throws TomlUnexpectedTypeException if the type isn't actually an integer
     * @return this value, cast into an arbitrary precision integer.
     */
    public BigInteger asBigInt() {
        this.expectType(TomlType.INTEGER);
        return this.asPrimitive().asBigInt();
    }

    /**
     * Convert this value into a double, throwing an error if it isn't a number.
     *
     * This implicitly converts from integers to doubles, checking for overflow in the process.
     * If the integer is too large to fit into an `double`, it will throw an {@link net.techcable.minitoml.errors.TomlOverflowException}
     *
     * However, with floating point numbers, this implicitly approximates.
     * For example, there is no way to accurately represent `.3` as a double.
     * This is why {@code .3-.2 !=.1}.
     *
     * Approximation is inherent to the nature of floating point numbers (but not integers).
     *
     * If you want to avoid approximation (both floating and ),
     * use arbitrary precision arithmetic and {@link #asBigDecimal()}.
     *
     * @throws TomlUnexpectedTypeException if the type isn't a number
     * @throws net.techcable.minitoml.errors.TomlOverflowException if the type is an integer, and it overflows a `double`
     * @return this value, approximated as a double
     */
    public double asDouble() {
        return switch (getType()) {
            case DECIMAL_NUMBER, INTEGER -> asPrimitive().asDouble();
            default -> throw new TomlUnexpectedTypeException("number", getType(), this.location);
        };
    }

    /**
     * Convert this value into an arbitrary precision decimal, throwing an error if it isn't a number.
     *
     * Implicitly converts from integers to doubles.
     *
     * Unlike {@link #asDouble()}, this should exactly represent any possible number.
     * It will never approximate.
     *
     * @throws TomlUnexpectedTypeException if the type is not a number
     * @return this number, as an arbitrary precision double
     */
    public BigDecimal asBigDecimal() {
        return switch (getType()) {
            case DECIMAL_NUMBER, INTEGER -> asPrimitive().asBigDecimal();
            default -> throw new TomlUnexpectedTypeException("number", getType(), this.location);
        };
    }

    /**
     * Convert this type to a json-formatted string.
     *
     * @return the equivalent json object, as a string
     */
    public final String toJsonString(boolean pretty) {
        StringBuilder builder = new StringBuilder();
        this.toJson(builder);
        return builder.toString();
    }
    protected abstract void toJson(StringBuilder builder);

    /**
     * Return a string representation of this type.
     *
     * This is intended for user display,
     *
     * This will not necessarily round trip.
     *
     * @return a string representation of this type
     */
    @Override
    public abstract String toString();
}
