package org.chargecar.lcddisplay.menu.action;

import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.CharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;
import org.chargecar.honda.bms.BMSAndEnergy;
import org.chargecar.lcddisplay.BMSManager;
import org.chargecar.lcddisplay.LCD;
import org.chargecar.lcddisplay.LCDConstants;
import org.chargecar.lcddisplay.LCDProxy;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class ErrorCodesMenuItemAction extends CharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(ErrorCodesMenuItemAction.class);

    public ErrorCodesMenuItemAction(final MenuItem menuItem,
                                    final MenuStatusManager menuStatusManager,
                                    final CharacterDisplay characterDisplay) {
        super(menuItem, menuStatusManager, characterDisplay);
    }

    public void activate() {
        final LCD lcd = LCDProxy.getInstance();
        BMSManager bmsManager = BMSManager.getInstance();
        BMSAndEnergy bmsData = (bmsManager == null) ? null : bmsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("ErrorCodesMenuItemAction.performAction(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
            return;
        } else if (lcd == null) {
            LOG.error("ErrorCodesMenuItemAction.activate(): lcd is null");
            return;
        }

        final int controllerErrorCode = lcd.getMotorControllerErrorCodes();
        final int bmsErrorCode = bmsData.getBmsState().getBMSFault().getCode();

        if (controllerErrorCode == 0 && bmsErrorCode == 0) {
            getCharacterDisplay().setLine(0, "No errors reported.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
        } else {
            if (controllerErrorCode != 0 && bmsErrorCode != 0) {
                getCharacterDisplay().setLine(0, "Controller Error: " + controllerErrorCode);
                getCharacterDisplay().setLine(1, "BMS Error: " + bmsErrorCode);
            } else if (controllerErrorCode != 0) {
                getCharacterDisplay().setLine(0, "Controller Error: " + controllerErrorCode);
            } else if (bmsErrorCode != 0) {
                getCharacterDisplay().setLine(0, "BMS Error: " + bmsErrorCode);
            }
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
        }
    }
}
