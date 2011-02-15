package org.chargecar.lcddisplay.lcd.menu.action;

import edu.cmu.ri.createlab.LCD;
import edu.cmu.ri.createlab.LCDProxy;
import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.CharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class TemperaturesMenuItemAction extends CharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(TemperaturesMenuItemAction.class);

    public TemperaturesMenuItemAction(final MenuItem menuItem,
                                         final MenuStatusManager menuStatusManager,
                                         final CharacterDisplay characterDisplay) {
        super(menuItem, menuStatusManager, characterDisplay);
    }

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("TemperaturesMenuItemAction.executor"));
    private ScheduledFuture<?> scheduledFuture = null;
    final LCD lcd = LCDProxy.getInstance();

    public void activate() {
        lcd.setText(0,0,String.format("%1$-" + 20 + "s", "Motor"));
        lcd.setText(1,0,String.format("%1$-" + 20 + "s", "Temperature: "));
        lcd.setText(2,0,String.format("%1$-" + 20 + "s", "Controller"));
        lcd.setText(3,0,String.format("%1$-" + 20 + "s", "Temperature: "));
        getCharacterDisplay().setLine(0, "Motor");
        getCharacterDisplay().setLine(1, "Temperature: ");
        getCharacterDisplay().setLine(2, "Controller");
        getCharacterDisplay().setLine(3, "Temperature: ");
        try {
            scheduledFuture = executor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    if (lcd == null) {
                        getCharacterDisplay().setLine(0, "No connection to LCD.");
                        return;
                    }
                    double motorTemp = Math.round(lcd.getMotorTemperatureInKelvin()*100.0) / 100.0;
                    double controllerTemp = Math.round(lcd.getControllerTemperatureInKelvin()*100.0) / 100.0;

                    LOG.debug("TemperaturesMenuItemAction.activate(): updating temperatures");
                    lcd.setText(1,13,String.format("%1$-" + 7 + "s", motorTemp));
                    lcd.setText(3,13,String.format("%1$-" + 7 + "s", controllerTemp));
                    getCharacterDisplay().setCharacter(1, 13, String.valueOf(motorTemp));
                    getCharacterDisplay().setCharacter(3, 13, String.valueOf(controllerTemp));
                }
            }, 0, 200, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // TODO: make this error message better
            LOG.error("TemperaturesMenuItemAction.activate(): failed to schedule task", e);
        }
    }

    @Override
    public void deactivate() {
        if (scheduledFuture != null) {
            LOG.debug("TemperaturesMenuItemAction.deactivate(): cancelling task");
            scheduledFuture.cancel(true);
        }
    }
}