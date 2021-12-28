package net.techcable.minitoml.errors;

import net.techcable.minitoml.TomlLocation;
import org.jetbrains.annotations.Nullable;

/**
 * If a value overflows its intended representation.
 *
 * This mainly happens for integer overflow.
 */
public class TomlOverflowException extends TomlException {
    public TomlOverflowException(String message, @Nullable TomlLocation location) {
        super(message, location);
    }
}
