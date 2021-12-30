package net.techcable.minitoml;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An immutable key in a toml table.
 */
public final class TomlKey {
    @Nullable
    private final TomlLocation location;
    private final List<String> parts;
    private TomlKey(List<String> parts, @Nullable TomlLocation location) {
        this.parts = Objects.requireNonNull(parts);
        this.location = location;
        if (parts.isEmpty()) throw new IllegalArgumentException("Key must have at least one part");
    }

    /**
     * Return the first part in the key.
     *
     * This will always succeed, because a key cannot be empty.
     */
    @NotNull
    public String firstPart() {
        return this.parts.get(0);
    }

    /**
     * Return the last part of the key.
     *
     * This will always succeed because a key cannot be empty.
     */
    @NotNull
    public String lastPart() {
        return this.parts.get(this.parts.size() - 1);
    }

    /**
     * Get the part at the specified index.
     *
     * @param index the index to get
     * @throws IndexOutOfBoundsException if the index is invalid
     * @return the part at the specified index
     */
    @NotNull
    public String getPart(int index) {
        return this.parts.get(index);
    }

    /**
     * Return a list of this key's parts.
     */
    public List<String> parts() {
        return parts;
    }

    @Nullable
    public TomlLocation getLocation() {
        return location;
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
        return new TomlKey(List.of(key),null);
    }

    /**
     * Create a new key from the specified parts.
     *
     * @param parts the parts of the key
     * @throws IllegalArgumentException if the parts are empty
     * @return a new key
     */
    public static TomlKey fromParts(List<String> parts) {
        Objects.requireNonNull(parts);
        if (parts.isEmpty()) throw new IllegalArgumentException("Key must have at least one part");
        return new TomlKey(List.copyOf(parts), null);
    }

    /**
     * Slice a portion of this key's parts.
     *
     * @param fromIndex the start index (inclusive)
     * @param toIndex the end index (inclusive)
     * @throws IndexOutOfBoundsException if the
     * @throws IllegalArgumentException if the resulting key would be empty
     * @return a sliced view of this key
     */
    public TomlKey sliceParts(int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, this.parts.size());
        if (fromIndex == toIndex) throw new IllegalArgumentException("Cannot create empty key (slice " + fromIndex + ":" + toIndex);
        return new TomlKey(
                this.parts.subList(fromIndex, toIndex),
                null // Not possible to accurately know the location
        );
    }

    /**
     * Pare a "simple" toml key, with only bare keys.
     *
     * The following keys are simple:
     * <ul>
     *     <li>foo</li>
     *     <li>foo.bar</li>
     *     <li>foo.bar.baz</li>
     *     <li>123.foo.bar</li>
     * </ul>
     * The following keys are not simple:
     * <ul>
     *     <li>'foo'</li>
     *     <li>'foo'.bar</li>
     * </ul>
     *
     *
     * @param key the simple key
     * @throws IllegalArgumentException if the key is not a simple key
     * @return the parsed key
     */
    public static TomlKey parseSimple(String key) {
        Objects.requireNonNull(key, "Null key");
        String[] parts = key.split("\\.");
        for (String part : parts) {
            if (!isValidBareIdentifier(part)) {
                throw new IllegalArgumentException("Key is not \"simple\": " + key);
            }
        }
        return new TomlKey(List.of(parts), null);
    }

    /* package */ static boolean isValidBareIdentifier(String s) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (!isValidBareIdentifier(s.charAt(i))) return false;
        }
        return true;
    }

    /**
     * Returns if this character is valid as a bare identifier.
     *
     * @param c the character to check
     * @return if it's a valid "bare" identifier
     */
    /*package */ static boolean isValidBareIdentifier(char c) {
        return AsciiUtils.isDigit(c) || AsciiUtils.isLetter(c) || c == '_' || c == '-';
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
     * Return a string representation of this key.
     *
     * The resulting value is a valid toml key.
     * It should round-trip correctly,
     * although it may not correspond exactly to the user's input.
     *
     * @return this key as a string
     */
    @Override
    public String toString() {
        if (parts.size() == 1) {
            if (isValidBareIdentifier(firstPart())) {
                return firstPart();
            }
        }
        StringBuilder builder = new StringBuilder();
        for (String part : this.parts()) {
            if (isValidBareIdentifier(part)) {
                builder.append(part);
            } else {
                EscapeUtil.writeJsonString(part, builder);
            }
        }
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return parts.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj instanceof TomlKey) {
            return ((TomlKey) obj).parts.equals(this.parts);
        } else {
            return false;
        }
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
            return new TomlKey(List.copyOf(parts), this.location);
        }
    }
}
