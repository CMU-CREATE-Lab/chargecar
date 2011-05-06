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
public final class UpdateLCDSoftwareMenuItemAction extends CharacterDisplayMenuItemAction {

    private static final Logger LOG = Logger.getLogger(UpdateLCDSoftwareMenuItemAction.class);
    private static final String DEFAULT_LABEL_ACTION_CANCELLED = "Cancelled!";

    private static final String PROPERTY_ACTION_CANCELLED = "action.cancelled";

    private final LCD lcd = LCDProxy.getInstance();
    private boolean doOnce = false;

    public UpdateLCDSoftwareMenuItemAction(final MenuItem menuItem,
                                           final MenuStatusManager menuStatusManager,
                                           final CharacterDisplay characterDisplay) {
        this(menuItem, menuStatusManager, characterDisplay, null);
    }

    public UpdateLCDSoftwareMenuItemAction(final MenuItem menuItem,
                                           final MenuStatusManager menuStatusManager,
                                           final CharacterDisplay characterDisplay,
                                           final Map<String, String> properties) {
        super(menuItem, menuStatusManager, characterDisplay, properties);
    }

    public void activate() {

        if (lcd == null) {
            LOG.error("UpdateLCDSoftwareMenuItemAction.activate(): lcd is null");
            return;
        }

        getCharacterDisplay().setLine(0, "Update LCD Software");
        getCharacterDisplay().setLine(1, "Please insert a USB");
        getCharacterDisplay().setLine(2, "drive now.");
        getCharacterDisplay().setLine(3, "[*] Update software");
    }

    public final void start() {
        if (!doOnce) {
            doOnce = true;
            getCharacterDisplay().setLine(0, "Preparing transfer.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            sleep();
            if (copyLcdFiles()) {
                getCharacterDisplay().setLine(0, "Software updated.");
                getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
                getCharacterDisplay().setLine(2, "Unplug the USB drive");
                getCharacterDisplay().setLine(3, "and cycle power now.");
            }
            sleepLongThenPopUpToParentMenuItem();
        }
    }

    public void cleanUp() {
        GeneralHelper.resetCounts();
        unmountUsbDrive();
    }

    public final void renameDirs() {
        if (new File(LCDConstants.TMP_LOCAL_LCD_SOFTWARE_PATH).exists() &&  new File(LCDConstants.LOCAL_LCD_SOFTWARE_PATH).exists()) {
            try {
                Runtime.getRuntime().exec("rm -rf " + LCDConstants.LOCAL_LCD_SOFTWARE_PATH + ";" + "mv " + LCDConstants.TMP_LOCAL_LCD_SOFTWARE_PATH + " " + LCDConstants.LOCAL_LCD_SOFTWARE_PATH);
            } catch (IOException e) {
                LOG.error("UpdateLCDSoftwareMenuItemAction.renameDirs(): " + e.getMessage());
            }
        }
    }

    public final boolean copyLcdFiles() {
        try {
            final File outputPath = new File(LCDConstants.LOCAL_LCD_SOFTWARE_PATH);
            final File[] tmpInputPath = GeneralHelper.listPath(new File(LCDConstants.USB_ROOT_PATH));

            if (tmpInputPath == null || tmpInputPath[0].lastModified() != 0) {
                getCharacterDisplay().setLine(0, "USB drive not found.");
                getCharacterDisplay().setLine(1, "No files were");
                getCharacterDisplay().setLine(2, "transferred.");
                return false;
            }

            final File inputPath = new File(tmpInputPath[0] + LCDConstants.USB_LCD_SOFTWARE_PATH);

            if (inputPath.exists()) {
                GeneralHelper.copyDirectory(inputPath, outputPath);
                getCharacterDisplay().setLine(0, "Finalizing.");
                getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
                getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
                getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
                cleanUp();
                //renameDirs();
                return true;
            } else {
                getCharacterDisplay().setLine(0, "Update dir not found");
                getCharacterDisplay().setLine(1, "No files were");
                getCharacterDisplay().setLine(2, "transferred.");
                cleanUp();
                return false;
            }
        } catch (IOException e) {
            LOG.error("UpdateLCDSoftwareMenuItemAction.copyLcdFiles(): " + e.getMessage());
            getCharacterDisplay().setLine(0, "There was an error");
            getCharacterDisplay().setLine(1, "transferring.");
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            cleanUp();
            return false;
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
            LOG.error("UpdateLCDSoftwareMenuItemAction.sleep(): InterruptedException while sleeping", e);
        }
    }

    private void sleepLong() {
        try {
            Thread.sleep(10000);
            doOnce = false;
        } catch (InterruptedException e) {
            LOG.error("UpdateLCDSoftwareMenuItemAction.sleepLong(): InterruptedException while sleeping", e);
        }
    }
}