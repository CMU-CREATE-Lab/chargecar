package org.chargecar.lcddisplay.menu.action;

import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.RepeatingActionCharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;
import org.chargecar.lcddisplay.LCD;
import org.chargecar.lcddisplay.LCDConstants;
import org.chargecar.lcddisplay.LCDProxy;

import java.util.concurrent.TimeUnit;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class MotorTemperatureMenuItemAction extends RepeatingActionCharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(MotorTemperatureMenuItemAction.class);

    public MotorTemperatureMenuItemAction(final MenuItem menuItem,
                                          final MenuStatusManager menuStatusManager,
                                          final CharacterDisplay characterDisplay) {
        super(menuItem, menuStatusManager, characterDisplay, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void preActivate() {
        getCharacterDisplay().setLine(0, "Motor");
        getCharacterDisplay().setLine(1, "Temperature: ");
        getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
    }

    @Override
    protected void performAction() {
        final LCD lcd = LCDProxy.getInstance();
        if (lcd == null) {
            LOG.error("MotorTemperatureMenuItemAction.performAction(): lcd is null");
            return;
        }
        final double motorTemp = Math.round(lcd.getTemperatureInCelsius(lcd.getMotorTemperatureInKelvin()) * 100.0) / 100.0;

        LOG.trace("MotorTemperatureMenuItemAction.performAction(): updating temperatures");
        getCharacterDisplay().setCharacter(1, 13, String.valueOf(motorTemp));
    }
}
