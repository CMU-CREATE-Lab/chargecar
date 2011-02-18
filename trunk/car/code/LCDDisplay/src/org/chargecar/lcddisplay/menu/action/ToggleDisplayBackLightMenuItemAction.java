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
public final class ToggleDisplayBackLightMenuItemAction extends TwoOptionMenuItemAction {
    private static final Logger LOG = Logger.getLogger(ToggleDisplayBackLightMenuItemAction.class);
    private final LCD lcd = LCDProxy.getInstance();
    private boolean isDisplayBackLightEnabled = true; //backlight is already turned on when the program starts

    public ToggleDisplayBackLightMenuItemAction(final MenuItem menuItem,
                                                final MenuStatusManager menuStatusManager,
                                                final CharacterDisplay characterDisplay) {
        this(menuItem, menuStatusManager, characterDisplay, null);
    }

    public ToggleDisplayBackLightMenuItemAction(final MenuItem menuItem,
                                                final MenuStatusManager menuStatusManager,
                                                final CharacterDisplay characterDisplay,
                                                final Map<String, String> properties) {
        super(menuItem, menuStatusManager, characterDisplay, properties);
    }

    protected boolean shouldOption1BeSelectedUponActivation() {
        return isDisplayBackLightEnabled;
    }

    protected void executeOption1Action() {
        setDisplayBackLightEnabled(true);
    }

    protected void executeOption2Action() {
        setDisplayBackLightEnabled(false);
    }

    private void setDisplayBackLightEnabled(final boolean actionState) {
        if (lcd == null) {
            LOG.error("ToggleDisplayBackLightMenuItemAction.setDisplayBackLightEnabled(): lcd is null");
            return;
        }
        getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");

        this.isDisplayBackLightEnabled = actionState;

        if (actionState)
            lcd.turnOnDisplayBackLight();
        else
            lcd.turnOffDisplayBackLight();

    }
}
