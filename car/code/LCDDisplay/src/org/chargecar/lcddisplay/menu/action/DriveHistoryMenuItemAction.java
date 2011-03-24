package org.chargecar.lcddisplay.menu.action;

import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.RepeatingActionCharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;
import org.chargecar.honda.bms.BMSAndEnergy;
import org.chargecar.honda.gps.GPSEvent;
import org.chargecar.lcddisplay.*;
import org.chargecar.lcddisplay.helpers.GeneralHelper;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class DriveHistoryMenuItemAction extends RepeatingActionCharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(DriveHistoryMenuItemAction.class);
    List<Double> previousLatLng = null;
    LCD lcd = null;
    BMSManager bmsManager = null;
    BMSAndEnergy bmsData = null;
    GPSManager gpsManager = null;
    GPSEvent gpsData = null;
    int currentState = 1;
    double costOfEnergy = .11;
    double civicMpg = 31;
    double averageGasPrice = 3.532; //in PA on 3/24/2011

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
        gpsManager = GPSManager.getInstance();
        gpsData = (gpsManager == null) ? null : gpsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
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
        else {
            double totalEnergyConsumed = Math.round(bmsData.getEnergyEquation().getKilowattHours() * 100.0) / 100.0;
            //total discharge (positive in the case of the bmsModel)
            double totalDischarge = Math.round(bmsData.getEnergyEquation().getKilowattHoursUsed() * 100.0) / 100.0;
            //total charge (regen, negative in the case of the bmsModel)
            double totalCharge = Math.round(bmsData.getEnergyEquation().getKilowattHoursRegen() * 100.0) / 100.0;

            getCharacterDisplay().setLine(0, "^|" + "    TRIP ENERGY");
            getCharacterDisplay().setLine(1, " | " + "Regen " + GeneralHelper.padLeft(String.valueOf(totalCharge) + "kWh", LCDConstants.NUM_COLS - 9));
            getCharacterDisplay().setLine(2, " | " + "Discharge " + GeneralHelper.padLeft(String.valueOf(totalDischarge) + "kWh", LCDConstants.NUM_COLS - 13));
            getCharacterDisplay().setLine(3, "v| " + "Consumed " + GeneralHelper.padLeft(String.valueOf(totalEnergyConsumed) + "kWh", LCDConstants.NUM_COLS - 12));

            currentState = 1;
        }
    }

    protected void performAction2() {
        currentState = 2;
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();
        gpsManager = GPSManager.getInstance();
        gpsData = (gpsManager == null) ? null : gpsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction2(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
            return;
        } else if (lcd == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction2(): lcd is null");
            return;
        }


        double totalEnergyConsumed = Math.round(Double.valueOf(lcd.getSavedProperty("lifetimeEnergyConsumed")) * 100.0) / 100.0;
        //total discharge (positive in the case of the bmsModel)
        double totalDischarge = Math.round(Double.valueOf(lcd.getSavedProperty("lifetimeEnergyDischarge")) * 100.0) / 100.0;
        //total charge (regen, negative in the case of the bmsModel)
        double totalCharge = Math.round(Double.valueOf(lcd.getSavedProperty("lifetimeEnergyRegen")) * 100.0) / 100.0;

        getCharacterDisplay().setLine(0, "^|" + "  LIFETIME ENERGY");
        getCharacterDisplay().setLine(1, " | " + "Regen " + GeneralHelper.padLeft(String.valueOf(totalCharge) + "kWh", LCDConstants.NUM_COLS - 9));
        getCharacterDisplay().setLine(2, " | " + "Discharge " + GeneralHelper.padLeft(String.valueOf(totalDischarge) + "kWh", LCDConstants.NUM_COLS - 13));
        getCharacterDisplay().setLine(3, "v| " + "Consumed " + GeneralHelper.padLeft(String.valueOf(totalEnergyConsumed) + "kWh", LCDConstants.NUM_COLS - 12));

    }

    protected void performAction3() {
        currentState = 3;
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();
        gpsManager = GPSManager.getInstance();
        gpsData = (gpsManager == null) ? null : gpsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction3(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
            return;
        } else if (lcd == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction3(): lcd is null");
            return;
        }


        double lifetimeEfficiency = Math.round(Double.valueOf(lcd.getSavedProperty("lifetimeEfficiency")) * 100.0) / 100.0;

        getCharacterDisplay().setLine(0, "^|" + "    LIFETIME");
        getCharacterDisplay().setLine(1, " |" + "    EFFICIENCY");
        getCharacterDisplay().setLine(2, " |" + GeneralHelper.padLeft(String.valueOf(lifetimeEfficiency), LCDConstants.NUM_COLS - 2));
        getCharacterDisplay().setLine(3, "v|         miles/kWh");
    }

    protected void performAction4() {
        currentState = 4;
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();
        gpsManager = GPSManager.getInstance();
        gpsData = (gpsManager == null) ? null : gpsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction4(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
            return;
        } else if (lcd == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction4(): lcd is null");
            return;
        }


        double lifetimeChargingTime = Math.round((Double.valueOf(lcd.getSavedProperty("lifetimeChargingTime")) * LCDConstants.SECONDS_TO_HOURS) * 100.0) / 100.0;
        double lifetimeDrivingTime = Math.round((Double.valueOf(lcd.getSavedProperty("lifetimeDrivingTime")) * LCDConstants.SECONDS_TO_HOURS) * 100.0) / 100.0;

        getCharacterDisplay().setLine(0, "^|" + "    LIFETIME");
        getCharacterDisplay().setLine(1, " |" + "    OPERATING");
        getCharacterDisplay().setLine(2, " |" + " Drive " + GeneralHelper.padLeft(String.valueOf(lifetimeDrivingTime) + "hrs", LCDConstants.NUM_COLS - 9));
        getCharacterDisplay().setLine(3, "v|" + " Charge " + GeneralHelper.padLeft(String.valueOf(lifetimeChargingTime) + "hrs", LCDConstants.NUM_COLS - 10));
    }

    protected void performAction5() {
        currentState = 5;
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();
        gpsManager = GPSManager.getInstance();
        gpsData = (gpsManager == null) ? null : gpsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction5(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
            return;
        } else if (lcd == null) {
            LOG.error("DriveHistoryMenuItemAction.performAction5(): lcd is null");
            return;
        }


        double lifetimeCostEletric = Math.round((costOfEnergy * Double.valueOf(lcd.getSavedProperty("lifetimeEnergyConsumed")) * 100.0) / 100.0);
        double lifetimeCostGas = ((((Double.valueOf(lcd.getSavedProperty("lifetimeDistanceTraveled")) / civicMpg) * averageGasPrice) * 100.0) / 100.0);

        getCharacterDisplay().setLine(0, "^|" + "    OPERATING");
        getCharacterDisplay().setLine(1, " |" + "    COSTS");
        getCharacterDisplay().setLine(2, " |" + "Electric " + GeneralHelper.padLeft("$" + String.valueOf(lifetimeCostEletric), LCDConstants.NUM_COLS - 11));
        getCharacterDisplay().setLine(3, "v|" + "Gas " + GeneralHelper.padLeft("$" + String.valueOf(lifetimeCostGas), LCDConstants.NUM_COLS - 6));


        //@money_saved = (@gas_price - @electricity_price).round(2)
	    //@percent_saved = ((@money_saved / @gas_price).round(2) * 100).to_i

    }

    public void upEvent() {
        if (currentState == 1) {
            currentState = 5;
            performAction5();
        } else if (currentState == 2) {
            currentState = 1;
            performAction();
        } else if (currentState == 3) {
            currentState = 2;
            performAction2();
        } else if (currentState == 4) {
            currentState = 3;
            performAction3();
        } else if (currentState == 5) {
            currentState = 4;
            performAction4();
        }
    }

    public void downEvent() {
        if (currentState == 1) {
            currentState = 2;
            performAction2();
        } else if (currentState == 2) {
            currentState = 3;
            performAction3();
        } else if (currentState == 3) {
            currentState = 4;
            performAction4();
        } else if (currentState == 4) {
            currentState = 5;
            performAction5();
        } else if (currentState == 5) {
            currentState = 1;
            performAction();
        }
    }
}
