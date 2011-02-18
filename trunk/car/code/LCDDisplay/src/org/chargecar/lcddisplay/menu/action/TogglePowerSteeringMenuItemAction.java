package org.chargecar.lcddisplay.menu.action;

import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.TwoOptionMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;
import org.chargecar.lcddisplay.LCD;
import org.chargecar.lcddisplay.LCDConstants;
import org.chargecar.lcddisplay.LCDProxy;

import java.util.Map;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class TogglePowerSteeringMenuItemAction extends TwoOptionMenuItemAction {
    private static final Logger LOG = Logger.getLogger(TogglePowerSteeringMenuItemAction.class);
    private final LCD lcd = LCDProxy.getInstance();
    private boolean isPowerSteeringEnabled = false;

    public TogglePowerSteeringMenuItemAction(final MenuItem menuItem,
                                             final MenuStatusManager menuStatusManager,
                                             final CharacterDisplay characterDisplay) {
        this(menuItem, menuStatusManager, characterDisplay, null);
    }

    public TogglePowerSteeringMenuItemAction(final MenuItem menuItem,
                                             final MenuStatusManager menuStatusManager,
                                             final CharacterDisplay characterDisplay,
                                             final Map<String, String> properties) {
        super(menuItem, menuStatusManager, characterDisplay, properties);
    }

    protected boolean shouldOption1BeSelectedUponActivation() {
        return isPowerSteeringEnabled;
    }

    protected void executeOption1Action() {
        setPowerSteeringEnabled(true);
    }

    protected void executeOption2Action() {
        setPowerSteeringEnabled(false);
    }

    private void setPowerSteeringEnabled(final boolean actionState) {
        if (lcd == null) {
            LOG.error("TogglePowerSteeringMenuItemAction.setPowerSteeringEnabled(): lcd is null");
            return;
        }
        getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");

        this.isPowerSteeringEnabled = actionState;

        if (actionState)
            lcd.turnOnPowerSteering();
        else
            lcd.turnOffPowerSteering();

    }
}
