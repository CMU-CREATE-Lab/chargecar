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
   private final Gauge<Double> odometerGauge = new Gauge<Double>(RESOURCES.getString("label.odometer"), "%08.1f");
   private final Gauge<Double> tripOdometer1Gauge = new Gauge<Double>(RESOURCES.getString("label.trip-odometer") + " 1", "%08.2f");
   private final Gauge<Double> tripOdometer2Gauge = new Gauge<Double>(RESOURCES.getString("label.trip-odometer") + " 2", "%08.2f");

   public JPanel getSpeedGauge()
      {
      return speedGauge;
      }

   public JPanel getOdometerGauge()
      {
      return odometerGauge;
      }

   public JPanel getTripOdometer1Gauge()
      {
      return tripOdometer1Gauge;
      }

   public JPanel getTripOdometer2Gauge()
      {
      return tripOdometer2Gauge;
      }

   protected void handleEventInGUIThread(final SpeedAndOdometry speedAndOdometry)
      {
      if (speedAndOdometry != null)
         {
         speedGauge.setValue(speedAndOdometry.getSpeed());
         odometerGauge.setValue(speedAndOdometry.getOdometer());
         tripOdometer1Gauge.setValue(speedAndOdometry.getTripOdometer1());
         tripOdometer2Gauge.setValue(speedAndOdometry.getTripOdometer2());
         }
      else
         {
         speedGauge.setValue(null);
         odometerGauge.setValue(null);
         tripOdometer1Gauge.setValue(null);
         tripOdometer2Gauge.setValue(null);
         }
      }
   }
