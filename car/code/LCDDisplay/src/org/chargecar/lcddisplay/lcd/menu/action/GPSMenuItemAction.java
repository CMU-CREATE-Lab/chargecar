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
public final class GPSMenuItemAction extends CharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(GPSMenuItemAction.class);

    public GPSMenuItemAction(final MenuItem menuItem,
                             final MenuStatusManager menuStatusManager,
                             final CharacterDisplay characterDisplay) {
        super(menuItem, menuStatusManager, characterDisplay);
    }

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("GPSMenuItemAction.executor"));
    private ScheduledFuture<?> scheduledFuture = null;
    final LCD lcd = LCDProxy.getInstance();
    final SensorBoard sensorboard = SensorBoard.getInstance();

    public void activate() {
        LOG.debug("GPSMenuItemAction.activate(): first entering activate.");
        try {
            scheduledFuture = executor.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {

                    if (sensorboard == null || sensorboard.getGpsEvent() == null) {
                        LOG.debug("GPSMenuItemAction.run(): gps is null");
                        getCharacterDisplay().setLine(0, "No connection to GPS.");
                        getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS-1,0," ");
                        return;
                    }else if (lcd == null) {
                        LOG.debug("GPSMenuItemAction.run(): lcd is null");
                        getCharacterDisplay().setLine(0, "No connection to LCD.");
                        getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS-1,0," ");
                        return;
                    }
                    final String lat = sensorboard.getGpsEvent().getLatitude();
                    final String lng = sensorboard.getGpsEvent().getLongitude();
                    final Integer elevation = sensorboard.getGpsEvent().getElevationInFeet();

                    LOG.debug("GPSMenuItemAction.activate(): updating GPS data");
                    getCharacterDisplay().setLine(0, "Latitude: " + lat);
                    getCharacterDisplay().setLine(1, "Longitude: " + lng);
                    getCharacterDisplay().setLine(2, "Elevation: " + elevation);
                    getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS-1,0," ");
                }
            }, 0, 200, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // TODO: make this error message better
            LOG.error("GPSMenuItemAction.activate(): failed to schedule task", e);
        }
    }

    @Override
    public void deactivate() {
        if (scheduledFuture != null) {
            LOG.debug("GPSMenuItemAction.deactivate(): cancelling task");
            scheduledFuture.cancel(true);
        }
    }
}
