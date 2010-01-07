package org.chargecar.sensorboard;

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

   private static final String CURRENT_STRING_FORMAT = "%+06.1f";
   private static final String VOLTAGE_STRING_FORMAT = "%05.1f";
   private static final String POWER_STRING_FORMAT = "%+06.1f";

   private final Gauge<Double> accessoryCurrentGauge = new Gauge<Double>(RESOURCES.getString("label.accessory"), CURRENT_STRING_FORMAT);
   private final Gauge<Double> batteryCurrentGauge = new Gauge<Double>(RESOURCES.getString("label.battery"), CURRENT_STRING_FORMAT);
   private final Gauge<Double> capacitorCurrentGauge = new Gauge<Double>(RESOURCES.getString("label.capacitor"), CURRENT_STRING_FORMAT);
   private final List<Gauge<Double>> motorCurrentGauges = new ArrayList<Gauge<Double>>(SensorBoardConstants.MOTOR_DEVICE_COUNT);

   private final Gauge<Double> accessoryVoltageGauge = new Gauge<Double>(RESOURCES.getString("label.accessory"), VOLTAGE_STRING_FORMAT);
   private final Gauge<Double> batteryVoltageGauge = new Gauge<Double>(RESOURCES.getString("label.battery"), VOLTAGE_STRING_FORMAT);
   private final Gauge<Double> capacitorVoltageGauge = new Gauge<Double>(RESOURCES.getString("label.capacitor"), VOLTAGE_STRING_FORMAT);
   private final List<Gauge<Double>> batteryVoltageGauges = new ArrayList<Gauge<Double>>(SensorBoardConstants.BATTERY_DEVICE_COUNT);

   private final Gauge<Double> accessoryPowerTotalGauge = new Gauge<Double>(RESOURCES.getString("label.total"), POWER_STRING_FORMAT);
   private final Gauge<Double> accessoryPowerUsedGauge = new Gauge<Double>(RESOURCES.getString("label.used"), POWER_STRING_FORMAT);
   private final Gauge<Double> accessoryPowerRegenGauge = new Gauge<Double>(RESOURCES.getString("label.regen"), POWER_STRING_FORMAT);
   private final Gauge<Double> batteryPowerTotalGauge = new Gauge<Double>(RESOURCES.getString("label.total"), POWER_STRING_FORMAT);
   private final Gauge<Double> batteryPowerUsedGauge = new Gauge<Double>(RESOURCES.getString("label.used"), POWER_STRING_FORMAT);
   private final Gauge<Double> batteryPowerRegenGauge = new Gauge<Double>(RESOURCES.getString("label.regen"), POWER_STRING_FORMAT);
   private final Gauge<Double> capacitorPowerTotalGauge = new Gauge<Double>(RESOURCES.getString("label.total"), POWER_STRING_FORMAT);
   private final Gauge<Double> capacitorPowerUsedGauge = new Gauge<Double>(RESOURCES.getString("label.used"), POWER_STRING_FORMAT);
   private final Gauge<Double> capacitorPowerRegenGauge = new Gauge<Double>(RESOURCES.getString("label.regen"), POWER_STRING_FORMAT);

   public PowerView()
      {
      for (int i = 0; i < SensorBoardConstants.MOTOR_DEVICE_COUNT; i++)
         {
         motorCurrentGauges.add(new Gauge<Double>(RESOURCES.getString("label.motor") + " " + (i + 1), CURRENT_STRING_FORMAT));
         }
      for (int i = 0; i < SensorBoardConstants.BATTERY_DEVICE_COUNT; i++)
         {
         batteryVoltageGauges.add(new Gauge<Double>(RESOURCES.getString("label.battery") + " " + (i + 1), VOLTAGE_STRING_FORMAT));
         }
      }

   public JPanel getAccessoryCurrentGauge()
      {
      return accessoryCurrentGauge;
      }

   public JPanel getBatteryCurrentGauge()
      {
      return batteryCurrentGauge;
      }

   public JPanel getCapacitorCurrentGauge()
      {
      return capacitorCurrentGauge;
      }

   public JPanel getMotorCurrentGauge(final int id)
      {
      if (0 <= id && id < SensorBoardConstants.MOTOR_DEVICE_COUNT)
         {
         return motorCurrentGauges.get(id);
         }
      return null;
      }

   public JPanel getAccessoryVoltageGauge()
      {
      return accessoryVoltageGauge;
      }

   public JPanel getBatteryVoltageGauge()
      {
      return batteryVoltageGauge;
      }

   public JPanel getCapacitorVoltageGauge()
      {
      return capacitorVoltageGauge;
      }

   public JPanel getBatteryVoltageGauge(final int id)
      {
      if (0 <= id && id < SensorBoardConstants.BATTERY_DEVICE_COUNT)
         {
         return batteryVoltageGauges.get(id);
         }
      return null;
      }

   public JPanel getAccessoryPowerTotalGauge()
      {
      return accessoryPowerTotalGauge;
      }

   public JPanel getAccessoryPowerUsedGauge()
      {
      return accessoryPowerUsedGauge;
      }

   public JPanel getAccessoryPowerRegenGauge()
      {
      return accessoryPowerRegenGauge;
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
            accessoryCurrentGauge.setValue(power.getCurrents().getAccessoryCurrent());
            batteryCurrentGauge.setValue(power.getCurrents().getBatteryCurrent());
            capacitorCurrentGauge.setValue(power.getCurrents().getCapacitorCurrent());
            for (int i = 0; i < SensorBoardConstants.MOTOR_DEVICE_COUNT; i++)
               {
               motorCurrentGauges.get(i).setValue(power.getCurrents().getMotorCurrent(i));
               }
            }
         else
            {
            setUnknownCurrents();
            }

         if (power.getVoltages() != null)
            {
            accessoryVoltageGauge.setValue(power.getVoltages().getAccessoryVoltage());
            batteryVoltageGauge.setValue(power.getVoltages().getBatteryVoltage());
            capacitorVoltageGauge.setValue(power.getVoltages().getCapacitorVoltage());
            for (int i = 0; i < SensorBoardConstants.BATTERY_DEVICE_COUNT; i++)
               {
               batteryVoltageGauges.get(i).setValue(power.getVoltages().getBatteryVoltage(i));
               }
            }
         else
            {
            setUnknownVoltages();
            }

         accessoryPowerTotalGauge.setValue(power.getAccessoryPowerEquation().getKilowattHours());
         accessoryPowerUsedGauge.setValue(power.getAccessoryPowerEquation().getKilowattHoursUsed());
         accessoryPowerRegenGauge.setValue(power.getAccessoryPowerEquation().getKilowattHoursRegen());
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
         accessoryPowerUsedGauge.setValue(null);
         accessoryPowerRegenGauge.setValue(null);
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
      accessoryCurrentGauge.setValue(null);
      batteryCurrentGauge.setValue(null);
      capacitorCurrentGauge.setValue(null);
      for (int i = 0; i < SensorBoardConstants.MOTOR_DEVICE_COUNT; i++)
         {
         motorCurrentGauges.get(i).setValue(null);
         }
      }

   private void setUnknownVoltages()
      {
      accessoryVoltageGauge.setValue(null);
      batteryVoltageGauge.setValue(null);
      capacitorVoltageGauge.setValue(null);
      for (int i = 0; i < SensorBoardConstants.BATTERY_DEVICE_COUNT; i++)
         {
         batteryVoltageGauges.get(i).setValue(null);
         }
      }
   }
