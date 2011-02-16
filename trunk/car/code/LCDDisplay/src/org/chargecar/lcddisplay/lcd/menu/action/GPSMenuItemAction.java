package org.chargecar.lcddisplay.lcd.menu.action;

import java.util.concurrent.TimeUnit;
import edu.cmu.ri.createlab.LCD;
import edu.cmu.ri.createlab.LCDConstants;
import edu.cmu.ri.createlab.LCDProxy;
import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.RepeatingActionCharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;
import org.chargecar.lcddisplay.lcd.SensorBoard;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class GPSMenuItemAction extends RepeatingActionCharacterDisplayMenuItemAction
   {
   private static final Logger LOG = Logger.getLogger(GPSMenuItemAction.class);

   public GPSMenuItemAction(final MenuItem menuItem,
                            final MenuStatusManager menuStatusManager,
                            final CharacterDisplay characterDisplay)
      {
      super(menuItem, menuStatusManager, characterDisplay, 0, 200, TimeUnit.MILLISECONDS);
      }

   @Override
   protected void performAction()
      {
      final LCD lcd = LCDProxy.getInstance();
      final SensorBoard sensorboard = SensorBoard.getInstance();
      if (sensorboard == null || sensorboard.getGpsEvent() == null)
         {
         LOG.debug("GPSMenuItemAction.run(): gps is null");
         getCharacterDisplay().setLine(0, "No connection to GPS.");
         getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
         return;
         }
      else if (lcd == null)
         {
         LOG.debug("GPSMenuItemAction.run(): lcd is null");
         getCharacterDisplay().setLine(0, "No connection to LCD.");
         getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
         return;
         }
      final String lat = sensorboard.getGpsEvent().getLatitude();
      final String lng = sensorboard.getGpsEvent().getLongitude();
      final Integer elevation = sensorboard.getGpsEvent().getElevationInFeet();

      LOG.debug("GPSMenuItemAction.activate(): updating GPS data");
      getCharacterDisplay().setLine(0, "Latitude: " + lat);
      getCharacterDisplay().setLine(1, "Longitude: " + lng);
      getCharacterDisplay().setLine(2, "Elevation: " + elevation);
      getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
      }
   }
