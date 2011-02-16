package org.chargecar.lcddisplay.lcd.menu.action;

import edu.cmu.ri.createlab.LCD;
import edu.cmu.ri.createlab.LCDConstants;
import edu.cmu.ri.createlab.LCDProxy;
import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.CharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.log4j.Logger;
import org.chargecar.lcddisplay.lcd.SensorBoard;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class CurrentsMenuItemAction extends CharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(CurrentsMenuItemAction.class);

    public CurrentsMenuItemAction(final MenuItem menuItem,
                                  final MenuStatusManager menuStatusManager,
                                  final CharacterDisplay characterDisplay) {
        super(menuItem, menuStatusManager, characterDisplay);
    }

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("VoltagesMenuItemAction.executor"));
    private ScheduledFuture<?> scheduledFuture = null;
    final LCD lcd = LCDProxy.getInstance();
    final SensorBoard sensorboard = SensorBoard.getInstance();

    public void activate() {
        try {
            scheduledFuture = executor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    if (sensorboard == null || sensorboard.getBmsAndEnergy() == null) {
                        LOG.debug("CurrentsMenuItemAction.run(): bms is null");
                        getCharacterDisplay().setLine(0, "No connection to BMS.");
                        getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
                        return;
                    } else if (lcd == null) {
                        getCharacterDisplay().setLine(0, "No connection to LCD.");
                        getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
                        return;
                    }
                    final double loadCurrent = Math.round(sensorboard.getBmsAndEnergy().getBmsState().getLoadCurrentAmps() * 100.0) / 100.0;
                    final double sourceCurrent = Math.round(sensorboard.getBmsAndEnergy().getBmsState().getSourceCurrentAmps() * 100.0) / 100.0;

                    LOG.debug("CurrentsMenuItemAction.activate(): updating voltages");
                    getCharacterDisplay().setLine(0, "Load Current: " + loadCurrent);
                    getCharacterDisplay().setLine(1, "Source Current: " + sourceCurrent);
                    getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
                }
            }, 0, 200, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // TODO: make this error message better
            LOG.error("CurrentsMenuItemAction.activate(): failed to schedule task", e);
        }
    }

    @Override
    public void deactivate() {
        if (scheduledFuture != null) {
            LOG.debug("CurrentsMenuItemAction.deactivate(): cancelling task");
            scheduledFuture.cancel(true);
        }
    }
}

