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
    private final BufferedReader reader;
    private TomlParser(BufferedReader reader) {
        this.reader = reader;
    }
    private int currentLineNumber = 0;
    // The character offset within the current line
    private int charOffset = 0;
    @Nullable
    private String currentLine;
    private String consumeLine() throws IOException {
        String line = currentLine();
        this.currentLine = null;
        return line;
    }
    @Nullable
    private String currentLine() throws IOException {
        if (currentLine == null) {
            String line = reader.readLine();
            if (line == null) return null;
            this.currentLine = line;
            charOffset = 0;
            currentLineNumber += 1;
            assert currentLineNumber >= 1;
        }
        return currentLine;
    }

    /**
     * Peeks at the next character, without consuming it.
     *
     * Returns -1 if at EOL (or EOF).
     */
    private int peekChar() throws IOException {
        String line = currentLine();
        if (line == null) return -1;
        if (charOffset < line.length()) {
            return charOffset;
        } else {
            return -1;
        }
    }

}
