package org.chargecar.lcddisplay.helpers;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public class GeneralHelper {

    private GeneralHelper() {
    }

    public static String padRight(final String s, final int n) {
        return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(final String s, final int n) {
        return String.format("%1$#" + n + "s", s);
    }

    public static double round(final double val, final int places) {
        final long factor = (long) Math.pow(10, places);

        // Shift the decimal the correct number of places
        // to the right.
        final double newVal = val * factor;

        // Round to the nearest integer.
        final long tmp = Math.round(newVal);

        // Shift the decimal the correct number of places
        // back to the left.
        return (double) tmp / factor;
    }
}
