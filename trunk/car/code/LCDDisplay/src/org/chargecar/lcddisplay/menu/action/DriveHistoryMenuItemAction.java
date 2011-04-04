package org.chargecar.lcddisplay.menu.action;

import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.RepeatingActionCharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;
import org.chargecar.honda.bms.BMSAndEnergy;
import org.chargecar.lcddisplay.BMSManager;
import org.chargecar.lcddisplay.LCD;
import org.chargecar.lcddisplay.LCDConstants;
import org.chargecar.lcddisplay.LCDProxy;
import org.chargecar.lcddisplay.helpers.GeneralHelper;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class DriveHistoryMenuItemAction extends RepeatingActionCharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(DriveHistoryMenuItemAction.class);
    private LCD lcd = null;
    private BMSManager bmsManager = null;
    private BMSAndEnergy bmsData = null;
    private int currentState = 1;
    //number of performAction methods there are in this class, set all to true so that
    //the first time we enter the action we print out their headings
    private List<Boolean> printHeadings = Arrays.asList(true, true, true, true, true, true, true);

    public DriveHistoryMenuItemAction(final MenuItem menuItem,
                                      final MenuStatusManager menuStatusManager,
                                      final CharacterDisplay characterDisplay) {
        super(menuItem, menuStatusManager, characterDisplay, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void performAction() {
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        } else if (lcd == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction(): lcd is null");
            return;
        }

        if (currentState == 2)
            performAction2();
        else if (currentState == 3)
            performAction3();
        else if (currentState == 4)
            performAction4();
        else if (currentState == 5)
            performAction5();
        else if (currentState == 6)
            performAction6();
        else if (currentState == 7)
            performAction7();
        else {
            currentState = 1;
            if (printHeadings.get(0)) {
                printHeadings.set(0, false);
                getCharacterDisplay().setLine(0, "^    TRIP ENERGY");
                getCharacterDisplay().setLine(1, "  Regen  ");
                getCharacterDisplay().setLine(2, "  Discharge ");
                getCharacterDisplay().setLine(3, "v Consumed ");
            }

            final double totalEnergyConsumed = GeneralHelper.round(bmsData.getEnergyEquation().getKilowattHours(), 2);
            //total discharge (positive in the case of the bmsModel)
            final double totalDischarge = GeneralHelper.round(bmsData.getEnergyEquation().getKilowattHoursUsed(), 2);
            //total charge (regen, negative in the case of the bmsModel)
            final double totalCharge = GeneralHelper.round(bmsData.getEnergyEquation().getKilowattHoursRegen(), 2);

            getCharacterDisplay().setCharacter(1, 8, GeneralHelper.padLeft(String.valueOf(totalCharge) + "kWh", LCDConstants.NUM_COLS - 8));
            getCharacterDisplay().setCharacter(2, 12, GeneralHelper.padLeft(String.valueOf(totalDischarge) + "kWh", LCDConstants.NUM_COLS - 12));
            getCharacterDisplay().setCharacter(3, 11, GeneralHelper.padLeft(String.valueOf(totalEnergyConsumed) + "kWh", LCDConstants.NUM_COLS - 11));
        }
    }

    public void performAction2() {
        currentState = 2;
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction2(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        } else if (lcd == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction2(): lcd is null");
            return;
        }

        if (printHeadings.get(1)) {
            printHeadings.set(1, false);
            getCharacterDisplay().setLine(0, "^    TRIP");
            getCharacterDisplay().setLine(1, "     EFFICIENCY");
            getCharacterDisplay().setLine(2, "  ");
            getCharacterDisplay().setLine(3, "v          miles/kWh");
        }

        final double tripEfficiency = GeneralHelper.round((lcd.getTripDistance() / bmsData.getEnergyEquation().getKilowattHours()), 2);
        getCharacterDisplay().setCharacter(2, 2, GeneralHelper.padLeft(String.valueOf(tripEfficiency), LCDConstants.NUM_COLS - 2));
    }

    public void performAction3() {
        currentState = 3;
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction3(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        } else if (lcd == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction3(): lcd is null");
            return;
        }

        if (printHeadings.get(2)) {
            printHeadings.set(2, false);
            getCharacterDisplay().setLine(0, "^    TRIP");
            getCharacterDisplay().setLine(1, "     AMP-HOURS");
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, "v                   ");
        }

        final double ampHours = GeneralHelper.round((bmsData.getEnergyEquation().getKilowattHours() * 1000) / bmsData.getBmsState().getPackTotalVoltage(), 2);

        getCharacterDisplay().setCharacter(3, 2, GeneralHelper.padLeft(String.valueOf(ampHours) + "Ah", LCDConstants.NUM_COLS - 2));
    }

    public void performAction4() {
        currentState = 4;
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction4(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        } else if (lcd == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction4(): lcd is null");
            return;
        }

        if (printHeadings.get(3)) {
            printHeadings.set(3, false);
            getCharacterDisplay().setLine(0, "^  LIFETIME ENERGY");
            getCharacterDisplay().setLine(1, "  Regen  ");
            getCharacterDisplay().setLine(2, "  Discharge ");
            getCharacterDisplay().setLine(3, "v Consumed ");
        }

        final double totalEnergyConsumed = GeneralHelper.round(Double.valueOf(lcd.getSavedProperty("lifetimeEnergyConsumed")), 2);
        //total discharge (positive in the case of the bmsModel)
        final double totalDischarge = GeneralHelper.round(Double.valueOf(lcd.getSavedProperty("lifetimeEnergyDischarge")), 2);
        //total charge (regen, negative in the case of the bmsModel)
        final double totalCharge = GeneralHelper.round(Double.valueOf(lcd.getSavedProperty("lifetimeEnergyRegen")), 2);

        getCharacterDisplay().setCharacter(1, 8, GeneralHelper.padLeft(String.valueOf(totalCharge) + "kWh", LCDConstants.NUM_COLS - 8));
        getCharacterDisplay().setCharacter(2, 12, GeneralHelper.padLeft(String.valueOf(totalDischarge) + "kWh", LCDConstants.NUM_COLS - 12));
        getCharacterDisplay().setCharacter(3, 11, GeneralHelper.padLeft(String.valueOf(totalEnergyConsumed) + "kWh", LCDConstants.NUM_COLS - 11));
    }

    public void performAction5() {
        currentState = 5;
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction5(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        } else if (lcd == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction5(): lcd is null");
            return;
        }

        if (printHeadings.get(4)) {
            printHeadings.set(4, false);
            getCharacterDisplay().setLine(0, "^     LIFETIME");
            getCharacterDisplay().setLine(1, "      EFFICIENCY");
            getCharacterDisplay().setLine(2, "  ");
            getCharacterDisplay().setLine(3, "v          miles/kWh");
        }

        final double lifetimeEfficiency = GeneralHelper.round(Double.valueOf(lcd.getSavedProperty("lifetimeEfficiency")), 2);
        getCharacterDisplay().setCharacter(2, 2, GeneralHelper.padLeft(String.valueOf(lifetimeEfficiency), LCDConstants.NUM_COLS - 2));
    }

    public void performAction6() {
        currentState = 6;
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction6(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        } else if (lcd == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction6(): lcd is null");
            return;
        }

        if (printHeadings.get(5)) {
            printHeadings.set(5, false);
            getCharacterDisplay().setLine(0, "^     LIFETIME");
            getCharacterDisplay().setLine(1, "      OPERATING");
            getCharacterDisplay().setLine(2, "  Drive ");
            getCharacterDisplay().setLine(3, "v Charge ");
        }

        final double lifetimeChargingTime = GeneralHelper.round((Double.valueOf(lcd.getSavedProperty("lifetimeChargingTime")) * LCDConstants.SECONDS_TO_HOURS), 2);
        final double lifetimeDrivingTime = GeneralHelper.round((Double.valueOf(lcd.getSavedProperty("lifetimeDrivingTime")) * LCDConstants.SECONDS_TO_HOURS), 2);

        getCharacterDisplay().setCharacter(2, 9, GeneralHelper.padLeft(String.valueOf(lifetimeDrivingTime) + "hrs", LCDConstants.NUM_COLS - 9));
        getCharacterDisplay().setCharacter(3, 10, GeneralHelper.padLeft(String.valueOf(lifetimeChargingTime) + "hrs", LCDConstants.NUM_COLS - 10));
    }

    public void performAction7() {
        currentState = 7;
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction7(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        } else if (lcd == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction7(): lcd is null");
            return;
        }

        if (printHeadings.get(6)) {
            printHeadings.set(6, false);
            getCharacterDisplay().setLine(0, "^  OPERATING COSTS");
            getCharacterDisplay().setLine(1, "  Electric ");
            getCharacterDisplay().setLine(2, "  Gas ");
            getCharacterDisplay().setLine(3, "v Savings ");
        }

        double lifetimeCostEletric = lcd.getCostOfElectricity() * Double.valueOf(lcd.getSavedProperty("lifetimeEnergyConsumed"));
        double lifetimeCostGas = ((Double.valueOf(lcd.getSavedProperty("lifetimeDistanceTraveled")) / lcd.getCarMpg()) * lcd.getCostOfGas());
        final double moneySaved = GeneralHelper.round((lifetimeCostGas - lifetimeCostEletric), 2);
        lifetimeCostEletric = GeneralHelper.round(lifetimeCostEletric, 2);
        lifetimeCostGas = GeneralHelper.round(lifetimeCostGas, 2);

        getCharacterDisplay().setCharacter(1, 11, GeneralHelper.padLeft("$" + String.valueOf(lifetimeCostEletric), LCDConstants.NUM_COLS - 11));
        getCharacterDisplay().setCharacter(2, 6, GeneralHelper.padLeft("$" + String.valueOf(lifetimeCostGas), LCDConstants.NUM_COLS - 6));
        getCharacterDisplay().setCharacter(3, 11, GeneralHelper.padLeft("$" + String.valueOf(moneySaved), LCDConstants.NUM_COLS - 11));
    }

    public void upEvent() {
        if (currentState == 1) {
            currentState = 7;
            printHeadings.set(0, true);
            performAction7();
        } else if (currentState == 2) {
            currentState = 1;
            printHeadings.set(1, true);
            performAction();
        } else if (currentState == 3) {
            currentState = 2;
            printHeadings.set(2, true);
            performAction2();
        } else if (currentState == 4) {
            currentState = 3;
            printHeadings.set(3, true);
            performAction3();
        } else if (currentState == 5) {
            currentState = 4;
            printHeadings.set(4, true);
            performAction4();
        } else if (currentState == 6) {
            currentState = 5;
            printHeadings.set(5, true);
            performAction5();
        } else if (currentState == 7) {
            currentState = 6;
            printHeadings.set(6, true);
            performAction6();
        }
    }

    public void downEvent() {
        if (currentState == 1) {
            currentState = 2;
            printHeadings.set(0, true);
            performAction2();
        } else if (currentState == 2) {
            currentState = 3;
            printHeadings.set(1, true);
            performAction3();
        } else if (currentState == 3) {
            currentState = 4;
            printHeadings.set(2, true);
            performAction4();
        } else if (currentState == 4) {
            currentState = 5;
            printHeadings.set(3, true);
            performAction5();
        } else if (currentState == 5) {
            currentState = 6;
            printHeadings.set(4, true);
            performAction6();
        } else if (currentState == 6) {
            currentState = 7;
            printHeadings.set(5, true);
            performAction7();
        } else if (currentState == 7) {
            currentState = 1;
            printHeadings.set(6, true);
            performAction();
        }
    }

    protected void postActivate() {
        final int numActions = printHeadings.size();
        for (int i = 0; i < numActions; i++) {
            printHeadings.set(i, true);
        }
    }
}
