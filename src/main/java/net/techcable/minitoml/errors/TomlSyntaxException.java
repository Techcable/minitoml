package net.techcable.minitoml.errors;

import net.techcable.minitoml.TomlLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Indicates a syntax error occured while parsing a toml file.
 *
 * This always has an associated {@link TomlLocation}
 */
public class TomlSyntaxException extends TomlException {

    public TomlSyntaxException(String message, TomlLocation location) {
        super(message, Objects.requireNonNull(location, "Location is required"));
    }


    @Override
    @NotNull
    public TomlLocation getLocation() {
        return Objects.requireNonNull(super.getLocation());
    }
}
