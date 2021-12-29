package net.techcable.minitoml;

import org.jetbrains.annotations.ApiStatus;

import java.math.BigInteger;

@ApiStatus.Internal
public class MathUtil {
    /**
     * Count the number of bits needed to represent the specified number,
     * properly handling twos complement representation
     *
     * Examples:
     * countEffectiveBits(Integer.MAX_VALUE) == 32
     * countEffectiveBits(Long.MAX_VALUE) == 64
     * countEffectiveBits(Long.MIN_VALUE) == 64
     * countEffectiveBits(((long) Integer.MAX_VALUE) + 1) == 33
     * countEffectiveBits(((long) Integer.MIN_VALUE) - 1) == 33
     *
     * @return the number of bits
     */
    public static int countEffectiveBits(long l) {
        /*
         * Carefully tested to count the number of bits needed to represent a long.
         *
         * This is needed to give good error messages like '36 bit integer cannot fit into a long: <integer>'
         */
        if (l == Long.MIN_VALUE) {
            return 64;
        } else {
            return 64 - Long.numberOfLeadingZeros(l < 0 ? ~l : l) + 1;
        }
    }
    public static boolean canFitExactlyInDouble(long l) {
        // Anything that takes less than 52 bits obviously fits
        if (countEffectiveBits(l) <= 52) return true;
        /*
         * Past this point only certain integers can be exactly represented as a long.
         *
         * Cast back and forth and see if it works.
         */
        double d = (double) l;
        return ((long) d) == l;
    }
    public static boolean canFitExactlyInInt(long l) {
        return ((long) ((int) l)) == l;
    }
}
