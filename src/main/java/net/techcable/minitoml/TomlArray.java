package net.techcable.minitoml;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TomlArray extends TomlValue {
    private final List<TomlValue> elements;

    public TomlArray(List<TomlValue> elements, @Nullable TomlLocation location) {
        super(TomlType.ARRAY, location);
        this.elements = List.copyOf(elements);
    }

    @Override
    protected void toJson(StringBuilder builder) {
        builder.append('[');
        boolean needsComma = false;
        for (TomlValue element : this.elements) {
            if (needsComma) builder.append(',');
            element.toJson(builder);
            needsComma = true;
        }
        builder.append(']');
    }

    @Override
    public String toString() {
        return this.toJsonString();
    }
}
