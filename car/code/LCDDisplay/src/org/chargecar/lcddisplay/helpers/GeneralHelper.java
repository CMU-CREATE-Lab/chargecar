package org.chargecar.lcddisplay.helpers;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public class GeneralHelper {

    public static String padRight(String s, int n) {
         return String.format("%1$-" + n + "s", s);
    }

    public static String padLeft(String s, int n) {
        return String.format("%1$#" + n + "s", s);
    }
    
}
