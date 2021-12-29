package net.techcable.minitoml;

import net.techcable.minitoml.TomlLexer.TokenType;
import net.techcable.minitoml.errors.TomlSyntaxException;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * A recursive decent parser for a TOML file.
 *
 * The top-level .
 */
public class TomlParser {
    private final TomlLexer lexer;
    private TomlParser(TomlLexer lexer) {
        this.lexer = lexer;
    }

    private TomlKey parseKey() throws IOException {
        var builder = TomlKey.builder();
        builder.add(parseKeyPart());
        while (lexer.peekToken() == TokenType.DOT) {
            lexer.skipToken();
            builder.add(parseKeyPart());
        }
        return builder.build();
    }
    private static boolean isValidBareIdentifier(char c) {
        return AsciiUtils.isDigit(c) || AsciiUtils.isLetter(c) || c == '_' || c == '-';
    }
    private String parseKeyPart() throws IOException {
        TokenType tokenType = lexer.peekToken();
        return switch (tokenType) {
            case BEGIN_STRING, BEGIN_LITERAL_STRING -> lexer.parseString(false);
            /*
             * Per the spec, bare keys can be ASCII letters, digits, underscores and dashes.
             */
            case DIGIT, UNDERSCORE, MINUS, LETTER -> {
                String bareKey = lexer.takeWhile(TomlParser::isValidBareIdentifier);
                if (bareKey.isEmpty()) throw new AssertionError(); // Should've been checked by peekToken
                yield bareKey;
            }
            default -> throw new TomlSyntaxException("Expected part of a key, but got " + tokenType, lexer.currentLocation());
        };
    }

    public TomlValue parseValue() throws IOException {
        lexer.skipComments();
        TomlLocation startLocation = lexer.currentLocation();
        TokenType tokenType = lexer.peekToken();
        return switch (tokenType) {
            case BEGIN_COMMENT -> throw new AssertionError();
            case BEGIN_STRING, BEGIN_LITERAL_STRING -> new TomlPrimitive(lexer.parseString(true), startLocation);
            case EOL, EOF, DOT, COMMA, UNDERSCORE, LETTER, CLOSE_BRACE, CLOSE_BRACKET -> {
                throw new TomlSyntaxException(
                        "Expected a value, but got " + tokenType,
                        lexer.currentLocation()
                );
            }
            /*
             * In all of these cases, the end result is a number
             */
            case PLUS, MINUS, DIGIT -> {
                Number number = lexer.parseNumber();
                if (number instanceof Double || number instanceof BigDecimal) {
                    yield new TomlPrimitive(TomlType.DECIMAL_NUMBER, number, startLocation);
                } else {
                    yield new TomlPrimitive(TomlType.INTEGER, number, startLocation);
                }
            }
            case OPEN_BRACKET -> throw new UnsupportedOperationException("Inline arrays");
            case OPEN_BRACE -> throw new UnsupportedOperationException("Inline braces");
        };
    }
}
