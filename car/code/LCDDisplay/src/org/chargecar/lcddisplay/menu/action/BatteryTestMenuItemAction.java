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

import java.util.concurrent.TimeUnit;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class BatteryTestMenuItemAction extends RepeatingActionCharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(GPSMenuItemAction.class);

    double previousLoadCurrent;
    double previousTotalEnergyConsumed;
    double previousAmpHours;
    double previousMaxVoltage;
    double previousMinVoltage;

    double totalEnergyConsumed = 0;
    double ampHours = 0;

    public BatteryTestMenuItemAction(final MenuItem menuItem,
                                     final MenuStatusManager menuStatusManager,
                                     final CharacterDisplay characterDisplay) {
        super(menuItem, menuStatusManager, characterDisplay, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void performAction() {
        final LCD lcd = LCDProxy.getInstance();
        BMSManager bmsManager = BMSManager.getInstance();
        BMSAndEnergy bmsData = (bmsManager == null) ? null : bmsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("GPSMenuItemAction.performAction(): gps is null");
            getCharacterDisplay().setLine(0, "No connection to GPS.");
            getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
            return;
        } else if (lcd == null) {
            LOG.error("GPSMenuItemAction.performAction(): lcd is null");
            return;
        }

        final double loadCurrent = GeneralHelper.round(bmsData.getBmsState().getLoadCurrentAmps(), 2);
        totalEnergyConsumed += GeneralHelper.round(bmsData.getEnergyEquation().getKilowattHoursDelta(), 2);
        ampHours += GeneralHelper.round((totalEnergyConsumed * 1000) / bmsData.getBmsState().getPackTotalVoltage(), 2);
        final double maxVoltage = GeneralHelper.round(bmsData.getBmsState().getMaximumCellVoltage(), 2);
        final double minVoltage = GeneralHelper.round(bmsData.getBmsState().getMinimumCellVoltage(), 2);

        getCharacterDisplay().setCharacter(0, 0, "BATT TEST");

        if (bmsData.getBmsState().getPackTotalVoltage() < 82.5 || bmsData.getBmsState().getMinimumCellVoltage() < 2.00) {
            getCharacterDisplay().setCharacter(0, 9, GeneralHelper.padLeft(String.valueOf(previousLoadCurrent) + " amps", LCDConstants.NUM_COLS - 9));
            getCharacterDisplay().setCharacter(1, 0, GeneralHelper.padLeft(String.valueOf(previousTotalEnergyConsumed) + " kWh", LCDConstants.NUM_COLS));
            getCharacterDisplay().setCharacter(2, 0, GeneralHelper.padLeft(String.valueOf(previousAmpHours) + " Ah", LCDConstants.NUM_COLS));
            getCharacterDisplay().setCharacter(3, 0, GeneralHelper.padLeft(String.valueOf(previousMinVoltage) + "/" + String.valueOf(previousMaxVoltage), LCDConstants.NUM_COLS));
        } else {
            previousLoadCurrent = loadCurrent;
            previousTotalEnergyConsumed = totalEnergyConsumed;
            previousAmpHours = ampHours;
            previousMaxVoltage = maxVoltage;
            previousMinVoltage = minVoltage;
            getCharacterDisplay().setCharacter(0, 9, GeneralHelper.padLeft(String.valueOf(loadCurrent) + " amps", LCDConstants.NUM_COLS - 9));
            getCharacterDisplay().setCharacter(1, 0, GeneralHelper.padLeft(String.valueOf(totalEnergyConsumed) + " kWh", LCDConstants.NUM_COLS));
            getCharacterDisplay().setCharacter(2, 0, GeneralHelper.padLeft(String.valueOf(ampHours) + " Ah", LCDConstants.NUM_COLS));
            getCharacterDisplay().setCharacter(3, 0, GeneralHelper.padLeft(String.valueOf(minVoltage) + "/" + String.valueOf(maxVoltage), LCDConstants.NUM_COLS));
        }
    }
}