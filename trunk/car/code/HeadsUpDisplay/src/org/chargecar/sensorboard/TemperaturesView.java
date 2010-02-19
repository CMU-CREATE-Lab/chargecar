package org.chargecar.sensorboard;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.PropertyResourceBundle;
import javax.swing.JPanel;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class TemperaturesView extends View<Temperatures>
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(TemperaturesView.class.getName());

   private final List<Meter> motorMeters = new ArrayList<Meter>(SensorBoardConstants.MOTOR_DEVICE_COUNT);
   private final List<Meter> motorControllerMeters = new ArrayList<Meter>(SensorBoardConstants.MOTOR_CONTROLLER_DEVICE_COUNT);
   private final List<Meter> auxiliaryMeters = new ArrayList<Meter>(SensorBoardConstants.AUXILIARY_DEVICE_COUNT);
   private final Meter capacitorMeter;
   private final Meter batteryMeter;
   private final Meter outsideMeter;

   public TemperaturesView()
      {
      final DefaultMeterConfig meterConfig = new DefaultMeterConfig(1);
      meterConfig.setDatasetColor(0, Color.RED);

      // configure the meters ==========================================================================================

      meterConfig.setSize(160, 160);
      meterConfig.setRange(0, 200);
      meterConfig.setTicks(20, 5);
      meterConfig.setNumberFormat(new DecimalFormat("##0.0"));
      meterConfig.setBackgroundColor(GUIConstants.CAPACITOR_METER_COLOR);
      meterConfig.setLabel(RESOURCES.getString("label.capacitor"), RESOURCES.getString("label.temperature"));

      capacitorMeter = new Meter(meterConfig);

      // ---------------------------------------------------------------------------------------------------------------

      meterConfig.clearDialRanges();
      meterConfig.setBackgroundColor(GUIConstants.BATTERY_METER_COLOR);
      meterConfig.setLabel(RESOURCES.getString("label.batteries"), RESOURCES.getString("label.temperature"));

      batteryMeter = new Meter(meterConfig);

      // ---------------------------------------------------------------------------------------------------------------

      meterConfig.clearDialRanges();
      meterConfig.setBackgroundColor(GUIConstants.DEFAULT_METER_COLOR);
      meterConfig.setLabel(RESOURCES.getString("label.outside"), RESOURCES.getString("label.temperature"));

      outsideMeter = new Meter(meterConfig);

      // ---------------------------------------------------------------------------------------------------------------

      meterConfig.clearDialRanges();
      meterConfig.setBackgroundColor(GUIConstants.MOTOR_METER_COLOR);
      for (int i = 0; i < SensorBoardConstants.MOTOR_DEVICE_COUNT; i++)
         {
         meterConfig.setLabel(RESOURCES.getString("label.motor") + " " + (i + 1), RESOURCES.getString("label.temperature"));
         motorMeters.add(new Meter(meterConfig));
         }

      meterConfig.clearDialRanges();
      meterConfig.setBackgroundColor(GUIConstants.MOTOR_CONTROLLER_METER_COLOR);
      for (int i = 0; i < SensorBoardConstants.MOTOR_CONTROLLER_DEVICE_COUNT; i++)
         {
         meterConfig.setLabel(RESOURCES.getString("label.controller") + " " + (i + 1), RESOURCES.getString("label.temperature"));
         motorControllerMeters.add(new Meter(meterConfig));
         }

      meterConfig.clearDialRanges();
      meterConfig.setBackgroundColor(GUIConstants.AUXILIARY_DEVICE_METER_COLOR);
      for (int i = 0; i < SensorBoardConstants.AUXILIARY_DEVICE_COUNT; i++)
         {
         meterConfig.setLabel(RESOURCES.getString("label.aux") + " " + (i + 1), RESOURCES.getString("label.temperature"));
         auxiliaryMeters.add(new Meter(meterConfig));
         }
      }

   public JPanel getMotorMeter(final int id)
      {
      if (0 <= id && id < SensorBoardConstants.MOTOR_DEVICE_COUNT)
         {
         return motorMeters.get(id);
         }
      return null;
      }

   public JPanel getMotorControllerMeter(final int id)
      {
      if (0 <= id && id < SensorBoardConstants.MOTOR_CONTROLLER_DEVICE_COUNT)
         {
         return motorControllerMeters.get(id);
         }
      return null;
      }

   public JPanel getAuxiliaryMeter(final int id)
      {
      if (0 <= id && id < SensorBoardConstants.AUXILIARY_DEVICE_COUNT)
         {
         return auxiliaryMeters.get(id);
         }
      return null;
      }

   public JPanel getCapacitorMeter()
      {
      return capacitorMeter;
      }

   public JPanel getBatteryMeter()
      {
      return batteryMeter;
      }

   public JPanel getOutsideMeter()
      {
      return outsideMeter;
      }

   protected void handleEventInGUIThread(final Temperatures temperatures)
      {
      if (temperatures != null)
         {
         for (int i = 0; i < SensorBoardConstants.MOTOR_DEVICE_COUNT; i++)
            {
            motorMeters.get(i).setValues(temperatures.getMotorTemperature(i));
            }
         for (int i = 0; i < SensorBoardConstants.MOTOR_CONTROLLER_DEVICE_COUNT; i++)
            {
            motorControllerMeters.get(i).setValues(temperatures.getMotorControllerTemperature(i));
            }
         for (int i = 0; i < SensorBoardConstants.AUXILIARY_DEVICE_COUNT; i++)
            {
            auxiliaryMeters.get(i).setValues(temperatures.getAuxiliaryTemperature(i));
            }
         capacitorMeter.setValues(temperatures.getCapacitorTemperature());
         batteryMeter.setValues(temperatures.getBatteryTemperature());
         outsideMeter.setValues(temperatures.getOutsideTemperature());
         }
      else
         {
         for (int i = 0; i < SensorBoardConstants.MOTOR_DEVICE_COUNT; i++)
            {
            motorMeters.get(i).setValues();
            }
         for (int i = 0; i < SensorBoardConstants.MOTOR_CONTROLLER_DEVICE_COUNT; i++)
            {
            motorControllerMeters.get(i).setValues();
            }
         for (int i = 0; i < SensorBoardConstants.AUXILIARY_DEVICE_COUNT; i++)
            {
            auxiliaryMeters.get(i).setValues();
            }
         capacitorMeter.setValues();
         batteryMeter.setValues();
         outsideMeter.setValues();
         }
      }
   }
