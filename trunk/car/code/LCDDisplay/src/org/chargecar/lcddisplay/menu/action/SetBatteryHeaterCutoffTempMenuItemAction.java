package org.chargecar.lcddisplay.menu.action;

import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.CharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;
import org.chargecar.lcddisplay.LCD;
import org.chargecar.lcddisplay.LCDProxy;

import java.util.Map;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class SetBatteryHeaterCutoffTempMenuItemAction extends CharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(SetBatteryHeaterCutoffTempMenuItemAction.class);

    private static final String DEFAULT_LABEL_ACTION_PERFORMED = "Battery hearter turn on value set to ";
    private static final String DEFAULT_LABEL_ACTION_CANCELLED = "Cancelled!";

    private static final String PROPERTY_ACTION_PERFORMED = "action.performed";
    private static final String PROPERTY_ACTION_CANCELLED = "action.cancelled";

    final LCD lcd = LCDProxy.getInstance();

    //private int temperature = lcd.getBatteryHeaterCutoffTemp();
    private int newTemp = lcd.getBatteryHeaterCutoffTemp();

    public SetBatteryHeaterCutoffTempMenuItemAction(final MenuItem menuItem,
                                                    final MenuStatusManager menuStatusManager,
                                                    final CharacterDisplay characterDisplay) {
        this(menuItem, menuStatusManager, characterDisplay, null);
    }

    public SetBatteryHeaterCutoffTempMenuItemAction(final MenuItem menuItem,
                                                    final MenuStatusManager menuStatusManager,
                                                    final CharacterDisplay characterDisplay,
                                                    final Map<String, String> properties) {
        super(menuItem, menuStatusManager, characterDisplay, properties);
    }

    public void activate() {
        newTemp = getBatteryHeaterTurnOnTemp();
        getCharacterDisplay().setLine(0, "^ Battery Heater");
        getCharacterDisplay().setLine(1, "  Cutoff Temp: " + newTemp);
        //getCharacterDisplay().setText("Battery Heater Turn On Temp: " + newTemp + " F");
        //getCharacterDisplay().setLine(2,"(use up/down buttons)");
        //getCharacterDisplay().setLine(3,"to set a new value)");
        //getCharacterDisplay().setLine(1, generateVolumeGraphLine());
    }

    public final void start() {
        setBatteryHeaterTurnOnTemp(newTemp);
        getCharacterDisplay().setText(getActionPerformedText());
        sleepThenPopUpToParentMenuItem();
    }

    public final void stop() {
        getCharacterDisplay().setText(getActionCancelledText());
        sleepThenPopUpToParentMenuItem();
    }

    public void upEvent() {
        newTemp++;
        //TODO alter at a  later point. Makes sense to have bounds checking, but the one right now is only useful with the initial layout
        if (newTemp > 99)
            newTemp = 99;

        getCharacterDisplay().setCharacter(1, 15, String.valueOf(newTemp));
        //getCharacterDisplay().setText("Battery Heater Turn On Temp: " + newTemp + " F");
        //getCharacterDisplay().setLine(2,"(use up/down buttons)");
        //getCharacterDisplay().setLine(3,"to set a new value)");
    }

    public void downEvent() {
        newTemp--;
        //TODO alter at a  later point. Makes sense to have bounds checking, but the one right now is only useful with the initial layout
        if (newTemp < 10)
            newTemp = 10;
        
        getCharacterDisplay().setCharacter(1, 15, String.valueOf(newTemp));
        //getCharacterDisplay().setText("Battery Heater Turn On Temp: " + newTemp + " F");
        //getCharacterDisplay().setLine(2,"(use up/down buttons)");
        //getCharacterDisplay().setLine(3,"to set a new value)");
    }

    public final void rightEvent() {
        //getCharacterDisplay().setLine(0, generateVolumeLine());
        //getCharacterDisplay().setLine(1, generateVolumeGraphLine());
    }

    public final void leftEvent() {
        //getCharacterDisplay().setLine(0, generateVolumeLine());
        //getCharacterDisplay().setLine(1, generateVolumeGraphLine());
    }

    private String getActionPerformedText() {
        return getProperty(PROPERTY_ACTION_PERFORMED, DEFAULT_LABEL_ACTION_PERFORMED) + getBatteryHeaterTurnOnTemp();
    }

    private String getActionCancelledText() {
        return getProperty(PROPERTY_ACTION_CANCELLED, DEFAULT_LABEL_ACTION_CANCELLED);
    }

    private int getBatteryHeaterTurnOnTemp() {
        // TODO: fetch from wherever we're persisting this
        return lcd.getBatteryHeaterCutoffTemp();
    }

    private void setBatteryHeaterTurnOnTemp(final int temp) {
        // TODO: persist this
        lcd.setBatteryHeaterCutoffTemp(temp);
    }

    private void sleepThenPopUpToParentMenuItem() {
        sleep();
        super.stop();
    }

    private void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            LOG.error("SetVolumeMenuItemAction.sleep(): InterruptedException while sleeping", e);
        }
    }
}