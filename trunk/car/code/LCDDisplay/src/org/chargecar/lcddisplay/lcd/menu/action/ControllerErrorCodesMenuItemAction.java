package org.chargecar.lcddisplay.lcd.menu.action;

import edu.cmu.ri.createlab.LCD;
import edu.cmu.ri.createlab.LCDProxy;
import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.CharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class ControllerErrorCodesMenuItemAction extends CharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(ControllerErrorCodesMenuItemAction.class);

    public ControllerErrorCodesMenuItemAction(final MenuItem menuItem,
                                              final MenuStatusManager menuStatusManager,
                                              final CharacterDisplay characterDisplay) {
        super(menuItem, menuStatusManager, characterDisplay);
    }

    public void activate() {
        final LCD lcd = LCDProxy.getInstance();

        if (lcd == null) {
            getCharacterDisplay().setLine(0, "No connection to LCD.");
            return;
        }

        lcd.setText(0, 0, String.format("%1$-" + 20 + "s", "Error Code: " + lcd.getMotorControllerErrorCodes()));
        getCharacterDisplay().setLine(0, "Error Code: " + lcd.getMotorControllerErrorCodes());
    }
}
