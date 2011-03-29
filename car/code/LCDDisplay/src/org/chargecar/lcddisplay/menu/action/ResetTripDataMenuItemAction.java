package org.chargecar.lcddisplay.menu.action;

import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.TwoOptionMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;
import org.chargecar.honda.bms.BMSAndEnergy;
import org.chargecar.lcddisplay.BMSManager;
import org.chargecar.lcddisplay.LCD;
import org.chargecar.lcddisplay.LCDConstants;
import org.chargecar.lcddisplay.LCDProxy;

import java.util.Map;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class ResetTripDataMenuItemAction extends TwoOptionMenuItemAction {
    private static final Logger LOG = Logger.getLogger(ResetTripDataMenuItemAction.class);

    public ResetTripDataMenuItemAction(final MenuItem menuItem,
                                       final MenuStatusManager menuStatusManager,
                                       final CharacterDisplay characterDisplay) {
        this(menuItem, menuStatusManager, characterDisplay, null);
    }

    public ResetTripDataMenuItemAction(final MenuItem menuItem,
                                       final MenuStatusManager menuStatusManager,
                                       final CharacterDisplay characterDisplay,
                                       final Map<String, String> properties) {
        super(menuItem, menuStatusManager, characterDisplay, properties);
    }

    protected boolean shouldOption1BeSelectedUponActivation() {
        return true;
    }

    protected void executeOption1Action() {
        resetTripData();
    }

    protected void executeOption2Action() {
        // do nothing
    }

    private void resetTripData() {
        final LCD lcd = LCDProxy.getInstance();
        final BMSManager bmsManager = BMSManager.getInstance();
        final BMSAndEnergy bmsData = (bmsManager == null) ? null : bmsManager.getData();
        if (lcd == null) {
            LOG.error("ResetTripDataMenuItemAction.resetTripData(): lcd is null");
            return;
        } else if (bmsManager == null || bmsData == null) {
            LOG.error("ResetTripDataMenuItemAction.resetTripData(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        }
        getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
        lcd.setTripDistance(0);
        bmsData.getEnergyEquation().reset();
    }
}
