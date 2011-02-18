package org.chargecar.lcddisplay.menu.action;

import java.util.Map;
import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.TwoOptionMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;
import org.chargecar.lcddisplay.LCD;
import org.chargecar.lcddisplay.LCDConstants;
import org.chargecar.lcddisplay.LCDProxy;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class ToggleCabinHeatMenuItemAction extends TwoOptionMenuItemAction {
    private static final Logger LOG = Logger.getLogger(ToggleCabinHeatMenuItemAction.class);
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
        setCabinHeatEnabled(true);
    }

    protected void executeOption2Action() {
        setCabinHeatEnabled(false);
    }

    private void setCabinHeatEnabled(final boolean actionState) {
        final LCD lcd = LCDProxy.getInstance();
        if (lcd == null) {
            LOG.error("ToggleCabinHeatMenuItemAction.setCabinHeatEnabled(): lcd is null");
            return;
        }
        getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");

        this.isCabinHeatEnabled = actionState;

        if (actionState)
            lcd.turnOnCabinHeat();
        else
            lcd.turnOffCabinHeat();

    }
}
