package net.techcable.minitoml;

import java.util.Objects;

/**
 * The location of a character within a toml file.
 *
 * Used for error reporting.
 */
public class TomlLocation {
    private final int lineNumber;
    private final int charOffset;

    /**
     * Get the line number.
     *
     * This is one-based, so it will always be >=1
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * The offset of the character within the file.
     *
     * @return the relative character offset
     */
    public int getCharOffset() {
        return charOffset;
    }

    /**
     * Create a new location object.
     *
     * @param lineNumber the line number
     * @param charOffset the character offset
     * @throws IllegalArgumentException if the line number is < 1 or the char offset is negative
     */
    public TomlLocation(int lineNumber, int charOffset) {
        if (lineNumber < 1 || charOffset < 0) throw new IllegalArgumentException();
        this.lineNumber = lineNumber;
        this.charOffset = charOffset;
    }

    @Override
    public String toString() {
        return lineNumber + ":" + charOffset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineNumber, charOffset);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj instanceof TomlLocation) {
            TomlLocation loc = (TomlLocation) obj;
            return loc.lineNumber == this.lineNumber && loc.charOffset == this.charOffset;
        } else {
            return false;
        }
    }
}
