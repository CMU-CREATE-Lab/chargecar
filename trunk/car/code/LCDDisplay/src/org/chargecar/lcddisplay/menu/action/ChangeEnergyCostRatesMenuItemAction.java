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
public final class ChangeEnergyCostRatesMenuItemAction extends CharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(ChangeEnergyCostRatesMenuItemAction.class);

    private static final String DEFAULT_LABEL_ACTION_PERFORMED = "Cost of Electricity set to: ";
    private static final String DEFAULT_LABEL2_ACTION_PERFORMED = "Cost of Gas set to: ";
    private static final String DEFAULT_LABEL3_ACTION_PERFORMED = "Car Mpg set to: ";
    private static final String DEFAULT_LABEL_ACTION_CANCELLED = "Cancelled!";

    private static final String PROPERTY_ACTION_PERFORMED = "action.performed";
    private static final String PROPERTY_ACTION_CANCELLED = "action.cancelled";

    private final LCD lcd = LCDProxy.getInstance();

    private double costOfElectricity;
    private int carMpg;
    private double costOfGas;

    private int state = 1;
    private double newRate;

    public ChangeEnergyCostRatesMenuItemAction(final MenuItem menuItem,
                                               final MenuStatusManager menuStatusManager,
                                               final CharacterDisplay characterDisplay) {
        this(menuItem, menuStatusManager, characterDisplay, null);
    }

    public ChangeEnergyCostRatesMenuItemAction(final MenuItem menuItem,
                                               final MenuStatusManager menuStatusManager,
                                               final CharacterDisplay characterDisplay,
                                               final Map<String, String> properties) {
        super(menuItem, menuStatusManager, characterDisplay, properties);
    }

    public void activate() {
        costOfElectricity = getCostOfElectricity();
        carMpg = getCarMpg();
        costOfGas = getCostOfGas();

        if (state == 2) {
            newRate = costOfGas;
            activate2();
        } else if (state == 3) {
            newRate = carMpg;
            activate3();
        } else {
            newRate = costOfElectricity;
            getCharacterDisplay().setLine(0, "^ Energy-Cost Rates");
            getCharacterDisplay().setLine(1, "  Eletric: $<" + GeneralHelper.round(costOfElectricity, 2) + ">");
            getCharacterDisplay().setLine(2, "  Gas: $" + GeneralHelper.round(costOfGas, 2));
            getCharacterDisplay().setLine(3, "v Mpg: " + carMpg);
        }
    }

    public void activate2() {
        getCharacterDisplay().setLine(0, "^ Energy-Cost Rates");
        getCharacterDisplay().setLine(1, "  Eletric: $" + GeneralHelper.round(costOfElectricity, 2));
        getCharacterDisplay().setLine(2, "  Gas: $<" + GeneralHelper.round(costOfGas, 2) + ">");
        getCharacterDisplay().setLine(3, "v Mpg: " + carMpg);
    }

    public void activate3() {
        getCharacterDisplay().setLine(0, "^ Energy-Cost Rates");
        getCharacterDisplay().setLine(1, "  Eletric: $" + GeneralHelper.round(costOfElectricity, 2));
        getCharacterDisplay().setLine(2, "  Gas: $" + GeneralHelper.round(costOfGas, 2));
        getCharacterDisplay().setLine(3, "v Mpg: <" + carMpg + ">");
    }

    public final void start() {
        if (state == 1) {
            setCostOfElectricity(newRate);
            getCharacterDisplay().setText(getActionPerformedText());
        } else if (state == 2) {
            setCostOfGas(newRate);
            getCharacterDisplay().setText(getActionPerformedText2());
        } else if (state == 3) {
            setCarMpg((int) newRate);
            getCharacterDisplay().setText(getActionPerformedText3());
        }
        sleepThenPopUpToParentMenuItem();
    }

    public final void stop() {
        getCharacterDisplay().setText(getActionCancelledText());
        sleepThenPopUpToParentMenuItem();
    }

    public void upEvent() {
        if (state == 1) {
            state = 3;
            newRate = getCarMpg();
            activate3();
        } else if (state == 2) {
            state = 1;
            newRate = getCostOfElectricity();
            activate();
        } else if (state == 3) {
            state = 2;
            newRate = getCostOfGas();
            activate2();
        }
    }

    public void downEvent() {
        if (state == 1) {
            state = 2;
            newRate = getCostOfGas();
            activate2();
        } else if (state == 2) {
            state = 3;
            newRate = getCarMpg();
            activate3();
        } else if (state == 3) {
            state = 1;
            newRate = getCostOfElectricity();
            activate();
        }
    }

    public final void rightEvent() {
        if (state == 1) {
            newRate += .01;
            if (newRate > 99999.99)
                newRate = 99999.99;
            getCharacterDisplay().setCharacter(1, 11, GeneralHelper.padRight("$<" + String.valueOf(GeneralHelper.round(newRate, 2)) + ">", LCDConstants.NUM_COLS - 11));
        } else if (state == 2) {
            newRate += .01;
            if (newRate > 99999.99)
                newRate = 99999.99;
            getCharacterDisplay().setCharacter(2, 7, GeneralHelper.padRight("$<" + String.valueOf(GeneralHelper.round(newRate, 2)) + ">", LCDConstants.NUM_COLS - 7));
        } else if (state == 3) {
            newRate += 1;
            if (newRate > 999)
                newRate = 999;
            getCharacterDisplay().setCharacter(3, 7, GeneralHelper.padRight("<" + String.valueOf((int) newRate) + ">", LCDConstants.NUM_COLS - 7));
        }
    }

    public final void leftEvent() {
        if (state == 1) {
            newRate -= .01;
            if (newRate < .01)
                newRate = .01;
            getCharacterDisplay().setCharacter(1, 11, GeneralHelper.padRight("$<" + String.valueOf(GeneralHelper.round(newRate, 2)) + ">", LCDConstants.NUM_COLS - 11));
        } else if (state == 2) {
            newRate -= .01;
            if (newRate < .01)
                newRate = .01;
            getCharacterDisplay().setCharacter(2, 7, GeneralHelper.padRight("$<" + String.valueOf(GeneralHelper.round(newRate, 2)) + ">", LCDConstants.NUM_COLS - 7));
        } else if (state == 3) {
            newRate -= 1;
            if (newRate <= 0)
                newRate = 1;
            getCharacterDisplay().setCharacter(3, 7, GeneralHelper.padRight("<" + String.valueOf((int) newRate) + ">", LCDConstants.NUM_COLS - 7));
        }
    }

    private String getActionPerformedText() {
        return getProperty(PROPERTY_ACTION_PERFORMED, DEFAULT_LABEL_ACTION_PERFORMED) + "$" + GeneralHelper.round(getCostOfElectricity(), 2);
    }

    private String getActionPerformedText2() {
        return getProperty(PROPERTY_ACTION_PERFORMED, DEFAULT_LABEL2_ACTION_PERFORMED) + "$" + GeneralHelper.round(getCostOfGas(), 2);
    }

    private String getActionPerformedText3() {
        return getProperty(PROPERTY_ACTION_PERFORMED, DEFAULT_LABEL3_ACTION_PERFORMED) + getCarMpg();
    }

    private String getActionCancelledText() {
        return getProperty(PROPERTY_ACTION_CANCELLED, DEFAULT_LABEL_ACTION_CANCELLED);
    }

    private double getCostOfElectricity() {
        return lcd.getCostOfElectricity();
    }

    private double getCostOfGas() {
        return lcd.getCostOfGas();
    }

    private int getCarMpg() {
        return lcd.getCarMpg();
    }

    private void setCostOfElectricity(final double newCostOfElectricity) {
        lcd.setCostOfElectricity(newCostOfElectricity);
    }

    private void setCostOfGas(final double newCostOfGas) {
        lcd.setCostOfGas(newCostOfGas);
    }

    private void setCarMpg(final int newCarMpg) {
        lcd.setCarMpg(newCarMpg);
    }

    private void sleepThenPopUpToParentMenuItem() {
        sleep();
        super.stop();
    }

    private void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            LOG.error("ChangeEnergyCostRatesMenuItemAction.sleep(): InterruptedException while sleeping", e);
        }
    }
}