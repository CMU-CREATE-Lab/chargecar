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
import org.chargecar.lcddisplay.SensorBoard;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class VoltagesMenuItemAction extends RepeatingActionCharacterDisplayMenuItemAction
   {
   private static final Logger LOG = Logger.getLogger(VoltagesMenuItemAction.class);

   public VoltagesMenuItemAction(final MenuItem menuItem,
                                 final MenuStatusManager menuStatusManager,
                                 final CharacterDisplay characterDisplay)
      {
      super(menuItem, menuStatusManager, characterDisplay, 0, 1, TimeUnit.SECONDS);
      }

   @Override
   protected void performAction()
      {
      final LCD lcd = LCDProxy.getInstance();
      final SensorBoard sensorboard = SensorBoard.getInstance();
      if (sensorboard == null || sensorboard.getBmsAndEnergy() == null)
         {
         LOG.debug("VoltagesMenuItemAction.run(): bms is null");
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
      final double minVoltage = Math.round(sensorboard.getBmsAndEnergy().getBmsState().getMinimumCellVoltage() * 100.0) / 100.0;
      final double maxVoltage = Math.round(sensorboard.getBmsAndEnergy().getBmsState().getMaximumCellVoltage() * 100.0) / 100.0;
      final double averageVoltage = Math.round(sensorboard.getBmsAndEnergy().getBmsState().getAverageCellVoltage() * 100.0) / 100.0;

      LOG.debug("VoltagesMenuItemAction.activate(): updating voltages");
      getCharacterDisplay().setLine(0, "Min Voltage: " + minVoltage);
      getCharacterDisplay().setLine(1, "Max Voltage: " + maxVoltage);
      getCharacterDisplay().setLine(2, "Average Voltage: " + averageVoltage);
      getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
      }
   }
