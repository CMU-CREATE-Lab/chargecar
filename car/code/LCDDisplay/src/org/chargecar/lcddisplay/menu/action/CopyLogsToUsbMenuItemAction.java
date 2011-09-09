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

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class CopyLogsToUsbMenuItemAction extends CharacterDisplayMenuItemAction {

    private static final Logger LOG = Logger.getLogger(CopyLogsToUsbMenuItemAction.class);
    private static final String DEFAULT_LABEL_ACTION_CANCELLED = "Cancelled!";
    private static final String PROPERTY_ACTION_CANCELLED = "action.cancelled";

    private int currentState = 1;

    private final LCD lcd = LCDProxy.getInstance();
    private boolean transferring = false;
    private boolean wasHere = false;

    private String lastDownloadDate = null;

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


    public void upEvent() {
        wasHere = false;
        if (currentState == 1) {
            currentState = 3;
            start();
        } else if (currentState == 2) {
            currentState = 1;
            start();
        } else if (currentState == 3) {
            currentState = 2;
            start();
        }
    }

    public void downEvent() {
        wasHere = false;
        if (currentState == 1) {
            currentState = 2;
            start();
        } else if (currentState == 2) {
            currentState = 3;
            start();
        } else if (currentState == 3) {
            currentState = 1;
            start();
        }
    }

    public void activate() {
        if (lcd == null) {
            LOG.error("CopyLogsToUsbMenuItemAction.activate(): lcd is null");
            return;
        }

        final String tmpDate = lcd.getSavedProperty("lastDownloadDate");
        lastDownloadDate = tmpDate.substring(4, 6) + "/" + tmpDate.substring(6, 8) + "/" + tmpDate.substring(0, 4);

        getCharacterDisplay().setLine(0, "Copy Files to USB");
        getCharacterDisplay().setLine(1, "Please insert a USB");
        getCharacterDisplay().setLine(2, "drive now.");
        getCharacterDisplay().setLine(3, "[*] Continue");
    }

    public final void start() {
        if (wasHere == false && transferring)
            transferring = false;
        if (!transferring) {
            if (currentState == 1) {
                getCharacterDisplay().setLine(0, "Download log files");
                getCharacterDisplay().setLine(1, "[*] Today only");
                getCharacterDisplay().setLine(2, "[ ] Since " + lastDownloadDate);
                getCharacterDisplay().setLine(3, "[ ] All files");
            } else if (currentState == 2) {
                getCharacterDisplay().setLine(0, "Download log files");
                getCharacterDisplay().setLine(1, "[ ] Today only");
                getCharacterDisplay().setLine(2, "[*] Since " + lastDownloadDate);
                getCharacterDisplay().setLine(3, "[ ] All files");
            } else if (currentState == 3) {
                getCharacterDisplay().setLine(0, "Download log files");
                getCharacterDisplay().setLine(1, "[ ] Today only");
                getCharacterDisplay().setLine(2, "[ ] Since " + lastDownloadDate);
                getCharacterDisplay().setLine(3, "[*] All files");
            }
        }
        if (wasHere) {
            wasHere = false;
            transferring = true;
            getCharacterDisplay().setLine(0, "Initializing.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            sleep();
            if (copyLogFiles()) {
                getCharacterDisplay().setLine(0, "Files transferred.");
                getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
                getCharacterDisplay().setLine(2, "It is safe to unplug");
                getCharacterDisplay().setLine(3, "your USB drive.");
            }
            sleepLongThenPopUpToParentMenuItem();
            return;
        }
        wasHere = true;
    }

    public void cleanUp() {
        GeneralHelper.resetCounts();
        GeneralHelper.unmountUsbDrive();
    }

    public final boolean copyLogFiles() {
        try {
            final File inputPath = new File(LCDConstants.LOG_PATH);
            //NOTE: We are assuming this path will always contain the drive me want
            //This works only if it is the first drive connected and the OS is setup using the
            //ChargeCar configuration.
            final File tmpOutputPath = new File(LCDConstants.USB_DRIVE_PATH);
            final File[] logFiles;

            logFiles = GeneralHelper.listPath(new File(LCDConstants.LOG_PATH));
            //a non-mounted drive has a date of 00:00:00 GMT, January 1, 1970
            //thus the last modified time returns 0
            if (tmpOutputPath == null) {// || tmpOutputPath[0].lastModified() != 0) {
                getCharacterDisplay().setLine(0, "USB drive not found.");
                getCharacterDisplay().setLine(1, "No files were");
                getCharacterDisplay().setLine(2, "transferred.");
                getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
                return false;
            } else if (logFiles == null) {
                LOG.error("CopyLogsToUsbMenuItemAction.activate(): Error reading log path.");
                getCharacterDisplay().setLine(0, "There was an error");
                getCharacterDisplay().setLine(1, "reading the log dir.");
                getCharacterDisplay().setLine(2, "No files were");
                getCharacterDisplay().setLine(3, "transferred.");
                cleanUp();
                return false;
            } else if (logFiles.length == 0) {
                getCharacterDisplay().setLine(0, "There are no log");
                getCharacterDisplay().setLine(1, "files present.");
                getCharacterDisplay().setLine(2, "No files were");
                getCharacterDisplay().setLine(3, "transferred.");
                cleanUp();
                return false;
            } else {
                final File outputPath = new File(tmpOutputPath.toString() + "/ChargeCar_logs");
                if (!outputPath.exists()) {
                    outputPath.mkdir();
                }

                int filterType = LCDConstants.FILTER_NONE;
                if (currentState == 1) {
                    filterType = LCDConstants.FILTER_TODAY;
                } else if (currentState == 2) {
                    filterType = LCDConstants.FILTER_SINCE_LAST_DOWNLOAD;
                } else if (currentState == 3) {
                    filterType = LCDConstants.FILTER_SINCE_BEGINNING;
                }

                GeneralHelper.copyDirectory(inputPath, outputPath, filterType);
                getCharacterDisplay().setLine(0, "Finalizing.");
                getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
                getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
                getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
                cleanUp();
                return true;
            }
        } catch (IOException e) {
            LOG.error("CopyLogsToUsbMenuItemAction.copyLogFiles(): " + e.getMessage());
            getCharacterDisplay().setLine(0, "There was an error");
            getCharacterDisplay().setLine(1, "transferring.");
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            cleanUp();
            return false;
        }
    }

    public final void stop() {
        transferring = false;
        wasHere = false;
        getCharacterDisplay().setText(getActionCancelledText());
        sleepThenPopUpToParentMenuItem();
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
            //doOnce = false;
        } catch (InterruptedException e) {
            LOG.error("CopyLogsToUsbMenuItemAction.sleepLong(): InterruptedException while sleeping", e);
        }
    }
}