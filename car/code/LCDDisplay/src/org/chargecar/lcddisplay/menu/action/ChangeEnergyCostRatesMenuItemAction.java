package org.chargecar.lcddisplay.menu.action;

import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.CharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;
import org.chargecar.lcddisplay.LCD;
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
        costOfElectricity = lcd.getCostOfElectricity();
        carMpg = lcd.getCarMpg();
        costOfGas = lcd.getCostOfGas();

        if (state == 2)
            activate2();
        else if (state == 3)
            activate3();
        else {
            getCharacterDisplay().setLine(0, "^  Energy-Cost Rates");
            getCharacterDisplay().setLine(1, "   Eletric: $<" + GeneralHelper.round(costOfElectricity, 2) + ">");
            getCharacterDisplay().setLine(2, "   Gas: $" + GeneralHelper.round(costOfGas, 2));
            getCharacterDisplay().setLine(3, "v  Mpg: " + carMpg);
        }
    }

    public void activate2() {
        getCharacterDisplay().setLine(0, "^  Energy-Cost Rates");
        getCharacterDisplay().setLine(1, "   Eletric: $" + GeneralHelper.round(costOfElectricity, 2));
        getCharacterDisplay().setLine(2, "   Gas: $<" + GeneralHelper.round(costOfGas, 2) + ">");
        getCharacterDisplay().setLine(3, "v  Mpg: " + carMpg);
    }

    public void activate3() {
        getCharacterDisplay().setLine(0, "^  Energy-Cost Rates");
        getCharacterDisplay().setLine(1, "   Eletric: $" + GeneralHelper.round(costOfElectricity, 2));
        getCharacterDisplay().setLine(2, "   Gas: $" + GeneralHelper.round(costOfGas, 2));
        getCharacterDisplay().setLine(3, "v  Mpg: <" + carMpg + ">");
    }

    public final void start() {
        if (state == 1) {
            setCostOfElectricity(costOfElectricity);
            getCharacterDisplay().setText(getActionPerformedText());
        } else if (state == 2) {
            setCostOfGas(costOfGas);
            getCharacterDisplay().setText(getActionPerformedText2());
        } else if (state == 3) {
            setCarMpg(carMpg);
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
            activate3();
        } else if (state == 2) {
            state = 1;
            activate();
        } else if (state == 3) {
            state = 2;
            activate2();
        }
    }

    public void downEvent() {
        if (state == 1) {
            state = 2;
            activate2();
        } else if (state == 2) {
            state = 3;
            activate3();
        } else if (state == 3) {
            state = 1;
            activate();
        }
    }

    public final void rightEvent() {
        if (state == 1) {
            costOfElectricity += .01;
            if (costOfElectricity > 9.99)
                costOfElectricity = 9.99;
            getCharacterDisplay().setCharacter(1, 14, String.valueOf(GeneralHelper.round(costOfElectricity, 2)));
        } else if (state == 2) {
            costOfGas += .01;
            if (costOfGas > 9.99)
                costOfGas = 9.99;
            getCharacterDisplay().setCharacter(2, 10, String.valueOf(GeneralHelper.round(costOfGas, 2)));
        } else if (state == 3) {
            carMpg += 1;
            if (carMpg > 99)
                carMpg = 99;
            getCharacterDisplay().setCharacter(3, 9, String.valueOf(carMpg));
        }
    }

    public final void leftEvent() {
        if (state == 1) {
            costOfElectricity -= .01;
            if (costOfElectricity < .01)
                costOfElectricity = .01;
            getCharacterDisplay().setCharacter(1, 14, String.valueOf(GeneralHelper.round(costOfElectricity, 2)));
        } else if (state == 2) {
            costOfGas -= .01;
            if (costOfGas < .01)
                costOfGas = .01;
            getCharacterDisplay().setCharacter(2, 10, String.valueOf(GeneralHelper.round(costOfGas, 2)));
        } else if (state == 3) {
            carMpg -= 1;
            if (carMpg < 10)
                carMpg = 10;
            getCharacterDisplay().setCharacter(3, 9, String.valueOf(carMpg));
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