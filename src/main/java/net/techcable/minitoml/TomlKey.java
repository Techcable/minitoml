package net.techcable.minitoml;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An immutable key in a toml table.
 */
public final class TomlKey {
    @Nullable
    private final TomlLocation location;
    private final String[] parts;
    private TomlKey(String[] parts, TomlLocation location) {
        this.parts = Objects.requireNonNull(parts);
        this.location = location;
        if (parts.length == 0) throw new IllegalArgumentException("Key may not be empty");
    }

    /**
     * Return the first part in the key.
     *
     * This will always succeed, because a key cannot be empty.
     */
    @NotNull
    public String first() {
        return this.parts[0];
    }

    /**
     * Return the last part of the key.
     *
     * This will always succeed because a key cannot be empty.
     */
    @NotNull
    public String last() {
        return this.parts[this.parts.length - 1];
    }

    /**
     * Get the part at the specified index.
     *
     * @param index the index to get
     * @throws IndexOutOfBoundsException if the index is invalid
     * @return the part at the specified inde
     */
    @NotNull
    public String getPart(int index) {
        return this.parts[index];
    }

    /**
     * Return a list of this key's parts.
     */
    public List<String> parts() {
        return List.of(parts);
    }

    /**
     * Copy this key, changing to the specified location.
     *
     * Does not affect the parts.
     *
     * @param location the new location
     * @return a new key, with the same parts and the new location
     */
    public TomlKey withLocation(TomlLocation location) {
        return new TomlKey(this.parts, location);
    }

    /**
     * Create a new key with only a single element
     *
     * @param key the key to create
     * @return a new key
     */
    public static TomlKey of(String key) {
        return new TomlKey(new String[]{key},null);
    }

    /**
     * Create a new key from the specified parts
     * @param first the first part of the key
     * @param other the other parts of the key
     * @return a new key
     */
    public static TomlKey of(String first, String... other) {
        return TomlKey.builder().add(first).addAll(other).build();
    }

    /**
     * Create a new key builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<String> parts = new ArrayList<>(1);
        private TomlLocation location = null;

        public Builder withLocation(TomlLocation location) {
            this.location = location;
            return this;
        }

        public Builder addAll(String... other) {
            for (String s : other) {
                this.add(s);
            }
            return this;
        }

        public Builder add(String key) {
            this.parts.add(Objects.requireNonNull(key));
            return this;
        }

        public int currentLength() {
            return this.parts.size();
        }

        public TomlKey build() {
            return new TomlKey(this.parts.toArray(new String[parts.size()]), this.location);
        }
    }
}
