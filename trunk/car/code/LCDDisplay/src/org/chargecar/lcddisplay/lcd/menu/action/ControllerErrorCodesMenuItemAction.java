package org.chargecar.lcddisplay.lcd.menu.action;

import edu.cmu.ri.createlab.LCD;
import edu.cmu.ri.createlab.LCDConstants;
import edu.cmu.ri.createlab.LCDProxy;
import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.CharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class ControllerErrorCodesMenuItemAction extends CharacterDisplayMenuItemAction {
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
        getCharacterDisplay().setLine(0, "Error Code: " + lcd.getMotorControllerErrorCodes());
        getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
        getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS-1,0," ");
    }
}
