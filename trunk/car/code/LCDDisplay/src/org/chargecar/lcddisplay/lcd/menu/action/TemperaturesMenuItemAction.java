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

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class TemperaturesMenuItemAction extends RepeatingActionCharacterDisplayMenuItemAction
   {
   private static final Logger LOG = Logger.getLogger(TemperaturesMenuItemAction.class);

   public TemperaturesMenuItemAction(final MenuItem menuItem,
                                     final MenuStatusManager menuStatusManager,
                                     final CharacterDisplay characterDisplay)
      {
      super(menuItem, menuStatusManager, characterDisplay, 0, 200, TimeUnit.MILLISECONDS);
      }

   @Override
   protected void preActivate()
      {
      getCharacterDisplay().setLine(0, "Motor");
      getCharacterDisplay().setLine(1, "Temperature: ");
      getCharacterDisplay().setLine(2, "Controller");
      getCharacterDisplay().setLine(3, "Temperature: ");
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
      final double motorTemp = Math.round(lcd.getMotorTemperatureInKelvin() * 100.0) / 100.0;
      final double controllerTemp = Math.round(lcd.getControllerTemperatureInKelvin() * 100.0) / 100.0;

      LOG.debug("TemperaturesMenuItemAction.activate(): updating temperatures");
      getCharacterDisplay().setCharacter(1, 13, String.valueOf(motorTemp));
      getCharacterDisplay().setCharacter(3, 13, String.valueOf(controllerTemp));
      }
   }
