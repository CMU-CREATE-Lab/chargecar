package org.chargecar.lcddisplay.menu.action;

import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.CharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;
import org.chargecar.lcddisplay.LCD;
import org.chargecar.lcddisplay.LCDConstants;
import org.chargecar.lcddisplay.LCDProxy;
import org.chargecar.lcddisplay.helpers.GeneralHelper;

import java.io.*;
import java.util.Map;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class CopyLogsToUsbMenuItemAction extends CharacterDisplayMenuItemAction {

    private static final Logger LOG = Logger.getLogger(CopyLogsToUsbMenuItemAction.class);
    private static final String DEFAULT_LABEL_ACTION_PERFORMED = "Files transferred.";
    private static final String DEFAULT_LABEL_ACTION_CANCELLED = "Cancelled!";

    private static final String PROPERTY_ACTION_PERFORMED = "action.performed";
    private static final String PROPERTY_ACTION_CANCELLED = "action.cancelled";

    private final LCD lcd = LCDProxy.getInstance();
    private boolean doOnce = false;

    public CopyLogsToUsbMenuItemAction(final MenuItem menuItem,
                                       final MenuStatusManager menuStatusManager,
                                       final CharacterDisplay characterDisplay) {
        this(menuItem, menuStatusManager, characterDisplay, null);
    }

    public CopyLogsToUsbMenuItemAction(final MenuItem menuItem,
                                       final MenuStatusManager menuStatusManager,
                                       final CharacterDisplay characterDisplay,
                                       final Map<String, String> properties) {
        super(menuItem, menuStatusManager, characterDisplay, properties);
    }

    public void activate() {
        final File[] logFiles;
        if (lcd == null) {
            LOG.error("CopyLogsToUsbMenuItemAction.activate(): lcd is null");
            return;
        }
        logFiles = GeneralHelper.listPath(new File(LCDConstants.LOG_PATH));
        getCharacterDisplay().setLine(0, "Copy Files to USB");
        getCharacterDisplay().setLine(1, "Please insert a USB");
        getCharacterDisplay().setLine(2, "drive now.");
        if (logFiles == null) {
            LOG.error("CopyLogsToUsbMenuItemAction.activate(): Error reading log path.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, "There was an error");
            getCharacterDisplay().setLine(3, "reading the log dir.");
            return;
        }

        if (logFiles.length == 0) {
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, "There are no log");
            getCharacterDisplay().setLine(3, "files present.");
        }
        getCharacterDisplay().setLine(3, "[*] Copy Log Files");
    }

    public final void start() {
        if (!doOnce) {
            doOnce = true;
            getCharacterDisplay().setLine(0, "Preparing transfer.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            sleep();
            copyLogFiles();
            unmountUsbDrive();
            sleepLongThenPopUpToParentMenuItem();
        }
    }

    public final void copyLogFiles() {
        try {
            final File inputPath = new File(LCDConstants.LOG_PATH);
            final File[] tmpOutputPath = GeneralHelper.listPath(new File(LCDConstants.USB_ROOT_PATH));

            if (!inputPath.exists()) {
                getCharacterDisplay().setLine(0, "Local dir not found.");
                getCharacterDisplay().setLine(1, "No files were");
                getCharacterDisplay().setLine(2, "transferred.");
            } else if (tmpOutputPath == null) {
                getCharacterDisplay().setLine(0, "USB drive not found.");
                getCharacterDisplay().setLine(1, "No files were");
                getCharacterDisplay().setLine(2, "transferred.");
            } else {
                final File outputPath = new File(tmpOutputPath[0].toString() + "/ChargeCar_logs");
                //Note, we are assuming there are no other usb drives connected.
                //really, without dedicating a specific usb drive to be used
                //with this, we really cannot do much better...I think
                GeneralHelper.copyDirectory(new File(LCDConstants.LOG_PATH), outputPath);
                getCharacterDisplay().setLine(0, "Files transferred.");
                getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
                getCharacterDisplay().setLine(2, "It is safe to unplug");
                getCharacterDisplay().setLine(3, "your USB drive.");
            }
        } catch (IOException e) {
            LOG.error("CopyLogsToUsbMenuItemAction.copyLcdFiles(): " + e.getMessage());
            getCharacterDisplay().setLine(0, "There was an error");
            getCharacterDisplay().setLine(1, "transferring.");
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
        }
    }

    public final void stop() {
        getCharacterDisplay().setText(getActionCancelledText());
        sleepThenPopUpToParentMenuItem();
    }

    private void unmountUsbDrive() {
        String path = LCDConstants.USB_UNMOUNT_PATH;
        int index = 1;
        try {
            while (!new File(path).exists()) {
                path += String.valueOf(index++);
                if (index > 7) break;
            }
            path = LCDConstants.USB_UNMOUNT_PATH2;
            index = 1;
            while (!new File(path).exists()) {
                path += String.valueOf(index++);
                if (index > 7) break;
            }
            Runtime.getRuntime().exec("sudo umount " + path);
        } catch (IOException e) {
            LOG.error("UpdateLCDSoftwareMenuItemAction.unmountUsbDrive(): " + e.getMessage());
        }
    }

    private String getActionPerformedText() {
        return getProperty(PROPERTY_ACTION_PERFORMED, DEFAULT_LABEL_ACTION_PERFORMED);
    }

    private String getActionCancelledText() {
        return getProperty(PROPERTY_ACTION_CANCELLED, DEFAULT_LABEL_ACTION_CANCELLED);
    }

    private void sleepThenPopUpToParentMenuItem() {
        sleep();
        super.stop();
    }

    private void sleepLongThenPopUpToParentMenuItem() {
        sleepLong();
        super.stop();
    }

    private void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            LOG.error("CopyLogsToUsbMenuItemAction.sleep(): InterruptedException while sleeping", e);
        }
    }

    private void sleepLong() {
        try {
            Thread.sleep(10000);
            doOnce = false;
        } catch (InterruptedException e) {
            LOG.error("CopyLogsToUsbMenuItemAction.sleepLong(): InterruptedException while sleeping", e);
        }
    }
}