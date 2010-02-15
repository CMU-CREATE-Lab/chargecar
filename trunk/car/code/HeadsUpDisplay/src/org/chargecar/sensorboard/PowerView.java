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
public final class PowerView extends View<Power>
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(PowerView.class.getName());

   private static final String POWER_STRING_FORMAT = "%06.1f";

   private final Meter batteryCurrentMeter;
   private final Meter batteryVoltageMeter;
   private final Meter capacitorCurrentMeter;
   private final Meter capacitorVoltageMeter;
   private final Meter accessoryCurrentMeter;
   private final Meter accessoryVoltageMeter;

   private final List<Meter> motorCurrentMeters = new ArrayList<Meter>(SensorBoardConstants.MOTOR_DEVICE_COUNT);
   private final List<Meter> batteryVoltageMeters = new ArrayList<Meter>(SensorBoardConstants.BATTERY_DEVICE_COUNT);

   private final Gauge<Double> accessoryPowerTotalGauge = new Gauge<Double>(RESOURCES.getString("label.total"), POWER_STRING_FORMAT);
   private final Gauge<Double> batteryPowerTotalGauge = new Gauge<Double>(RESOURCES.getString("label.total"), POWER_STRING_FORMAT);
   private final Gauge<Double> batteryPowerUsedGauge = new Gauge<Double>(RESOURCES.getString("label.used"), POWER_STRING_FORMAT);
   private final Gauge<Double> batteryPowerRegenGauge = new Gauge<Double>(RESOURCES.getString("label.regen"), POWER_STRING_FORMAT);
   private final Gauge<Double> capacitorPowerTotalGauge = new Gauge<Double>(RESOURCES.getString("label.total"), POWER_STRING_FORMAT);
   private final Gauge<Double> capacitorPowerUsedGauge = new Gauge<Double>(RESOURCES.getString("label.used"), POWER_STRING_FORMAT);
   private final Gauge<Double> capacitorPowerRegenGauge = new Gauge<Double>(RESOURCES.getString("label.regen"), POWER_STRING_FORMAT);

   public PowerView()
      {
      final DefaultMeterConfig meterConfig = new DefaultMeterConfig(3);
      meterConfig.setDatasetColor(0, Color.RED);
      meterConfig.setDatasetColor(1, Color.GREEN);
      meterConfig.setDatasetColor(2, Color.BLUE);

      // configure the meters ==========================================================================================

      meterConfig.setSize(160, 160);
      meterConfig.setRange(-1200, 1200);
      meterConfig.setTicks(200, 3);
      meterConfig.addDialRange(-1200, -1100, Color.RED);
      meterConfig.addDialRange(-1100, -1000, new Color(255, 150, 0));
      meterConfig.addDialRange(-1000, -900, Color.YELLOW);
      meterConfig.addDialRange(900, 1000, Color.YELLOW);
      meterConfig.addDialRange(1000, 1100, new Color(255, 150, 0));
      meterConfig.addDialRange(1100, 1200, Color.RED);
      meterConfig.setNumberFormat(new DecimalFormat("###0"));
      meterConfig.setLabel(RESOURCES.getString("label.battery"), RESOURCES.getString("label.current"));

      batteryCurrentMeter = new Meter(meterConfig);

      // ---------------------------------------------------------------------------------------------------------------

      meterConfig.clearDialRanges();
      meterConfig.setRange(30, 70);
      meterConfig.setTicks(5, 4);
      meterConfig.setNumberFormat(new DecimalFormat("#0.0"));
      meterConfig.setLabel(RESOURCES.getString("label.battery"), RESOURCES.getString("label.voltage"));

      batteryVoltageMeter = new Meter(meterConfig);

      // ---------------------------------------------------------------------------------------------------------------

      meterConfig.clearDialRanges();
      meterConfig.setRange(-1600, 1600);
      meterConfig.setTicks(400, 3);
      meterConfig.setNumberFormat(new DecimalFormat("###0"));
      meterConfig.setLabel(RESOURCES.getString("label.capacitor"), RESOURCES.getString("label.current"));

      capacitorCurrentMeter = new Meter(meterConfig);

      // ---------------------------------------------------------------------------------------------------------------

      meterConfig.clearDialRanges();
      meterConfig.setRange(0, 55);
      meterConfig.setTicks(5, 4);
      meterConfig.setNumberFormat(new DecimalFormat("#0.0"));
      meterConfig.setLabel(RESOURCES.getString("label.capacitor"), RESOURCES.getString("label.voltage"));

      capacitorVoltageMeter = new Meter(meterConfig);

      // ---------------------------------------------------------------------------------------------------------------

      meterConfig.clearDialRanges();
      meterConfig.setRange(-100, 100);
      meterConfig.setTicks(20, 5);
      meterConfig.setNumberFormat(new DecimalFormat("##0.0"));
      meterConfig.setLabel(RESOURCES.getString("label.accessory"), RESOURCES.getString("label.current"));

      accessoryCurrentMeter = new Meter(meterConfig);

      // ---------------------------------------------------------------------------------------------------------------

      meterConfig.clearDialRanges();
      meterConfig.setRange(8, 16);
      meterConfig.setTicks(1, 9);
      meterConfig.setNumberFormat(new DecimalFormat("#0.0"));
      meterConfig.setLabel(RESOURCES.getString("label.accessory"), RESOURCES.getString("label.voltage"));

      accessoryVoltageMeter = new Meter(meterConfig);

      // ---------------------------------------------------------------------------------------------------------------

      meterConfig.clearDialRanges();
      meterConfig.setRange(8, 16);
      meterConfig.setTicks(1, 9);
      meterConfig.setNumberFormat(new DecimalFormat("#0.0"));
      for (int i = 0; i < SensorBoardConstants.BATTERY_DEVICE_COUNT; i++)
         {
         // 8 to 16
         meterConfig.setLabel(RESOURCES.getString("label.battery") + " " + (i + 1), RESOURCES.getString("label.voltage"));
         batteryVoltageMeters.add(new Meter(meterConfig));
         }

      meterConfig.clearDialRanges();
      meterConfig.setRange(-500, 500);
      meterConfig.setTicks(100, 9);
      meterConfig.setNumberFormat(new DecimalFormat("##0.0"));
      for (int i = 0; i < SensorBoardConstants.MOTOR_DEVICE_COUNT; i++)
         {
         meterConfig.setLabel(RESOURCES.getString("label.motor") + " " + (i + 1), RESOURCES.getString("label.current"));
         motorCurrentMeters.add(new Meter(meterConfig));
         }
      }

   public JPanel getAccessoryCurrentMeter()
      {
      return accessoryCurrentMeter;
      }

   public JPanel getBatteryCurrentMeter()
      {
      return batteryCurrentMeter;
      }

   public JPanel getCapacitorCurrentMeter()
      {
      return capacitorCurrentMeter;
      }

   public JPanel getMotorCurrentMeter(final int id)
      {
      if (0 <= id && id < SensorBoardConstants.MOTOR_DEVICE_COUNT)
         {
         return motorCurrentMeters.get(id);
         }
      return null;
      }

   public JPanel getAccessoryVoltageMeter()
      {
      return accessoryVoltageMeter;
      }

   public JPanel getBatteryVoltageMeter()
      {
      return batteryVoltageMeter;
      }

   public JPanel getCapacitorVoltageMeter()
      {
      return capacitorVoltageMeter;
      }

   public JPanel getBatteryVoltageMeter(final int id)
      {
      if (0 <= id && id < SensorBoardConstants.BATTERY_DEVICE_COUNT)
         {
         return batteryVoltageMeters.get(id);
         }
      return null;
      }

   public JPanel getAccessoryPowerTotalGauge()
      {
      return accessoryPowerTotalGauge;
      }

   public JPanel getBatteryPowerTotalGauge()
      {
      return batteryPowerTotalGauge;
      }

   public JPanel getBatteryPowerUsedGauge()
      {
      return batteryPowerUsedGauge;
      }

   public JPanel getBatteryPowerRegenGauge()
      {
      return batteryPowerRegenGauge;
      }

   public JPanel getCapacitorPowerTotalGauge()
      {
      return capacitorPowerTotalGauge;
      }

   public JPanel getCapacitorPowerUsedGauge()
      {
      return capacitorPowerUsedGauge;
      }

   public JPanel getCapacitorPowerRegenGauge()
      {
      return capacitorPowerRegenGauge;
      }

   protected void handleEventInGUIThread(final Power power)
      {
      if (power != null)
         {
         if (power.getCurrents() != null)
            {
            accessoryCurrentMeter.setValues(power.getCurrents().getAccessoryCurrent(),
                                            power.getMinimumCurrents().getAccessoryCurrent(),
                                            power.getMaximumCurrents().getAccessoryCurrent());
            batteryCurrentMeter.setValues(power.getCurrents().getBatteryCurrent(),
                                          power.getMinimumCurrents().getBatteryCurrent(),
                                          power.getMaximumCurrents().getBatteryCurrent());
            capacitorCurrentMeter.setValues(power.getCurrents().getCapacitorCurrent(),
                                            power.getMinimumCurrents().getCapacitorCurrent(),
                                            power.getMaximumCurrents().getCapacitorCurrent());
            for (int i = 0; i < SensorBoardConstants.MOTOR_DEVICE_COUNT; i++)
               {
               motorCurrentMeters.get(i).setValues(power.getCurrents().getMotorCurrent(i),
                                                   power.getMinimumCurrents().getMotorCurrent(i),
                                                   power.getMaximumCurrents().getMotorCurrent(i));
               }
            }
         else
            {
            setUnknownCurrents();
            }

         if (power.getVoltages() != null)
            {
            accessoryVoltageMeter.setValues(power.getVoltages().getAccessoryVoltage(),
                                            power.getMinimumVoltages().getAccessoryVoltage(),
                                            power.getMaximumVoltages().getAccessoryVoltage());
            batteryVoltageMeter.setValues(power.getVoltages().getBatteryVoltage(),
                                          power.getMinimumVoltages().getBatteryVoltage(),
                                          power.getMaximumVoltages().getBatteryVoltage());
            capacitorVoltageMeter.setValues(power.getVoltages().getCapacitorVoltage(),
                                            power.getMinimumVoltages().getCapacitorVoltage(),
                                            power.getMaximumVoltages().getCapacitorVoltage());
            for (int i = 0; i < SensorBoardConstants.BATTERY_DEVICE_COUNT; i++)
               {
               batteryVoltageMeters.get(i).setValues(power.getVoltages().getBatteryVoltage(i),
                                                     power.getMinimumVoltages().getBatteryVoltage(i),
                                                     power.getMaximumVoltages().getBatteryVoltage(i));
               }
            }
         else
            {
            setUnknownVoltages();
            }

         accessoryPowerTotalGauge.setValue(power.getAccessoryPowerEquation().getKilowattHours());
         batteryPowerTotalGauge.setValue(power.getBatteryPowerEquation().getKilowattHours());
         batteryPowerUsedGauge.setValue(power.getBatteryPowerEquation().getKilowattHoursUsed());
         batteryPowerRegenGauge.setValue(power.getBatteryPowerEquation().getKilowattHoursRegen());
         capacitorPowerTotalGauge.setValue(power.getCapacitorPowerEquation().getKilowattHours());
         capacitorPowerUsedGauge.setValue(power.getCapacitorPowerEquation().getKilowattHoursUsed());
         capacitorPowerRegenGauge.setValue(power.getCapacitorPowerEquation().getKilowattHoursRegen());
         }
      else
         {
         setUnknownCurrents();
         setUnknownVoltages();
         accessoryPowerTotalGauge.setValue(null);
         batteryPowerTotalGauge.setValue(null);
         batteryPowerUsedGauge.setValue(null);
         batteryPowerRegenGauge.setValue(null);
         capacitorPowerTotalGauge.setValue(null);
         capacitorPowerUsedGauge.setValue(null);
         capacitorPowerRegenGauge.setValue(null);
         }
      }

   private void setUnknownCurrents()
      {
      accessoryCurrentMeter.setValues(null, null, null);
      batteryCurrentMeter.setValues(null, null, null);
      capacitorCurrentMeter.setValues(null, null, null);
      for (int i = 0; i < SensorBoardConstants.MOTOR_DEVICE_COUNT; i++)
         {
         motorCurrentMeters.get(i).setValues(null, null, null);
         }
      }

   private void setUnknownVoltages()
      {
      accessoryVoltageMeter.setValues(null, null, null);
      batteryVoltageMeter.setValues(null, null, null);
      capacitorVoltageMeter.setValues(null, null, null);
      for (int i = 0; i < SensorBoardConstants.BATTERY_DEVICE_COUNT; i++)
         {
         batteryVoltageMeters.get(i).setValues(null, null, null);
         }
      }
   }
