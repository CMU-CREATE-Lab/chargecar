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
public final class DrivingModeMenuItemAction extends RepeatingActionCharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(DrivingModeMenuItemAction.class);
    List<Double> previousLatLng = null;
    LCD lcd = null;
    BMSManager bmsManager = null;
    BMSAndEnergy bmsData = null;
    GPSManager gpsManager = null;
    GPSEvent gpsData = null;
    int currentState = 1;

    public DrivingModeMenuItemAction(final MenuItem menuItem,
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
            LOG.error("DrivingModeMenuItemAction.performAction(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
            return;
        } else if (lcd == null) {
            LOG.error("DrivingModeMenuItemAction.performAction(): lcd is null");
            return;
        }

        if (currentState == 2)
            performAction2();
        else if (currentState == 3)
            performAction3();
        else if (currentState == 4)
            performAction4();
        else {
            currentState = 1;
            //instantaneous
            double powerFlowInKw = (bmsData.getBmsState().getPackTotalVoltage() * bmsData.getBmsState().getLoadCurrentAmps()) / 1000;
            powerFlowInKw = Math.round(powerFlowInKw * 100.0) / 100.0;
            double currentEfficiency = ((bmsData.getEnergyEquation().getKilowattHours() * lcd.getTripDistance()) * 100.0) / 100.0;
            currentEfficiency = Math.round(currentEfficiency * 100.0) / 100.0;
            LOG.trace("DrivingModeMenuItemAction.performAction(): updating kwhMeter");
            getCharacterDisplay().setLine(0, "^| Charge " + GeneralHelper.padLeft(bmsData.getBmsState().getStateOfChargePercentage() + "%", LCDConstants.NUM_COLS - 10));
            getCharacterDisplay().setLine(1, " | Pwr Flow " + GeneralHelper.padLeft(powerFlowInKw + "kW", LCDConstants.NUM_COLS - 12));
            getCharacterDisplay().setLine(2, " | Efficiency " + GeneralHelper.padLeft(String.valueOf(currentEfficiency), LCDConstants.NUM_COLS - 14));
            getCharacterDisplay().setLine(3, "v|         miles/kWh");

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
            LOG.error("DrivingModeMenuItemAction.performAction2(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
            return;
        } else if (lcd == null) {
            LOG.error("DrivingModeMenuItemAction.performAction2(): lcd is null");
            return;
        }


        getCharacterDisplay().setLine(0, "^|" + "GENRL TEMPERATURES");
        getCharacterDisplay().setLine(1, " | " + "Motor " + GeneralHelper.padLeft(Math.round((lcd.getTemperatureInCelsius(lcd.getMotorTemperatureInKelvin()) * 100.0) / 100.0) + "C", LCDConstants.NUM_COLS - 9));
        getCharacterDisplay().setLine(2, " | " + "Controller " + GeneralHelper.padLeft(Math.round((lcd.getTemperatureInCelsius(lcd.getControllerTemperatureInKelvin()) * 100.0) / 100.0) + "C", LCDConstants.NUM_COLS - 14));
        getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, "v|                  ");
    }

    protected void performAction3() {
        currentState = 3;
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();
        gpsManager = GPSManager.getInstance();
        gpsData = (gpsManager == null) ? null : gpsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DrivingModeMenuItemAction.performAction2(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
            return;
        } else if (lcd == null) {
            LOG.error("DrivingModeMenuItemAction.performAction2(): lcd is null");
            return;
        }


        getCharacterDisplay().setLine(0, "^|" + "BATT TEMPERATURES");
        getCharacterDisplay().setLine(1, " | " + "Avg Temp " + GeneralHelper.padLeft((Math.round(bmsData.getBmsState().getAverageCellBoardTemp() * 100.0) / 100.0) + "C", LCDConstants.NUM_COLS - 12));
        getCharacterDisplay().setLine(2, " | " + "Min Temp " + GeneralHelper.padLeft((Math.round(bmsData.getBmsState().getMinimumCellBoardTemp() * 100.0) / 100.0) + "C", LCDConstants.NUM_COLS - 12));
        getCharacterDisplay().setLine(3, "v| " + "Max Temp " + GeneralHelper.padLeft((Math.round(bmsData.getBmsState().getMinimumCellBoardTemp() * 100.0) / 100.0) + "C", LCDConstants.NUM_COLS - 12));
    }

    protected void performAction4() {
        currentState = 4;
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();
        gpsManager = GPSManager.getInstance();
        gpsData = (gpsManager == null) ? null : gpsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DrivingModeMenuItemAction.performAction2(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
            return;
        } else if (lcd == null) {
            LOG.error("DrivingModeMenuItemAction.performAction2(): lcd is null");
            return;
        }


        getCharacterDisplay().setLine(0, "^|" + "    VOLTAGES");
        getCharacterDisplay().setLine(1, " | " + "Avg Voltage " + GeneralHelper.padLeft(String.valueOf(Math.round(bmsData.getBmsState().getAverageCellVoltage() * 100.0) / 100.0), LCDConstants.NUM_COLS - 14));
        getCharacterDisplay().setLine(2, " | " + "Min Voltage " + GeneralHelper.padLeft(String.valueOf(Math.round(bmsData.getBmsState().getMinimumCellVoltage() * 100.0) / 100.0), LCDConstants.NUM_COLS - 15));
        getCharacterDisplay().setLine(3, "v| " + "Max Voltage " + GeneralHelper.padLeft(String.valueOf(Math.round(bmsData.getBmsState().getMaximumCellVoltage() * 100.0) / 100.0), LCDConstants.NUM_COLS - 15));
    }

    public void upEvent() {
        if (currentState == 1) {
            currentState = 4;
            performAction4();
        } else if (currentState == 2) {
            currentState = 1;
            performAction();
        } else if (currentState == 3) {
            currentState = 2;
            performAction2();
        } else if (currentState == 4) {
            currentState = 3;
            performAction3();
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
            currentState = 1;
            performAction();
        }
    }
}