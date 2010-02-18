package org.chargecar.sensorboard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * <code>PowerController</code> is the MVC controller class for the {@link PowerModel} and {@link PowerView}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class PowerController
   {
   private static final Log LOG = LogFactory.getLog(PowerController.class);

   public PowerController(final PowerModel powerModel)
      {
      }

   public void resetBatteryCurrent()
      {
      LOG.debug("################################################## PowerController.resetBatteryCurrent()");
      }

   public void resetBatteryVoltage()
      {
      LOG.debug("################################################## PowerController.resetBatteryVoltage()");
      }

   public void resetCapacitorCurrent()
      {
      LOG.debug("################################################## PowerController.resetCapacitorCurrent()");
      }

   public void resetCapacitorVoltage()
      {
      LOG.debug("################################################## PowerController.resetCapacitorVoltage()");
      }

   public void resetAccessoryCurrent()
      {
      LOG.debug("################################################## PowerController.resetAccessoryCurrent()");
      }

   public void resetAccessoryVoltage()
      {
      LOG.debug("################################################## PowerController.resetAccessoryVoltage()");
      }

   public void resetMotorCurrent(final int id)
      {
      LOG.debug("################################################## PowerController.resetMotorCurrent(" + id + ")");
      }

   public void resetBatteryVoltage(final int id)
      {
      LOG.debug("################################################## PowerController.resetBatteryVoltage(" + id + ")");
      }

   public void resetBatteryPower()
      {
      LOG.debug("################################################## PowerController.resetBatteryPower()");
      }

   public void resetCapacitorPower()
      {
      LOG.debug("################################################## PowerController.resetCapacitorPower()");
      }

   public void resetAccessoryPower()
      {
      LOG.debug("################################################## PowerController.resetAccessoryPower()");
      }
   }
