package net.techcable.minitoml.errors;

import net.techcable.minitoml.TomlLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * An error that occurs parsing a toml file.
 */
public class TomlException extends RuntimeException {
    @Nullable
    private final TomlLocation location;

    public TomlException(String message, @Nullable TomlLocation location) {
        super(Objects.requireNonNull(message));
        this.location = location;
    }
    public TomlException(String message, Exception cause, @Nullable TomlLocation location) {
        super(Objects.requireNonNull(message), cause);
        this.location = location;
    }


    /**
     * Returns the location that the error occurred at.
     *
     * Returns null if the location is unknown.
     */
    @Nullable
    public TomlLocation getLocation() {
        return location;
    }

    @Override
    public String toString() {
        if (location == null) {
            return getMessage();
        } else {
            return getMessage() + " at " + location;
        }
    }
}
