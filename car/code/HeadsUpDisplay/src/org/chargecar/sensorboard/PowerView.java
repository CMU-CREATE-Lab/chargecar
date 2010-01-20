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

   private static final String CURRENT_STRING_FORMAT = "%+07.1f";
   private static final String VOLTAGE_STRING_FORMAT = "%04.1f";
   private static final String POWER_STRING_FORMAT = "%06.1f";

   private final MinMaxGauge<Double> accessoryCurrentGauge = new MinMaxGauge<Double>(RESOURCES.getString("label.accessory"), CURRENT_STRING_FORMAT);
   private final MinMaxGauge<Double> batteryCurrentGauge = new MinMaxGauge<Double>(RESOURCES.getString("label.batteries"), CURRENT_STRING_FORMAT);
   private final MinMaxGauge<Double> capacitorCurrentGauge = new MinMaxGauge<Double>(RESOURCES.getString("label.capacitor"), CURRENT_STRING_FORMAT);
   private final List<MinMaxGauge<Double>> motorCurrentGauges = new ArrayList<MinMaxGauge<Double>>(SensorBoardConstants.MOTOR_DEVICE_COUNT);

   private final MinMaxGauge<Double> accessoryVoltageGauge = new MinMaxGauge<Double>(RESOURCES.getString("label.accessory"), VOLTAGE_STRING_FORMAT);
   private final MinMaxGauge<Double> batteryVoltageGauge = new MinMaxGauge<Double>(RESOURCES.getString("label.batteries"), VOLTAGE_STRING_FORMAT);
   private final MinMaxGauge<Double> capacitorVoltageGauge = new MinMaxGauge<Double>(RESOURCES.getString("label.capacitor"), VOLTAGE_STRING_FORMAT);
   private final List<MinMaxGauge<Double>> batteryVoltageGauges = new ArrayList<MinMaxGauge<Double>>(SensorBoardConstants.BATTERY_DEVICE_COUNT);

   private final Gauge<Double> accessoryPowerTotalGauge = new Gauge<Double>(RESOURCES.getString("label.total"), POWER_STRING_FORMAT);
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
         motorCurrentGauges.add(new MinMaxGauge<Double>(RESOURCES.getString("label.motor") + " " + (i + 1), CURRENT_STRING_FORMAT));
         }
      for (int i = 0; i < SensorBoardConstants.BATTERY_DEVICE_COUNT; i++)
         {
         batteryVoltageGauges.add(new MinMaxGauge<Double>(RESOURCES.getString("label.battery") + " " + (i + 1), VOLTAGE_STRING_FORMAT));
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
            accessoryCurrentGauge.setValues(power.getCurrents().getAccessoryCurrent(),
                                            power.getMinimumCurrents().getAccessoryCurrent(),
                                            power.getMaximumCurrents().getAccessoryCurrent());
            batteryCurrentGauge.setValues(power.getCurrents().getBatteryCurrent(),
                                          power.getMinimumCurrents().getBatteryCurrent(),
                                          power.getMaximumCurrents().getBatteryCurrent());
            capacitorCurrentGauge.setValues(power.getCurrents().getCapacitorCurrent(),
                                            power.getMinimumCurrents().getCapacitorCurrent(),
                                            power.getMaximumCurrents().getCapacitorCurrent());
            for (int i = 0; i < SensorBoardConstants.MOTOR_DEVICE_COUNT; i++)
               {
               motorCurrentGauges.get(i).setValues(power.getCurrents().getMotorCurrent(i),
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
            accessoryVoltageGauge.setValues(power.getVoltages().getAccessoryVoltage(),
                                            power.getMinimumVoltages().getAccessoryVoltage(),
                                            power.getMaximumVoltages().getAccessoryVoltage());
            batteryVoltageGauge.setValues(power.getVoltages().getBatteryVoltage(),
                                          power.getMinimumVoltages().getBatteryVoltage(),
                                          power.getMaximumVoltages().getBatteryVoltage());
            capacitorVoltageGauge.setValues(power.getVoltages().getCapacitorVoltage(),
                                            power.getMinimumVoltages().getCapacitorVoltage(),
                                            power.getMaximumVoltages().getCapacitorVoltage());
            for (int i = 0; i < SensorBoardConstants.BATTERY_DEVICE_COUNT; i++)
               {
               batteryVoltageGauges.get(i).setValues(power.getVoltages().getBatteryVoltage(i),
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
      accessoryCurrentGauge.setValues(null, null, null);
      batteryCurrentGauge.setValues(null, null, null);
      capacitorCurrentGauge.setValues(null, null, null);
      for (int i = 0; i < SensorBoardConstants.MOTOR_DEVICE_COUNT; i++)
         {
         motorCurrentGauges.get(i).setValues(null, null, null);
         }
      }

   private void setUnknownVoltages()
      {
      accessoryVoltageGauge.setValues(null, null, null);
      batteryVoltageGauge.setValues(null, null, null);
      capacitorVoltageGauge.setValues(null, null, null);
      for (int i = 0; i < SensorBoardConstants.BATTERY_DEVICE_COUNT; i++)
         {
         batteryVoltageGauges.get(i).setValues(null, null, null);
         }
      }
   }
