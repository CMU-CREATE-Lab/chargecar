package org.chargecar.sensorboard;

import java.util.PropertyResourceBundle;
import javax.swing.JPanel;
import org.chargecar.HeadsUpDisplay;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SpeedAndOdometryView extends View<SpeedAndOdometry>
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(SpeedAndOdometryView.class.getName());
   private static final PropertyResourceBundle COMMON_RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(HeadsUpDisplay.class.getName());
   private static final String UNKNOWN_VALUE = COMMON_RESOURCES.getString("unknown-value");

   private final Gauge<Integer> speedGaugePanel = new Gauge<Integer>(RESOURCES.getString("label.speed"), "%02d");
   private final Gauge<Double> odometerGaugePanel = new Gauge<Double>(RESOURCES.getString("label.odometer"), "%08.2f");
   private final Gauge<Double> tripOdometerGaugePanel = new Gauge<Double>(RESOURCES.getString("label.trip-odometer"), "%08.2f");

   public JPanel getSpeedGaugePanel()
      {
      return speedGaugePanel;
      }

   public JPanel getOdometerGaugePanel()
      {
      return odometerGaugePanel;
      }

   public JPanel getTripOdometerGaugePanel()
      {
      return tripOdometerGaugePanel;
      }

   public void handleEvent(final SpeedAndOdometry speedAndOdometry)
      {
      runInGUIThread(
            new Runnable()
            {
            public void run()
               {
               if (speedAndOdometry != null)
                  {
                  speedGaugePanel.setValue(speedAndOdometry.getSpeed());
                  odometerGaugePanel.setValue(speedAndOdometry.getOdometer());
                  tripOdometerGaugePanel.setValue(speedAndOdometry.getTripOdometer());
                  }
               else
                  {
                  speedGaugePanel.setValue(UNKNOWN_VALUE);
                  odometerGaugePanel.setValue(UNKNOWN_VALUE);
                  tripOdometerGaugePanel.setValue(UNKNOWN_VALUE);
                  }
               }
            });
      }
   }
