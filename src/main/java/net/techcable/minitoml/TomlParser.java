package net.techcable.minitoml;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;

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

}
