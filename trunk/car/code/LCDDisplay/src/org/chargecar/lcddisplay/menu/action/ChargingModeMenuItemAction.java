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
public final class ChargingModeMenuItemAction extends RepeatingActionCharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(ChargingModeMenuItemAction.class);
    List<Double> previousLatLng = null;
    LCD lcd = null;
    BMSManager bmsManager = null;
    BMSAndEnergy bmsData = null;
    GPSManager gpsManager = null;
    GPSEvent gpsData = null;
    int currentState = 1;

    public ChargingModeMenuItemAction(final MenuItem menuItem,
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
            LOG.error("ChargingModeMenuItemAction.performAction(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        } else if (lcd == null) {
            LOG.error("ChargingModeMenuItemAction.performAction(): lcd is null");
            return;
        }

        if (currentState == 2)
            performAction2();
        else if (currentState == 3)
            performAction3();
        else {
            //instantaneous
            currentState = 1;
            LOG.trace("ChargingModeMenuItemAction.performAction(): updating kwhMeter");
            getCharacterDisplay().setLine(0, "^ Charge " + GeneralHelper.padLeft(bmsData.getBmsState().getStateOfChargePercentage() / 2 + "%", LCDConstants.NUM_COLS - 9));
            getCharacterDisplay().setLine(1, "  " + "Src Curnt " + GeneralHelper.padLeft(String.valueOf(GeneralHelper.round(bmsData.getBmsState().getSourceCurrentAmps(), 2)), LCDConstants.NUM_COLS - 12));
            getCharacterDisplay().setLine(2, "  Charging Time " + GeneralHelper.padLeft(String.valueOf(GeneralHelper.round(lcd.getChargingTime() * LCDConstants.SECONDS_TO_HOURS, 2)), LCDConstants.NUM_COLS - 16));
            getCharacterDisplay().setLine(3, "v                hrs");
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
            LOG.error("ChargingModeMenuItemAction.performAction2(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        } else if (lcd == null) {
            LOG.error("ChargingModeMenuItemAction.performAction2(): lcd is null");
            return;
        }
        getCharacterDisplay().setLine(0, "^ " + "BATT TEMPERATURES");
        getCharacterDisplay().setLine(1, "  " + "Avg Temp " + GeneralHelper.padLeft((GeneralHelper.round(bmsData.getBmsState().getAverageCellBoardTemp(), 2)) + "C", LCDConstants.NUM_COLS - 11));
        getCharacterDisplay().setLine(2, "  " + "Min Temp " + GeneralHelper.padLeft((GeneralHelper.round(bmsData.getBmsState().getMinimumCellBoardTemp(), 2)) + "C", LCDConstants.NUM_COLS - 11));
        getCharacterDisplay().setLine(3, "v " + "Max Temp " + GeneralHelper.padLeft((GeneralHelper.round(bmsData.getBmsState().getMaximumCellBoardTemp(), 2)) + "C", LCDConstants.NUM_COLS - 11));
    }

    protected void performAction3() {
        currentState = 3;
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();
        gpsManager = GPSManager.getInstance();
        gpsData = (gpsManager == null) ? null : gpsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("ChargingModeMenuItemAction.performAction2(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        } else if (lcd == null) {
            LOG.error("ChargingModeMenuItemAction.performAction2(): lcd is null");
            return;
        }
        getCharacterDisplay().setLine(0, "^ " + "     VOLTAGES");
        getCharacterDisplay().setLine(1, "  " + "Avg Voltage " + GeneralHelper.padLeft(String.valueOf(GeneralHelper.round(bmsData.getBmsState().getAverageCellVoltage(), 2)), LCDConstants.NUM_COLS - 14));
        getCharacterDisplay().setLine(2, "  " + "Min Voltage " + GeneralHelper.padLeft(String.valueOf(GeneralHelper.round(bmsData.getBmsState().getMinimumCellVoltage(), 2)), LCDConstants.NUM_COLS - 14));
        getCharacterDisplay().setLine(3, "v " + "Max Voltage " + GeneralHelper.padLeft(String.valueOf(GeneralHelper.round(bmsData.getBmsState().getMaximumCellVoltage(), 2)), LCDConstants.NUM_COLS - 14));
    }

    public void upEvent() {
        if (currentState == 1) {
            currentState = 3;
            performAction3();
        } else if (currentState == 2) {
            currentState = 1;
            performAction();
        } else if (currentState == 3) {
            currentState = 2;
            performAction2();
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
            currentState = 1;
            performAction();
        }
    }
}
