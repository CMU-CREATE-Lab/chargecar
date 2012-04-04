package org.chargecar.sensorboard;

import java.util.PropertyResourceBundle;
import edu.cmu.ri.createlab.util.mvc.View;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class EfficiencyView extends View<Efficiency>
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(EfficiencyView.class.getName());

   private final Gauge<Double> batteryEfficiencyGauge = new Gauge<Double>(RESOURCES.getString("label.efficiency"), "%07.3f");

   public Gauge getBatteryEfficiencyGauge()
      {
      return batteryEfficiencyGauge;
      }

   @Override
   protected void handleEventInGUIThread(final Efficiency efficiency)
      {
      if (efficiency != null)
         {
         batteryEfficiencyGauge.setValue(efficiency.getBatteryEfficiency());
         }
      else
         {
         batteryEfficiencyGauge.setValue(null);
         }
      }
   }
