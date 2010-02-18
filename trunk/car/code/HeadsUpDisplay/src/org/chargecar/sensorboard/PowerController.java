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
   private final PowerModel powerModel;

   public PowerController(final PowerModel powerModel)
      {
      this.powerModel = powerModel;
      }

   public void resetBatteryCurrentMinMax()
      {
      powerModel.resetBatteryCurrentMinMax();
      }

   public void resetBatteryVoltageMinMax()
      {
      powerModel.resetBatteryVoltageMinMax();
      }

   public void resetCapacitorCurrentMinMax()
      {
      powerModel.resetCapacitorCurrentMinMax();
      }

   public void resetCapacitorVoltageMinMax()
      {
      powerModel.resetCapacitorVoltageMinMax();
      }

   public void resetAccessoryCurrentMinMax()
      {
      powerModel.resetAccessoryCurrentMinMax();
      }

   public void resetAccessoryVoltageMinMax()
      {
      powerModel.resetAccessoryVoltageMinMax();
      }

   public void resetMotorCurrentMinMax(final int id)
      {
      powerModel.resetMotorCurrentMinMax(id);
      }

   public void resetBatteryVoltageMinMax(final int id)
      {
      powerModel.resetBatteryVoltageMinMax(id);
      }

   public void resetBatteryPowerEquation()
      {
      powerModel.resetBatteryPowerEquation();
      }

   public void resetCapacitorPowerEquation()
      {
      powerModel.resetCapacitorPowerEquation();
      }

   public void resetAccessoryPowerEquation()
      {
      powerModel.resetAccessoryPowerEquation();
      }
   }
