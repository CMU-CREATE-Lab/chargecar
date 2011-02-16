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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class RPMMenuItemAction extends CharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(RPMMenuItemAction.class);

    public RPMMenuItemAction(final MenuItem menuItem,
                             final MenuStatusManager menuStatusManager,
                             final CharacterDisplay characterDisplay) {
        super(menuItem, menuStatusManager, characterDisplay);
    }

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("RPMMenuItemAction.executor"));
    private ScheduledFuture<?> scheduledFuture = null;
    final LCD lcd = LCDProxy.getInstance();

    public void activate() {
        //TODO should check for lcd being null
        getCharacterDisplay().setLine(0, "RPM: ");
        try {
            scheduledFuture = executor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    if (lcd == null) {
                        getCharacterDisplay().setLine(0, "No connection to LCD.");
                        getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS-1,0," ");
                        return;
                    }
                    final double rpm = Math.round(lcd.getRPM() * 100.0) / 100.0;
                    LOG.debug("RPMMenuItemAction.activate(): updating rpm");
                    getCharacterDisplay().setCharacter(0, 5, String.valueOf(rpm));
                    getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS-1,0," ");
                }
            }, 0, 200, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // TODO: make this error message better
            LOG.error("RPMMenuItemAction.activate(): failed to schedule task", e);
        }
    }

    @Override
    public void deactivate() {
        if (scheduledFuture != null) {
            LOG.debug("RPMMenuItemAction.deactivate(): cancelling task");
            scheduledFuture.cancel(true);
        }
    }
}

