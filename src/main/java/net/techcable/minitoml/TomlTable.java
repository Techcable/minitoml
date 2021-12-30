package net.techcable.minitoml;

import net.techcable.minitoml.errors.TomlMissingKeyException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A toml table.
 *
 * This is a mapping from keys (strings) to values ({@link TomlValue}
 */
public class TomlTable extends TomlValue {
    private final Map<String, TomlValue> shallowKeys;

    /* package */ TomlTable(Map<String, TomlValue> valueMap, TomlLocation location) {
        super(TomlType.TABLE, location);
        this.shallowKeys = Objects.requireNonNull(valueMap);
    }

    /**
     * Get the value associated with the specified key, if it is present.
     *
     * The specified key may contain dots, however it must be a "simple" key as specified by {@link TomlKey#parseSimple(String)}
     *
     * @param key the key to get
     * @throws IllegalArgumentException if the specified key is not simple
     * @return the value associated with the specified key.
     */
    @NotNull
    public Optional<TomlValue> get(String key) {
        // fast path for simple keys
        TomlValue value = this.shallowKeys.get(key);
        if (value != null) return Optional.of(value);
        return this.get(TomlKey.parseSimple(key));
    }

    /**
     * Get the value associated with the specified key, if it is present.
     *
     * This method should never throw an exception.
     *
     * @param key the key to get
     * @return the associated value (if present)
     */
    @NotNull
    public Optional<TomlValue> get(TomlKey key) {
        Objects.requireNonNull(key, "Null key");
        TomlTable targetTable = this;
        List<String> parts = key.parts();
        if (parts.size() > 1) {
            // Everything but the last part
            for (int i = 0; i < parts.size() - 1; i++) {
                String part = parts.get(i);
                TomlValue value = targetTable.shallowKeys.get(key);
                if (value instanceof TomlTable) {
                    targetTable = (TomlTable) value;
                } else {
                    return Optional.empty();
                }
            }
        }
        return Optional.ofNullable(shallowKeys.get(key.lastPart()));
    }

    /**
     * Require that the value associated with the specified key is present, throwing a {@link TomlMissingKeyException} if it is missing.
     *
     * The specified key may contain dots, however it must be a "simple" key as specified by {@link TomlKey#parseSimple(String)}.
     * This is intended as convenience method for when the key is a compile-time constant (not when the key is specified by the user).
     *
     * @param key the key to get
     * @throws TomlMissingKeyException if the specified key is missing
     * @throws IllegalArgumentException if the specified key is not simple
     * @return
     */
    @NotNull
    public TomlValue require(String key) {
        // fast path for simple keys
        TomlValue value = this.shallowKeys.get(key);
        return value != null ? value : this.require(TomlKey.parseSimple(key));
    }

    /**
     * Require that the specified key exists, throwing an appropriate error if it's missing
     *
     * @param key the key to require
     * @throws TomlMissingKeyException if the key is missing
     * @return the value with the specified key
     */
    @NotNull
    public TomlValue require(TomlKey key) {
        return this.get(key).orElseThrow(() -> new TomlMissingKeyException(key, this.getLocation()));
    }

    @Override
    protected void toJson(StringBuilder builder) {
        builder.append('{');
        boolean needsComma = false;
        for (Map.Entry<String, TomlValue> entry : this.shallowKeys.entrySet()) {
            if (needsComma) builder.append(',');
            EscapeUtil.writeJsonString(entry.getKey(), builder);
            builder.append(':');
            entry.getValue().toJson(builder);
            needsComma = true;
        }
        builder.append('}');
    }

    @Override
    public String toString() {
        return this.toJsonString();
    }

    /**
     * Return a copy of this table, with the location changed to the specified value.
     *
     * @param location the new location
     * @return this table, with the new location
     */
    public TomlTable withLocation(TomlLocation location) {
        return new TomlTable(this.shallowKeys, location);
    }

    /**
     * View this table as a "shallow" map.
     *
     * This does not do the special handling of nested keys, so it may not be what you want.
     *
     * @return this table, viewed as a shallow map.
     */
    public Map<String, TomlValue> asShallowMap() {
        return this.shallowKeys; // Should be unmodifiable
    }

    /**
     * Create a new builder to modify this table's entries.
     *
     * @return a new builder
     */
    public TomlTableBuilder rebuild()  {
        TomlTableBuilder builder = new TomlTableBuilder();
        builder.shallowKeys.putAll(this.shallowKeys);
        builder.location = this.getLocation();
        return builder;
    }

    /**
     * Create a table with the specified (shallow) values.
     *
     * This is the counterpart to {@link #asShallowMap()}
     *
     * @param valueMap the map
     * @return a new table with the specified keys
     */
    public static TomlTable fromShallowMap(Map<String, TomlValue> valueMap) {
        return new TomlTable(Map.copyOf(valueMap), null);
    }

    public static TomlTableBuilder builder() {
        return new TomlTableBuilder();
    }

}
