package org.chargecar.lcddisplay.helpers;

import org.apache.log4j.Logger;
import org.chargecar.lcddisplay.LCD;
import org.chargecar.lcddisplay.LCDConstants;
import org.chargecar.lcddisplay.LCDProxy;

import java.io.*;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public class GeneralHelper {
    private static final Logger LOG = Logger.getLogger(GeneralHelper.class);
    private static double numFiles = 0;
    private static final LCD lcd = LCDProxy.getInstance();
    private static Date startDate;
    private static Date endDate;
    private static Date testDate = null;
    private static List<File> fileList = new ArrayList<File>();

    private GeneralHelper() {
    }

    public static void unmountUsbDrive() {
        try {
            Runtime.getRuntime().exec("umount " + LCDConstants.USB_DRIVE_PATH);
        } catch (IOException e) {
            LOG.error("GeneralHelper.unmountUsbDrive(): " + e.getMessage());
        }
    }

    public static boolean isWithinDateRange(final Date startDate, final Date endDate, final Date testDate) {
        return !(testDate.before(startDate) || testDate.after(endDate));

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

    public static void getFileList(final File file, final int filterType) {
        if (file.isDirectory()) {
            final File[] files;

            if (filterType == LCDConstants.FILTER_NONE || filterType == LCDConstants.FILTER_SINCE_BEGINNING) {
                files = file.listFiles();
            } else {
                final FileFilter dirFilter = new FileFilter() {
                    public boolean accept(final File f) {
                        if (filterType == LCDConstants.FILTER_TODAY) {
                            final Calendar cal = Calendar.getInstance();
                            cal.set(Calendar.HOUR_OF_DAY, 0);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);
                            cal.getTime();
                            startDate = cal.getTime();
                            endDate = new Date();
                        } else if (filterType == LCDConstants.FILTER_SINCE_LAST_DOWNLOAD) {
                            final DateFormat df = new SimpleDateFormat("yyyyMMdd");
                            try {
                                startDate = df.parse(lcd.getSavedProperty("lastDownloadDate"));
                            } catch (ParseException e) {
                                LOG.error(e.getMessage());
                            }
                            endDate = new Date();
                        }

                        final Pattern pattern = Pattern.compile("\\d{8}");
                        final Matcher matcher = pattern.matcher(f.getName());
                        if (matcher.find()) {
                            final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
                            try {
                                testDate = formatter.parse(matcher.group(0));
                            } catch (ParseException e) {
                                LOG.error(e.getMessage());
                            }
                        }
                        return (f.isDirectory() || isWithinDateRange(startDate, endDate, testDate));
                    }
                };
                files = file.listFiles(dirFilter);
            }

            for (int i = 0; i < files.length; i++) {
                getFileList(files[i], filterType);
            }
        } else {
            numFiles += 1;
            fileList.add(file);
        }
    }

    /* copy a file from one location to another */
    public static void copyFile(final File sourceFile, final File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileInputStream fIn = null;
        FileOutputStream fOut = null;
        FileChannel source = null;
        FileChannel destination = null;
        try {
            fIn = new FileInputStream(sourceFile);
            source = fIn.getChannel();
            fOut = new FileOutputStream(destFile);
            destination = fOut.getChannel();
            long transfered = 0;
            final long bytes = source.size();
            while (transfered < bytes) {
                transfered += destination.transferFrom(source, 0, source.size());
                destination.position(transfered);
            }
        } finally {
            if (source != null) {
                source.close();
            } else if (fIn != null) {
                fIn.close();
            }
            if (destination != null) {
                destination.close();
            } else if (fOut != null) {
                fOut.close();
            }
        }
    }

    /* copy files from one location to another*/
    public static void copyDirectory(final File sourceLocation, final File targetLocation, final int filterType)
            throws IOException {
        if (numFiles == 0) {
            lcd.setText(0, 0, "Transfer in progress");
            lcd.setText(1, 0, LCDConstants.BLANK_LINE);
            lcd.setText(2, 4, padRight("0% complete", LCDConstants.NUM_COLS));
            lcd.setText(3, 0, LCDConstants.BLANK_LINE);
            getFileList(sourceLocation, filterType);
        }

        for (int i = 0; i < fileList.size(); i++) {
            copyFile(fileList.get(i), new File(targetLocation, fileList.get(i).getName()));
            lcd.setText(2, 4, padRight(Math.round(((i + 1) / numFiles) * 100) + "% complete", LCDConstants.NUM_COLS));
        }
        if (filterType == LCDConstants.FILTER_SINCE_LAST_DOWNLOAD) {
            final DateFormat df = new SimpleDateFormat("yyyyMMdd");
            lcd.setSavedProperty("lastDownloadDate", df.format(new Date()));
        }
    }

    public static void resetCounts() {
        numFiles = 0;
        fileList.clear();
    }

}