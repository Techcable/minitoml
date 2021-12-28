package net.techcable.minitoml.errors;

import net.techcable.minitoml.TomlLocation;
import net.techcable.minitoml.TomlType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Indicates that a value had an unexpected type.
 */
public class TomlUnexpectedTypeException extends TomlException {
    /**
     * A succinct description of the type that was expected.
     */
    public final String expectedType;
    /**
     * The actual type of the value.
     */
    public final TomlType actualType;
    public TomlUnexpectedTypeException(TomlType expectedType, TomlType actualType, @Nullable TomlLocation location) {
        this(expectedType.toString(), actualType, location);
    }
    public TomlUnexpectedTypeException(String expectedType, TomlType actualType, @Nullable TomlLocation location) {
        super("Expected a " + expectedType + ", but got a " + actualType, location);
        this.expectedType = Objects.requireNonNull(expectedType);
        this.actualType = Objects.requireNonNull(actualType);
    }
}
