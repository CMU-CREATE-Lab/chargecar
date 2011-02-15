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
public final class ToggleCabinHeatMenuItemAction extends TwoOptionMenuItemAction {
    final LCD lcd = LCDProxy.getInstance();
    private boolean isCabinHeatEnabled = false;

    public ToggleCabinHeatMenuItemAction(final MenuItem menuItem,
                                         final MenuStatusManager menuStatusManager,
                                         final CharacterDisplay characterDisplay) {
        this(menuItem, menuStatusManager, characterDisplay, null);
    }

    public ToggleCabinHeatMenuItemAction(final MenuItem menuItem,
                                         final MenuStatusManager menuStatusManager,
                                         final CharacterDisplay characterDisplay,
                                         final Map<String, String> properties) {
        super(menuItem, menuStatusManager, characterDisplay, properties);
    }

    protected boolean shouldOption1BeSelectedUponActivation() {
        return isCabinHeatEnabled;
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
        this.isCabinHeatEnabled = actionState;

        if (actionState)
            lcd.turnOnCabinHeat();
        else
            lcd.turnOffCabinHeat();

    }
}
