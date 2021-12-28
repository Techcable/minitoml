package net.techcable.minitoml;

import net.techcable.minitoml.errors.TomlSyntaxException;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Objects;

/* package */ final class TomlLexer {
    private final BufferedReader reader;
    private int lineNumber = 0;
    private int charOffset = 0;
    /**
     * A flag indicating EOF
     */
    private boolean eof = false;
    private String currentLine;
    private final EnumSet<ParserFlag> flags;

    public TomlLexer(BufferedReader reader, EnumSet<ParserFlag> flags) {
        this.reader = Objects.requireNonNull(reader);
        this.flags = Objects.requireNonNull(flags);
    }

    /**
     * Consumes the current line.
     *
     * Returns null if already at the end of the file.
     */
    @Nullable
    private String consumeLine() throws IOException {
        String line = currentLine();
        this.currentLine = null;
        return line;
    }
    @Nullable
    private String currentLine() throws IOException {
        if (this.currentLine == null) {
            this.currentLine = advanceLine();
        }
        return this.currentLine;
    }
    @Nullable // cold path
    private String advanceLine() throws IOException{
        String line = this.reader.readLine();
        if (line == null) return null; // EOF
        this.charOffset = 0;
        this.lineNumber += 1;
        this.currentLine = line;
        return line;
    }

    private TomlLocation currentLocation() {
        if (lineNumber < 1) throw new IllegalStateException();
        return new TomlLocation(
                this.lineNumber,
                this.charOffset
        );
    }

    private int peekChar() throws IOException {
        return peekChar(0);
    }
    /**
     * Peek the `ahead` characters in the current line,
     * returning `-1` if it reaches past the EOF or EOL.
     *
     * @return the next character or `-1` if
     */
    private int peekChar(int ahead) throws IOException {
        if (ahead < 0) throw new IllegalArgumentException();
        String line = currentLine();
        if (line == null) return -1;
        if (charOffset < line.length() - ahead) {
            return line.charAt(charOffset + ahead);
        } else {
            return -1;
        }
    }

    /**
     * Skip over the current character.
     *
     * @throws IllegalStateException if there is an EOF or EOL
     */
    private void skipChar() throws IOException {
        skipChars(1);
    }

    enum NumberSign {
        PLUS(1),
        MINUS(-1),
        MISSING(1);

        final int value;
        NumberSign(int value) {
            this.value = value;
        }
    }
    enum NumberParseMode {
        HEX(16),
        BINARY(2),
        OCTAL(8),
        INTEGER(10),
        FLOAT(-1);

        private final int radix;
        NumberParseMode(int radix) {
            this.radix = radix;
        }

        public boolean isValidChar(char c) {
            if (this == FLOAT) throw new UnsupportedOperationException();
            return switch (c) {
                case '_' -> true;
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    int val = ((int) c) - '0';
                    return val < this.radix;
                }
                case 'A', 'B', 'C', 'D', 'E', 'F' -> this == HEX;
                default -> false;
            };
        }
    }

    public Number parseNumber() throws IOException {
        int startIndex = this.charOffset;
        NumberSign sign = switch (peekChar()) {
            case '+' -> NumberSign.PLUS;
            case '-' -> NumberSign.MINUS;
            default -> NumberSign.MISSING;
        };
        NumberParseMode mode = switch (peekChar()) {
            case '0' -> switch (peekChar(1)) {
                case 'x' -> NumberParseMode.HEX;
                case 'o' -> NumberParseMode.OCTAL;
                case 'b' -> NumberParseMode.BINARY;
                case '.' -> NumberParseMode.FLOAT;
                default -> NumberParseMode.INTEGER;
            };
            // TODO: Better identifiers for keywords
            case 'n', 'i' -> NumberParseMode.FLOAT;
            default -> NumberParseMode.INTEGER;
        };

    }

    /**
     * Return the number of characters remaining on the current line.
     */
    private int remaining() {
        if (currentLine == null) throw new IllegalStateException();
        return currentLine.length() - charOffset;

    }

    /**
     * Skip over the specified number of character.
     *
     * @throws IllegalStateException if there are insufficent number
     */
    private void skipChars(int amount) throws IOException {
        String line = currentLine();
        if (line == null) throw new IllegalStateException("EOF");
        if (amount < 0) throw new IllegalArgumentException();
        int remaining = line.length() - charOffset;
        if (amount > remaining) {
            throw new IllegalArgumentException("Tried to skip " + amount + " chars with only " + remaining + " chars remaining");
        }
        this.charOffset += amount;
    }

    private void skipWhitespace() throws IOException {
        int res;
        while ((res = peekChar()) >= 0) {
            if (res == ' ' || res == '\t') {
                skipChar();
            } else {
                break;
            }
        }
    }

    /**
     * Peek at the next token without necessarily consuming it.
     *
     * Returns
     */
    public TokenType peekToken() throws IOException {
        skipWhitespace();
        int c = peekChar();
        return switch (c) {
            case '#' -> TokenType.BEGIN_COMMENT;
            case '"' -> TokenType.BEGIN_STRING;
            case '.'  -> TokenType.DOT;
            case '\'' -> TokenType.BEGIN_LITERAL_STRING;
            case '+' -> TokenType.PLUS;
            case '-' -> TokenType.MINUS; // NOTE: Could be identifier or number....
            case -1 -> {
                if (eof) {
                    yield TokenType.EOF;
                } else {
                    yield TokenType.EOL;
                }
            }
            case '[' -> TokenType.OPEN_BRACKET;
            case ']' -> TokenType.CLOSE_BRACKET;
            case '_' -> TokenType.IDENTIFIER;
            default -> {
                if (c >= '0' && c <= '9') {
                    yield TokenType.DIGIT; // Could be identifier or number
                } else if (c >= 'a' && c <= 'z') {
                    yield TokenType.IDENTIFIER;
                } else if (c >= 'A' && c <= 'Z') {
                    yield TokenType.IDENTIFIER;
                } else {
                    throw new TomlSyntaxException(
                            "Unexpected character " + c,
                            currentLocation()
                    );
                }
            }
        };
    }

    public void skipToken(TokenType token) {
        if (!token.simple) throw new IllegalArgumentException("Token is not simple: " + token);
        TokenType actualToken = peekToken();
        if (actualToken != token) throw new IllegalStateException();
        this.skipChars(token.length());

    }


    public String parseString(boolean allowMultiline) throws IOException {
        boolean literal = switch (peekToken()) {
            case BEGIN_LITERAL_STRING -> true;
            case BEGIN_STRING -> false;
            default -> throw new IllegalStateException("Unexpected token: " + token);
        };
        char quoteChar = literal ? '\'' : '"';
        TomlLocation startLocation = this.currentLocation();
        if (peekChar(1) == quoteChar && peekChar(2) == quoteChar) {
            if (allowMultiline) {
                return parseMultilineString(literal);
            } else {
                throw new TomlSyntaxException("Multiline strings are not allowed here", currentLocation());
            }
        } else {
            assert peekChar() == quoteChar;
            skipChar();
        }

        if (literal) {
            // We have already skipped over the starting quote, so we can just find the ending
            int end = this.currentLine.indexOf('\'', charOffset);
            assert end >= charOffset;
            if (end < 0) {
                throw new TomlSyntaxException(
                        "Unable to find closing quote `'` for literal string",
                        startLocation
                );
            }
            int length = end - charOffset;
            assert length >= 0;
            String result = this.currentLine.substring(charOffset, end);
            assert result.length() == length;
            this.skipChars(length);
            return result;
        } else {
            /*
             * Now we have to parse a regular ("basic") string, including escapes.
             *
             * The initial opening quote has already been parsed.
             * We cannot
             */
            int c;
            StringBuilder result = new StringBuilder();
            while ((c = peekChar()) >= 0) {
                switch (c) {
                    case '\\' -> this.parseRegularEscape(result);
                    case '"' -> {
                        // Found closing quote
                        this.skipChar();
                        return result.toString();
                    }
                    default -> {
                        result.append(c);
                        this.skipChar();
                    }
                }
            }
            throw new TomlSyntaxException("Unable to find closing quote `\"` for basic string", startLocation)
        }
    }
    private void parseRegularEscape(StringBuilder result) throws IOException {
        TomlLocation escapeStart = currentLocation();
        int escapeChar = peekChar(1);
        if (escapeChar < 0) throw new TomlSyntaxException("Unexpected EOL after string escape", escapeStart);
        skipChars(2);
        int remaining = remaining();
        // We have now consumed both the backlash and the escape char
        if (escapeChar == 'u' || escapeChar == 'U') {
            // Unicode escapes are special
            int neededDigits = switch (escapeChar) {
                case 'u' -> 4;
                case 'U' -> 8;
                default -> throw new AssertionError();
            };
            if (remaining < neededDigits) {
                throw new TomlSyntaxException(
                        "A unicode escape \\" + escapeChar + " must be followed by "
                                + neededDigits + " hex digits",
                        escapeStart
                );
            }
            int escapedCodepoint;
            try {
                escapedCodepoint = Integer.parseUnsignedInt(currentLine, charOffset, charOffset + neededDigits, 16);
            } catch (NumberFormatException e) {
                throw new TomlSyntaxException("Invalid hex digits for unicode escape \\" + escapeChar, escapeStart);
            }
            if (escapedCodepoint < 0) throw new IllegalArgumentException();
            try {
                result.append(Character.toChars(escapedCodepoint))
            } catch (IllegalArgumentException e) {
                throw new TomlSyntaxException("Not a valid uncicode codepoint: " + escapedCodepoint, escapeStart);
            }
        } else {
            int escapedChar = EscapeUtil.parseSimpleEscape((char) escapeChar);
            if (escapedChar < 0)
                throw new TomlSyntaxException("Invalid escape sequence \\" + escapeChar, escapeStart);
            ;
            result.append(Character.toChars(escapedChar));
        }
    }
    private String parseMultilineString(boolean literal) throws IOException {
        this.skipChars(3);
        throw new UnsupportedOperationException("TODO");
    }

    public enum TokenType {
        BEGIN_COMMENT(false),
        BEGIN_STRING(false),
        BEGIN_LITERAL_STRING(false),
        EOL(false),
        EOF(false),
        DOT(false),
        // Not simple because it signals the beginning of a number
        PLUS(false),
        /**
         * Either the beginning of a number or an identifier.
         */
        MINUS(false),
        // Not simple because it signals the beginning of a number
        DIGIT(false),
        COMMA(true),
        IDENTIFIER(false),
        OPEN_BRACKET(true),
        CLOSE_BRACKET(true),
        OPEN_BRACE(true),
        CLOSE_BRACE(true);

        int length() {
            if (!simple) throw new UnsupportedOperationException();
            return 1; // For now, simple tokens are always length one
        }

        /* package */ final boolean simple;
        TokenType(boolean simple) {
            this.simple = simple;
        }
    }
}
