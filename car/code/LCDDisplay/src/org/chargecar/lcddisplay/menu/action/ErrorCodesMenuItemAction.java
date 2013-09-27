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
    private static final String[] errorCodeLookupTable = {"Driving off while plugged in", "Interlock is tripped",
            "Communication fault with a bank or cell", "Charge overcurrent",
            "Discharge overcurrent", "Over-temperature",
            "Under voltage", "Over voltage",
            "No battery voltage", "High voltage B- leak to chassis",
            "High voltage B+ leak to chassis", "Relay K1 is shorted",
            "Contactor K2 is shorted", "Contactor K3 is shorted",
            "Open K1 or K3, or shorted K2", "Open K2",
            "Excessive precharge time", "EEPROM stack overflow"};


    public ErrorCodesMenuItemAction(final MenuItem menuItem,
                                    final MenuStatusManager menuStatusManager,
                                    final CharacterDisplay characterDisplay) {
        super(menuItem, menuStatusManager, characterDisplay);
    }

    public void activate() {
        final LCD lcd = LCDProxy.getInstance();
        final BMSManager bmsManager = BMSManager.getInstance();
        final BMSAndEnergy bmsData = (bmsManager == null) ? null : bmsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("ErrorCodesMenuItemAction.performAction(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            //getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        } else if (lcd == null) {
            LOG.error("ErrorCodesMenuItemAction.activate(): lcd is null");
            return;
        }

        //final int controllerErrorCode = lcd.getMotorControllerErrorCodes();
        final int bmsErrorCode = bmsData.getBmsState().getBMSFault().getCode();

        if (bmsErrorCode == 0) {
            getCharacterDisplay().setLine(0, "No BMS errors");
            getCharacterDisplay().setLine(1, "currently being");
            getCharacterDisplay().setLine(2, "reported.");
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
        } else {
            getCharacterDisplay().setLine(0, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            try {
                getCharacterDisplay().setText(errorCodeLookupTable[bmsErrorCode - 1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                getCharacterDisplay().setLine(0, "BMS error code");
                getCharacterDisplay().setLine(1, "not found.");
                LOG.error("BMS error code not found: " + e);
            }
        }
    }
}
