package org.chargecar.lcddisplay.menu.action;

import java.util.concurrent.TimeUnit;
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
      final BMSManager manager = BMSManager.getInstance();
      final BMSAndEnergy data = (manager == null) ? null : manager.getData();

      if (manager == null || data == null)
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
      final double loadCurrent = Math.round(data.getBmsState().getLoadCurrentAmps() * 100.0) / 100.0;
      final double sourceCurrent = Math.round(data.getBmsState().getSourceCurrentAmps() * 100.0) / 100.0;

      LOG.debug("CurrentsMenuItemAction.activate(): updating currents");
      getCharacterDisplay().setLine(0, "Load Current: " + loadCurrent);
      getCharacterDisplay().setLine(1, "Source Current: " + sourceCurrent);
      getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
      }
   }

