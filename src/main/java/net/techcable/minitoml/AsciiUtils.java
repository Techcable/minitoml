package net.techcable.minitoml;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class AsciiUtils {
    public static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    public static boolean isLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }
}
