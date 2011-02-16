package org.chargecar.lcddisplay.menu.action;

import java.util.Map;
import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.TwoOptionMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.chargecar.lcddisplay.LCD;
import org.chargecar.lcddisplay.LCDConstants;
import org.chargecar.lcddisplay.LCDProxy;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class ToggleBatteryCoolingMenuItemAction extends TwoOptionMenuItemAction {
    private final LCD lcd = LCDProxy.getInstance();
    private boolean isBatteryCoolingEnabled = false;

    public ToggleBatteryCoolingMenuItemAction(final MenuItem menuItem,
                                              final MenuStatusManager menuStatusManager,
                                              final CharacterDisplay characterDisplay) {
        this(menuItem, menuStatusManager, characterDisplay, null);
    }

    public ToggleBatteryCoolingMenuItemAction(final MenuItem menuItem,
                                              final MenuStatusManager menuStatusManager,
                                              final CharacterDisplay characterDisplay,
                                              final Map<String, String> properties) {
        super(menuItem, menuStatusManager, characterDisplay, properties);
    }

    protected boolean shouldOption1BeSelectedUponActivation() {
        return isBatteryCoolingEnabled;
    }

    protected void executeOption1Action() {
        setAirConditioningEnabled(true);
    }

    protected void executeOption2Action() {
        setAirConditioningEnabled(false);
    }

    private void setAirConditioningEnabled(final boolean actionState) {
        if (lcd == null) return;
        getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS-1,0," ");

        this.isBatteryCoolingEnabled = actionState;

        if (actionState)
            lcd.turnOnBatteryCooling();
        else
            lcd.turnOffBatteryCooling();

    }
}
