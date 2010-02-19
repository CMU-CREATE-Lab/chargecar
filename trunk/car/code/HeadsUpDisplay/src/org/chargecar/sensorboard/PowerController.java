package org.chargecar.sensorboard;

/**
 * <p>
 * <code>PowerController</code> is the MVC controller class for the {@link PowerModel} and {@link PowerView}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class PowerController
   {
   private final PowerModel model;

   public PowerController(final PowerModel model)
      {
      this.model = model;
      }

   public void resetBatteryCurrentMinMax()
      {
      model.resetBatteryCurrentMinMax();
      }

   public void resetBatteryVoltageMinMax()
      {
      model.resetBatteryVoltageMinMax();
      }

   public void resetCapacitorCurrentMinMax()
      {
      model.resetCapacitorCurrentMinMax();
      }

   public void resetCapacitorVoltageMinMax()
      {
      model.resetCapacitorVoltageMinMax();
      }

   public void resetAccessoryCurrentMinMax()
      {
      model.resetAccessoryCurrentMinMax();
      }

   public void resetAccessoryVoltageMinMax()
      {
      model.resetAccessoryVoltageMinMax();
      }

   public void resetMotorCurrentMinMax(final int id)
      {
      model.resetMotorCurrentMinMax(id);
      }

   public void resetBatteryVoltageMinMax(final int id)
      {
      model.resetBatteryVoltageMinMax(id);
      }

   public void resetBatteryPowerEquation()
      {
      model.resetBatteryPowerEquation();
      }

   public void resetCapacitorPowerEquation()
      {
      model.resetCapacitorPowerEquation();
      }

   public void resetAccessoryPowerEquation()
      {
      model.resetAccessoryPowerEquation();
      }
   }
