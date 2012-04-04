package org.chargecar.sensorboard;

/**
 * <p>
 * <code>EfficiencyController</code> is the MVC controller class for the {@link EfficiencyModel} and {@link EfficiencyView}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class EfficiencyController
   {
   private final EfficiencyModel model;

   public EfficiencyController(final EfficiencyModel model)
      {
      this.model = model;
      }

   public void resetBatteryEfficiency()
      {
      model.resetBatteryEfficiency();
      }
   }
