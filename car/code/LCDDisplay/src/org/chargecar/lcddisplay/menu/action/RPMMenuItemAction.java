package org.chargecar.lcddisplay.menu.action;

import java.util.concurrent.TimeUnit;
import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.RepeatingActionCharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;
import org.chargecar.lcddisplay.LCD;
import org.chargecar.lcddisplay.LCDConstants;
import org.chargecar.lcddisplay.LCDProxy;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class RPMMenuItemAction extends RepeatingActionCharacterDisplayMenuItemAction
   {
   private static final Logger LOG = Logger.getLogger(RPMMenuItemAction.class);

   public RPMMenuItemAction(final MenuItem menuItem,
                            final MenuStatusManager menuStatusManager,
                            final CharacterDisplay characterDisplay)
      {
      super(menuItem, menuStatusManager, characterDisplay, 0, 200, TimeUnit.MILLISECONDS);
      }

   @Override
   protected void preActivate()
      {
      getCharacterDisplay().setLine(0, "RPM: ");
      }

   @Override
   protected void performAction()
      {
      final LCD lcd = LCDProxy.getInstance();
      if (lcd == null)
         {
         getCharacterDisplay().setLine(0, "No connection to LCD.");
         getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
         return;
         }
      final double rpm = Math.round(lcd.getRPM() * 100.0) / 100.0;
      LOG.debug("RPMMenuItemAction.activate(): updating rpm");
      getCharacterDisplay().setCharacter(0, 5, String.valueOf(rpm));
      getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
      }
   }

