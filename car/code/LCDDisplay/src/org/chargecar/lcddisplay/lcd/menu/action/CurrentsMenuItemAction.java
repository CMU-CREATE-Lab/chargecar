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
public final class CurrentsMenuItemAction extends RepeatingActionCharacterDisplayMenuItemAction
   {
   private static final Logger LOG = Logger.getLogger(CurrentsMenuItemAction.class);

   public CurrentsMenuItemAction(final MenuItem menuItem,
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
      if (sensorboard == null || sensorboard.getBmsAndEnergy() == null)
         {
         LOG.debug("CurrentsMenuItemAction.run(): bms is null");
         getCharacterDisplay().setLine(0, "No connection to BMS.");
         getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
         return;
         }
      else if (lcd == null)
         {
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
   }

