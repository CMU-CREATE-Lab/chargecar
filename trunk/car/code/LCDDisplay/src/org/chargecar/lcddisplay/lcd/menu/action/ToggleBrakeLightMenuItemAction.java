package org.chargecar.lcddisplay.lcd.menu.action;

import edu.cmu.ri.createlab.LCD;
import edu.cmu.ri.createlab.LCDProxy;
import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.TwoOptionMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;

import java.util.Map;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class ToggleBrakeLightMenuItemAction extends TwoOptionMenuItemAction {
    final LCD lcd = LCDProxy.getInstance();
    private boolean isBrakeLightEnabled = false;

    public ToggleBrakeLightMenuItemAction(final MenuItem menuItem,
                                          final MenuStatusManager menuStatusManager,
                                          final CharacterDisplay characterDisplay) {
        this(menuItem, menuStatusManager, characterDisplay, null);
    }

    public ToggleBrakeLightMenuItemAction(final MenuItem menuItem,
                                          final MenuStatusManager menuStatusManager,
                                          final CharacterDisplay characterDisplay,
                                          final Map<String, String> properties) {
        super(menuItem, menuStatusManager, characterDisplay, properties);
    }

    protected boolean shouldOption1BeSelectedUponActivation() {
        return isBrakeLightEnabled;
    }

    protected void executeOption1Action() {
        setAirConditioningEnabled(true);
    }

    protected void executeOption2Action() {
        setAirConditioningEnabled(false);
    }

    private void setAirConditioningEnabled(final boolean actionState) {
        if (lcd == null) {
            getCharacterDisplay().setLine(0, "No connection to LCD.");
            return;
        }
        this.isBrakeLightEnabled = actionState;

        if (actionState)
            lcd.turnOnBrakeLight();
        else
            lcd.turnOffBrakeLight();

    }
}
