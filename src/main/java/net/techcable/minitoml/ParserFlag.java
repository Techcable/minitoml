package net.techcable.minitoml;

import javax.swing.text.html.parser.Parser;
import java.util.EnumSet;

public enum ParserFlag {
    /**
     * Parse large integers as a {@link java.math.BigInteger} instead of throwing an error.
     *
     * The standard says "Arbitrary 64-bit signed integers should be accepted and handled losslessly.
     * If an integer cannot be represented losslessly, an error must be thrown."
     *
     * This option is enabled by default, since it only
     *
     */
    USE_BIG_INTEGERS(true),
    /**
     * Parse decimal numbers as a {@link java.math.BigDecimal} instead of as a double.
     *
     * Enabling this option would give a more exact representation of the user's input.
     * For example the number `.3`
     *
     * According to the standard, "Floats should be implemented as IEEE 754 binary64 values."
     *
     * In the interest of performance (and standards compliance), this option is disabled by default.
     */
    USE_EXACT_DECIMALS(false);

    private final boolean enabledByDefault;
    ParserFlag(boolean enabledByDefault) {
        this.enabledByDefault = enabledByDefault;
    }
    public static EnumSet<ParserFlag> defaultFlags() {
        EnumSet<ParserFlag> flags = EnumSet.noneOf(ParserFlag.class);
        for (ParserFlag flag : ParserFlag.values()) {
            if (flag.enabledByDefault) {
                flags.add(flag);
            }
        }
        return flags;
    }

}
