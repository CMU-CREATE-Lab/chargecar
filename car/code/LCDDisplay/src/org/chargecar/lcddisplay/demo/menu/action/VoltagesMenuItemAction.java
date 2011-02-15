package org.chargecar.lcddisplay.lcd.menu.action;

import edu.cmu.ri.createlab.LCD;
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
public final class VoltagesMenuItemAction extends CharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(VoltagesMenuItemAction.class);

    public VoltagesMenuItemAction(final MenuItem menuItem,
                                  final MenuStatusManager menuStatusManager,
                                  final CharacterDisplay characterDisplay) {
        super(menuItem, menuStatusManager, characterDisplay);
    }

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("VoltagesMenuItemAction.executor"));
    private ScheduledFuture<?> scheduledFuture = null;
    final LCD lcd = LCDProxy.getInstance();
    final SensorBoard sensorboard = SensorBoard.getInstance();

    public void activate() {
        //LOG.debug("VoltagesMenuItemAction.activate():" + SensorBoard.getInstance().getBmsAndEnergy().getBmsState().toLoggingString());

        try {
            scheduledFuture = executor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    if (sensorboard.getBmsAndEnergy() == null) {
                        LOG.debug("VoltagesMenuItemAction.run(): sensorboard is null");
                        getCharacterDisplay().setLine(0, "No connection to BMS.");
                        return;
                    }else if (lcd == null) {
                        getCharacterDisplay().setLine(0, "No connection to LCD.");
                        return;
                    }
                    double minVoltage = Math.round(sensorboard.getBmsAndEnergy().getBmsState().getMinimumCellVoltage() * 100.0) / 100.0;
                    double maxVoltage = Math.round(sensorboard.getBmsAndEnergy().getBmsState().getMaximumCellVoltage() * 100.0) / 100.0;
                    double averageVoltage = Math.round(sensorboard.getBmsAndEnergy().getBmsState().getAverageCellVoltage() * 100.0) / 100.0;

                    LOG.debug("VoltagesMenuItemAction.activate(): updating voltages");
                    lcd.setText(0, 0, String.format("%1$-" + 20 + "s", "Min Voltage: " + minVoltage));
                    lcd.setText(1, 0, String.format("%1$-" + 20 + "s", "Max Voltage: " + maxVoltage));
                    lcd.setText(2, 0, String.format("%1$-" + 20 + "s", "Average Voltage: " + averageVoltage));
                    getCharacterDisplay().setLine(0, "Min Voltage: " + minVoltage);
                    getCharacterDisplay().setLine(1, "Max Voltage: " + maxVoltage);
                    getCharacterDisplay().setLine(2, "Average Voltage: " + averageVoltage);
                }
            }, 0, 200, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // TODO: make this error message better
            LOG.error("VoltagesMenuItemAction.activate(): failed to schedule task", e);
        }
    }

    @Override
    public void deactivate() {
        if (scheduledFuture != null) {
            LOG.debug("VoltagesMenuItemAction.deactivate(): cancelling task");
            scheduledFuture.cancel(true);
        }
    }
}
