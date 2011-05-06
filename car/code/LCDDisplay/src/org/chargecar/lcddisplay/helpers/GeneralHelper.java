package org.chargecar.lcddisplay.helpers;

import org.apache.log4j.Logger;
import org.chargecar.lcddisplay.LCD;
import org.chargecar.lcddisplay.LCDConstants;
import org.chargecar.lcddisplay.LCDProxy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public class GeneralHelper {
    private static final Logger LOG = Logger.getLogger(GeneralHelper.class);
    private static double index = 1;
    private static double numFiles = 0;
    private static final LCD lcd = LCDProxy.getInstance();

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

    /* Lists the contents of a directory. Only does one level*/
    public static File[] listPath(final File path) {
        final File[] files = path.listFiles();
        if (files == null) return null;
        return files;
    }

    /* Count the number of files in a directory. Does so recursively*/
    public static int numFiles(final File file) {
        //Store the total size of all files
        int size = 0;
        if (file.isDirectory()) {
            //All files and subdirectories
            final File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                //Recursive call
                size += numFiles(files[i]);
            }
        }
        //Base case
        else {
            size += 1;
        }
        return size;
    }

    /* copy files from one location to another*/
    public static void copyDirectory(final File sourceLocation, final File targetLocation)
            throws IOException {

        if (numFiles == 0) {
            lcd.setText(0, 0, "Transfer in progress");
            lcd.setText(2, 4, "0% complete");
            numFiles = numFiles(sourceLocation);
        }

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            final String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {
            try {
                final FileChannel in = new FileInputStream(sourceLocation).getChannel();
                final FileChannel out = new FileOutputStream(targetLocation).getChannel();
                in.transferTo(0, in.size(), out);
            } catch (IOException e) {
                LOG.error("GeneralHelper.copyDirectory(): " + e.getMessage());
            }

            lcd.setText(2, 4, padRight(Math.round((index++ / numFiles) * 100) + "% complete", LCDConstants.NUM_COLS));

        }
    }

    public static void resetCounts() {
        numFiles = 0;
        index = 1;
    }

}
