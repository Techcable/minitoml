package net.techcable.minitoml.errors;

import net.techcable.minitoml.TomlDate;
import net.techcable.minitoml.TomlLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TomlUnexpectedDateException extends TomlException {
    @NotNull
    private final TomlDate originalDate;

    public TomlDate getOriginalDate() {
        return originalDate;
    }

    public TomlUnexpectedDateException(@NotNull TomlDate originalDate, String reason, @Nullable TomlLocation location) {
        super("Invalid value, " + reason + ": " + originalDate, location);
        this.originalDate = Objects.requireNonNull(originalDate);
    }
}
