package org.chargecar.lcddisplay.menu.action;

import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.CharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;
import org.chargecar.lcddisplay.LCD;
import org.chargecar.lcddisplay.LCDConstants;
import org.chargecar.lcddisplay.LCDProxy;
import org.chargecar.lcddisplay.helpers.GeneralHelper;

import java.util.Map;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class ChangeAccessoryButtonsMenuItemAction extends CharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(ChangeAccessoryButtonsMenuItemAction.class);

    private static final String DEFAULT_LABEL_ACTION_PERFORMED = "Accessory button 1  set to: ";
    private static final String DEFAULT_LABEL2_ACTION_PERFORMED = "Accessory button 2  set to: ";
    private static final String DEFAULT_LABEL3_ACTION_PERFORMED = "Accessory button 3  set to: ";
    private static final String DEFAULT_LABEL_ACTION_CANCELLED = "Cancelled!";

    private static final String PROPERTY_ACTION_PERFORMED = "action.performed";
    private static final String PROPERTY_ACTION_CANCELLED = "action.cancelled";

    private final LCD lcd = LCDProxy.getInstance();

    private String accessoryButtonOneAction;
    private String accessoryButtonTwoAction;
    private String accessoryButtonThreeAction;

    private String newState;

    private int state = 1;
    private int index = 0;
    private final int numStates = LCDConstants.ACCESSORY_BUTTON_STATES.size();


    public ChangeAccessoryButtonsMenuItemAction(final MenuItem menuItem,
                                                final MenuStatusManager menuStatusManager,
                                                final CharacterDisplay characterDisplay) {
        this(menuItem, menuStatusManager, characterDisplay, null);
    }

    public ChangeAccessoryButtonsMenuItemAction(final MenuItem menuItem,
                                                final MenuStatusManager menuStatusManager,
                                                final CharacterDisplay characterDisplay,
                                                final Map<String, String> properties) {
        super(menuItem, menuStatusManager, characterDisplay, properties);
    }

    public void activate() {
        
        accessoryButtonOneAction = getAccessoryButtonOneState();
        accessoryButtonTwoAction = getAccessoryButtonTwoState();
        accessoryButtonThreeAction = getAccessoryButtonThreeState();

        if (state == 2) {
            newState = accessoryButtonTwoAction;
            activate2();
        } else if (state == 3) {
            newState = accessoryButtonThreeAction;
            activate3();
        } else {
            newState = accessoryButtonOneAction;
            getCharacterDisplay().setLine(0, "^ Assign Acc. Btns");
            getCharacterDisplay().setLine(1, "  Acc. Btn 1: <" + accessoryButtonOneAction + ">");
            getCharacterDisplay().setLine(2, "  Acc. Btn 2: " + accessoryButtonTwoAction);
            getCharacterDisplay().setLine(3, "v Acc. Btn 3: " + accessoryButtonThreeAction);
        }
    }

    public void activate2() {
        getCharacterDisplay().setLine(0, "^ Assign Acc. Btns");
        getCharacterDisplay().setLine(1, "  Acc. Btn 1: " + accessoryButtonOneAction);
        getCharacterDisplay().setLine(2, "  Acc. Btn 2: <" + accessoryButtonTwoAction + ">");
        getCharacterDisplay().setLine(3, "v Acc. Btn 3: " + accessoryButtonThreeAction);
    }

    public void activate3() {
        getCharacterDisplay().setLine(0, "^ Assign Acc. Btns");
        getCharacterDisplay().setLine(1, "  Acc. Btn 1: " + accessoryButtonOneAction);
        getCharacterDisplay().setLine(2, "  Acc. Btn 2: " + accessoryButtonTwoAction);
        getCharacterDisplay().setLine(3, "v Acc. Btn 3: <" + accessoryButtonThreeAction + ">");
    }

    public final void start() {
        if (state == 1) {
            setAccessoryButtonOneAction(newState);
            getCharacterDisplay().setText(getActionPerformedText());
        } else if (state == 2) {
            setAccessoryButtonTwoAction(newState);
            getCharacterDisplay().setText(getActionPerformedText2());
        } else if (state == 3) {
            setAccessoryButtonThreeAction(newState);
            getCharacterDisplay().setText(getActionPerformedText3());
        }
        sleepThenPopUpToParentMenuItem();
    }

    public final void stop() {
        getCharacterDisplay().setText(getActionCancelledText());
        sleepThenPopUpToParentMenuItem();
    }

    public void upEvent() {
        index = 0;
        if (state == 1) {
            state = 3;
            while (index < numStates && (LCDConstants.ACCESSORY_BUTTON_STATES.get(index).compareTo(getAccessoryButtonThreeState())) != 0) {
                index++;
            }
            newState = getAccessoryButtonThreeState();
            activate3();
        } else if (state == 2) {
            state = 1;
            while (index < numStates && (LCDConstants.ACCESSORY_BUTTON_STATES.get(index).compareTo(getAccessoryButtonOneState())) != 0) {
                index++;
            }
            newState = getAccessoryButtonOneState();
            activate();
        } else if (state == 3) {
            state = 2;
            while (index < numStates && (LCDConstants.ACCESSORY_BUTTON_STATES.get(index).compareTo(getAccessoryButtonTwoState())) != 0) {
                index++;
            }
            newState = getAccessoryButtonTwoState();
            activate2();
        }
    }

    public void downEvent() {
        index = 0;
        if (state == 1) {
            state = 2;
            while (index < numStates && (LCDConstants.ACCESSORY_BUTTON_STATES.get(index).compareTo(getAccessoryButtonTwoState())) != 0) {
                index++;
            }
            newState = getAccessoryButtonTwoState();
            activate2();
        } else if (state == 2) {
            state = 3;
            while (index < numStates && (LCDConstants.ACCESSORY_BUTTON_STATES.get(index).compareTo(getAccessoryButtonThreeState())) != 0) {
                index++;
            }
            newState = getAccessoryButtonThreeState();
            activate3();
        } else if (state == 3) {
            state = 1;
            while (index < numStates && (LCDConstants.ACCESSORY_BUTTON_STATES.get(index).compareTo(getAccessoryButtonOneState())) != 0) {
                index++;
            }
            newState = getAccessoryButtonOneState();
            activate();
        }
    }

    public final void rightEvent() {
        if (state == 1) {
            index++;
            if (index >= numStates) index = 0;
            newState = LCDConstants.ACCESSORY_BUTTON_STATES.get(index);
            final String output = "<" + newState + ">";
            getCharacterDisplay().setCharacter(1, 14, GeneralHelper.padRight(output, LCDConstants.NUM_COLS - 14));
        } else if (state == 2) {
            index++;
            if (index >= numStates) index = 0;
            newState = LCDConstants.ACCESSORY_BUTTON_STATES.get(index);
            final String output = "<" + newState + ">";
            getCharacterDisplay().setCharacter(2, 14, GeneralHelper.padRight(output, LCDConstants.NUM_COLS - 14));
        } else if (state == 3) {
            index++;
            if (index >= numStates) index = 0;
            newState = LCDConstants.ACCESSORY_BUTTON_STATES.get(index);
            final String output = "<" + newState + ">";
            getCharacterDisplay().setCharacter(3, 14, GeneralHelper.padRight(output, LCDConstants.NUM_COLS - 14));
        }
    }

    public final void leftEvent() {
        if (state == 1) {
            index--;
            if (index < 0) index = numStates - 1;
            newState = LCDConstants.ACCESSORY_BUTTON_STATES.get(index);
            final String output = "<" + newState + ">";
            getCharacterDisplay().setCharacter(1, 14, GeneralHelper.padRight(output, LCDConstants.NUM_COLS - 14));
        } else if (state == 2) {
            index--;
            if (index < 0) index = numStates - 1;
            newState = LCDConstants.ACCESSORY_BUTTON_STATES.get(index);
            final String output = "<" + newState + ">";
            getCharacterDisplay().setCharacter(2, 14, GeneralHelper.padRight(output, LCDConstants.NUM_COLS - 14));
        } else if (state == 3) {
            index--;
            if (index < 0) index = numStates - 1;
            newState = LCDConstants.ACCESSORY_BUTTON_STATES.get(index);
            final String output = "<" + newState + ">";
            getCharacterDisplay().setCharacter(3, 14, GeneralHelper.padRight(output, LCDConstants.NUM_COLS - 14));
        }
    }

    private String getActionPerformedText() {
        return getProperty(PROPERTY_ACTION_PERFORMED, DEFAULT_LABEL_ACTION_PERFORMED) + getAccessoryButtonOneState();
    }

    private String getActionPerformedText2() {
        return getProperty(PROPERTY_ACTION_PERFORMED, DEFAULT_LABEL2_ACTION_PERFORMED) + getAccessoryButtonTwoState();
    }

    private String getActionPerformedText3() {
        return getProperty(PROPERTY_ACTION_PERFORMED, DEFAULT_LABEL3_ACTION_PERFORMED) + getAccessoryButtonThreeState();
    }

    private String getActionCancelledText() {
        return getProperty(PROPERTY_ACTION_CANCELLED, DEFAULT_LABEL_ACTION_CANCELLED);
    }

    private String getAccessoryButtonOneState() {
        return lcd.getAccessoryButtonOne();
    }

    private String getAccessoryButtonTwoState() {
        return lcd.getAccessoryButtonTwo();
    }

    private String getAccessoryButtonThreeState() {
        return lcd.getAccessoryButtonThree();
    }

    private void setAccessoryButtonOneAction(final String newAccessoryButtonOneState) {
        lcd.setAccessoryButtonOne(newAccessoryButtonOneState);
    }

    private void setAccessoryButtonTwoAction(final String newAccessoryButtonTwoState) {
        lcd.setAccessoryButtonTwo(newAccessoryButtonTwoState);
    }

    private void setAccessoryButtonThreeAction(final String newAccessoryButtonThreeState) {
        lcd.setAccessoryButtonThree(newAccessoryButtonThreeState);
    }

    private void sleepThenPopUpToParentMenuItem() {
        sleep();
        super.stop();
    }

    private void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            LOG.error("ChangeAccessoryButtonsMenuItemAction.sleep(): InterruptedException while sleeping", e);
        }
    }
}

