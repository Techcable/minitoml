package net.techcable.minitoml;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class EscapeUtil {
    /**
     * Return the escaped character associated with the specified escape character.
     * <p>
     * Returns -1 if the escape is invalid.
     * <p>
     * Does not handle unicode escapes (will throw UnsupportedOperationException)
     *
     * @param c T
     * @return the value of the escaped character, or -1 if invalid
     * @throws UnsupportedOperationException if unicode escapes are used
     */
    public static int parseSimpleEscape(char c) {
        return switch (c) {
            case 'b' -> '\b';
            case 't' -> '\t';
            case 'n' -> '\n';
            case 'f' -> '\f';
            case 'r' -> '\r';
            case '"' -> '"';
            case '\\' -> '\\';
            case 'u', 'U' -> throw new UnsupportedOperationException();
            default -> -1;
        };
    }

    /**
     * Escape the specified character, allowing it to be used in a json string.
     *
     * @param c the character to escape
     * @param res the buffer to escape into
     */
    public static void escapeJsonStyle(char c, StringBuilder res) {
        switch (c) {
            case '\n' -> res.append("\\n");
            case '\"' -> res.append("\\\"");
            case '\\' -> res.append("\\\\");
            case '\t' -> res.append("\\t");
            default -> {
                if (c >= 0x20 && c < 0x7F) {
                    // Ascii printable (and not a special character)
                    res.append(c);
                } else {
                    // Do a unicode escape
                    res.append("\\u");
                    String hex = Integer.toHexString(c);
                    // Pad with leading zeros
                    int missing = 4 - hex.length();
                    res.append("0".repeat(missing));
                    res.append(hex);
                }
            }
        }
    }

    /**
     * Write a json encoded string
     *
     * @param value the string to encode
     * @param out the buffer to encode into
     */
    public static void writeJsonString(String value, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            escapeJsonStyle(value.charAt(i), out);
        }
        out.append('"');
    }
}
