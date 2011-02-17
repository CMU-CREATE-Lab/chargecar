package org.chargecar.lcddisplay.menu.action;

import java.util.concurrent.TimeUnit;
import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.RepeatingActionCharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;
import org.chargecar.honda.gps.GPSEvent;
import org.chargecar.lcddisplay.GPSManager;
import org.chargecar.lcddisplay.LCD;
import org.chargecar.lcddisplay.LCDConstants;
import org.chargecar.lcddisplay.LCDProxy;

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
      final GPSManager manager = GPSManager.getInstance();
      final GPSEvent data = (manager == null) ? null : manager.getData();
      
      if (manager == null || data == null)
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
      final String lat = data.getLatitude();
      final String lng = data.getLongitude();
      final Integer elevation = data.getElevationInFeet();

      LOG.debug("GPSMenuItemAction.activate(): updating GPS data");
      getCharacterDisplay().setLine(0, "Latitude: " + lat);
      getCharacterDisplay().setLine(1, "Longitude: " + lng);
      getCharacterDisplay().setLine(2, "Elevation: " + elevation);
      getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
      }
   }
