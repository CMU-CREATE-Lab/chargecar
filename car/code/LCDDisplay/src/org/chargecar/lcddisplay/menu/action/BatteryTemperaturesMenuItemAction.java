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

import java.util.concurrent.TimeUnit;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class BatteryTemperaturesMenuItemAction extends RepeatingActionCharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(CurrentsMenuItemAction.class);

    public BatteryTemperaturesMenuItemAction(final MenuItem menuItem,
                                  final MenuStatusManager menuStatusManager,
                                  final CharacterDisplay characterDisplay) {
        super(menuItem, menuStatusManager, characterDisplay, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void performAction() {
        final LCD lcd = LCDProxy.getInstance();
        final BMSManager manager = BMSManager.getInstance();
        final BMSAndEnergy data = (manager == null) ? null : manager.getData();

        if (manager == null || data == null) {
            LOG.error("BatteryTemperaturesMenuItemAction.performAction(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
            return;
        } else if (lcd == null) {
            LOG.error("BatteryTemperaturesMenuItemAction.performAction(): lcd is null");
            return;
        }
        final double minCellTemp = Math.round(data.getBmsState().getMinimumCellBoardTemp() * 100.0) / 100.0;
        final double maxCellTemp = Math.round(data.getBmsState().getMaximumCellBoardTemp() * 100.0) / 100.0;
        final double avgCellTemp = Math.round(data.getBmsState().getAverageCellBoardTemp() * 100.0) / 100.0;

        LOG.trace("BatteryTemperaturesMenuItemAction.activate(): updating battery temps");
        getCharacterDisplay().setLine(0, "Min Batt Temp: " + minCellTemp);
        getCharacterDisplay().setLine(1, "Max Batt Temp: " + maxCellTemp);
        getCharacterDisplay().setLine(2, "Avg Batt Temp: " + avgCellTemp);
        getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
    }
}

