package org.chargecar.sensorboard;

import java.util.ArrayList;
import java.util.List;
import java.util.PropertyResourceBundle;
import javax.swing.JPanel;
import org.chargecar.HeadsUpDisplay;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class TemperaturesView extends View<Temperatures>
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(TemperaturesView.class.getName());
   private static final PropertyResourceBundle COMMON_RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(HeadsUpDisplay.class.getName());
   private static final String UNKNOWN_VALUE = COMMON_RESOURCES.getString("unknown-value");

   private final List<Gauge<Double>> motorGauges = new ArrayList<Gauge<Double>>(SensorBoardConstants.MOTOR_DEVICE_COUNT);
   private final List<Gauge<Double>> motorControllerGauges = new ArrayList<Gauge<Double>>(SensorBoardConstants.MOTOR_CONTROLLER_DEVICE_COUNT);
   private final List<Gauge<Double>> auxiliaryGauges = new ArrayList<Gauge<Double>>(SensorBoardConstants.AUXILIARY_DEVICE_COUNT);
   private final Gauge<Double> capacitorGauge = new Gauge<Double>(RESOURCES.getString("label.capacitor"), "%05.1f");
   private final Gauge<Double> batteryGauge = new Gauge<Double>(RESOURCES.getString("label.battery"), "%05.1f");
   private final Gauge<Double> outsideGauge = new Gauge<Double>(RESOURCES.getString("label.outside"), "%05.1f");

   public TemperaturesView()
      {
      for (int i = 0; i < SensorBoardConstants.MOTOR_DEVICE_COUNT; i++)
         {
         motorGauges.add(new Gauge<Double>(RESOURCES.getString("label.motor") + " " + (i + 1), "%05.1f"));
         }
      for (int i = 0; i < SensorBoardConstants.MOTOR_CONTROLLER_DEVICE_COUNT; i++)
         {
         motorControllerGauges.add(new Gauge<Double>(RESOURCES.getString("label.controller") + " " + (i + 1), "%05.1f"));
         }
      for (int i = 0; i < SensorBoardConstants.AUXILIARY_DEVICE_COUNT; i++)
         {
         auxiliaryGauges.add(new Gauge<Double>(RESOURCES.getString("label.aux") + " " + (i + 1), "%05.1f"));
         }
      }

   public JPanel getMotorGauge(final int id)
      {
      if (0 <= id && id < SensorBoardConstants.MOTOR_DEVICE_COUNT)
         {
         return motorGauges.get(id);
         }
      return null;
      }

   public JPanel getMotorControllerGauge(final int id)
      {
      if (0 <= id && id < SensorBoardConstants.MOTOR_CONTROLLER_DEVICE_COUNT)
         {
         return motorControllerGauges.get(id);
         }
      return null;
      }

   public JPanel getAuxiliaryGauge(final int id)
      {
      if (0 <= id && id < SensorBoardConstants.AUXILIARY_DEVICE_COUNT)
         {
         return auxiliaryGauges.get(id);
         }
      return null;
      }

   public JPanel getCapacitorGauge()
      {
      return capacitorGauge;
      }

   public JPanel getBatteryGauge()
      {
      return batteryGauge;
      }

   public JPanel getOutsideGauge()
      {
      return outsideGauge;
      }

   public void handleEvent(final Temperatures temperatures)
      {
      runInGUIThread(
            new Runnable()
            {
            public void run()
               {
               if (temperatures != null)
                  {
                  for (int i = 0; i < SensorBoardConstants.MOTOR_DEVICE_COUNT; i++)
                     {
                     motorGauges.get(i).setValue(temperatures.getMotorTemperature(i));
                     }
                  for (int i = 0; i < SensorBoardConstants.MOTOR_CONTROLLER_DEVICE_COUNT; i++)
                     {
                     motorControllerGauges.get(i).setValue(temperatures.getMotorControllerTemperature(i));
                     }
                  for (int i = 0; i < SensorBoardConstants.AUXILIARY_DEVICE_COUNT; i++)
                     {
                     auxiliaryGauges.get(i).setValue(temperatures.getAuxiliaryTemperature(i));
                     }
                  capacitorGauge.setValue(temperatures.getCapacitorTemperature());
                  batteryGauge.setValue(temperatures.getBatteryTemperature());
                  outsideGauge.setValue(temperatures.getOutsideTemperature());
                  }
               else
                  {
                  for (int i = 0; i < SensorBoardConstants.MOTOR_DEVICE_COUNT; i++)
                     {
                     motorGauges.get(i).setValue(UNKNOWN_VALUE);
                     }
                  for (int i = 0; i < SensorBoardConstants.MOTOR_CONTROLLER_DEVICE_COUNT; i++)
                     {
                     motorControllerGauges.get(i).setValue(UNKNOWN_VALUE);
                     }
                  for (int i = 0; i < SensorBoardConstants.AUXILIARY_DEVICE_COUNT; i++)
                     {
                     auxiliaryGauges.get(i).setValue(UNKNOWN_VALUE);
                     }
                  capacitorGauge.setValue(UNKNOWN_VALUE);
                  batteryGauge.setValue(UNKNOWN_VALUE);
                  outsideGauge.setValue(UNKNOWN_VALUE);
                  }
               }
            });
      }
   }
