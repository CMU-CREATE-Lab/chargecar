package org.chargecar.sensorboard;

import java.util.PropertyResourceBundle;
import javax.swing.JPanel;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SpeedAndOdometryView extends View<SpeedAndOdometry>
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(SpeedAndOdometryView.class.getName());

   private final Gauge<Integer> speedGauge = new Gauge<Integer>(RESOURCES.getString("label.speed"), "%02d");
   private final Gauge<Double> odometerGauge = new Gauge<Double>(RESOURCES.getString("label.odometer"), "%08.2f");
   private final Gauge<Double> tripOdometerGauge = new Gauge<Double>(RESOURCES.getString("label.trip-odometer"), "%08.2f");

   public JPanel getSpeedGauge()
      {
      return speedGauge;
      }

   public JPanel getOdometerGauge()
      {
      return odometerGauge;
      }

   public JPanel getTripOdometerGauge()
      {
      return tripOdometerGauge;
      }

   protected void handleEventInGUIThread(final SpeedAndOdometry speedAndOdometry)
      {
      if (speedAndOdometry != null)
         {
         speedGauge.setValue(speedAndOdometry.getSpeed());
         odometerGauge.setValue(speedAndOdometry.getOdometer());
         tripOdometerGauge.setValue(speedAndOdometry.getTripOdometer());
         }
      else
         {
         speedGauge.setValue(null);
         odometerGauge.setValue(null);
         tripOdometerGauge.setValue(null);
         }
      }
   }
