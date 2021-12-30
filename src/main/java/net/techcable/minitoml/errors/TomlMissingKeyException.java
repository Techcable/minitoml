package net.techcable.minitoml.errors;

import net.techcable.minitoml.TomlKey;
import net.techcable.minitoml.TomlLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TomlMissingKeyException extends TomlException {
    private final TomlKey missingKey;
    public TomlMissingKeyException(TomlKey missingKey, @Nullable TomlLocation location) {
        super("Missing required key: " + missingKey, location);
        this.missingKey = Objects.requireNonNull(missingKey);
    }

    public TomlKey getMissingKey() {
        return missingKey;
    }
}
