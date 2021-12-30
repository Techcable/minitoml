package net.techcable.minitoml;

import net.techcable.minitoml.errors.TomlOverflowException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TomlTableBuilder {
    /*
     * The value is either instanceof:
     * 1. TomlValue
     * 2. TomlTableBuilder
     * 3. ArrayList<TomlTable>
     */
    private final Map<String, Object> shallowKeys = new HashMap<>();
    private final Map<String, TomlTableBuilder> subTableBuilders = new HashMap<>();
    private TomlLocation location;

    /**
     * Set the location of the new table to the specified value.
     *
     * @param location the new location.
     * @return this object
     */
    public TomlTableBuilder withLocation(TomlLocation location) {
        this.location = location;
        return this;
    }

    /**
     * Associate the specified value with the specified key.
     * <p>
     * Overrides any existing value.
     * <p>
     * The specified key must be a simple key as specified by {@link TomlKey#parseSimple(String)} .
     * <p>
     * Supports doted keys like `foo.bar`, creating intermediate tables as necessary.
     *
     * @param key   the key
     * @param value the new value
     * @return the new builder.
     */
    public TomlTableBuilder put(String key, TomlValue value) {
        put(TomlKey.parseSimple(key), value);
        return this;
    }

    /**
     * Associate the specified value with the specified key.
     * <p>
     * Overrides any existing value.
     * <p>
     * Supports doted keys like `foo.bar`, creating intermediate tables as necessary.
     *
     * @param key   the key
     * @param value the new value
     * @return the new builder.
     */
    public TomlTableBuilder put(TomlKey key, TomlValue value) {
        List<String> parts = key.parts();
        if (parts.size() == 1) {
            String part = key.firstPart();
            // Easy
            this.shallowKeys.put(part, value);
        } else {
            if (subBuilder == null) {
                TomlValue existingValue = shallowKeys.remove(key.firstPart());
                if (existingValue == null) {
                    subBuilder = TomlTable.builder();
                } else if (existingValue instanceof TomlTable) {
                    // Take existing table, and modify it
                    subBuilder = ((TomlTable) existingValue).rebuild();
                } else {
                    // Completely override any non-table values
                    subBuilder = TomlTable.builder();
                }
                subTableBuilders.put(key.firstPart(), subBuilder);
            }
            assert subBuilder != null;
            assert subBuilder == subTableBuilders.get(key.firstPart());
            // Recurse
            try {
                subBuilder.put(key.sliceParts(1, key.parts().size()), value);
            } catch (StackOverflowError e) {
                // TODO: Need more robust limits
                throw new TomlOverflowException(
                        "Key with " + key.parts().size() + " levels of nesting is unreasonably large",
                        key.getLocation()
                );
            }
        }
        return this;
    }

    public TomlTable build() {
        return new TomlTable(Map.copyOf(this.shallowKeys), this.location);
    }
}
