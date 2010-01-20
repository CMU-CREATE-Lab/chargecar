package org.chargecar.sensorboard;

import java.util.Arrays;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class VoltagesImpl extends SensorBoardDataImpl implements Voltages
   {
   private static final double VOLTAGE_CONVERSION_FACTOR = 52.11;

   private final Double[] batteryVoltages = new Double[SensorBoardConstants.BATTERY_DEVICE_COUNT];
   private final Double capacitorVoltage;
   private final Double accessoryVoltage;
   private final Double[] auxiliaryVoltages = new Double[SensorBoardConstants.AUXILIARY_DEVICE_COUNT];

   /**
    * Copy constructor
    */
   public VoltagesImpl(final Voltages voltages)
      {
      for (int i = 0; i < batteryVoltages.length; i++)
         {
         batteryVoltages[i] = voltages.getBatteryVoltage(i);
         }
      capacitorVoltage = voltages.getCapacitorVoltage();
      accessoryVoltage = voltages.getAccessoryVoltage();
      for (int i = 0; i < auxiliaryVoltages.length; i++)
         {
         auxiliaryVoltages[i] = voltages.getAuxiliaryVoltage(i);
         }
      }

   public VoltagesImpl(final String[] rawValues)
      {
      batteryVoltages[0] = convertToVoltage(rawValues[5]);
      batteryVoltages[1] = convertToVoltage(rawValues[4]) - batteryVoltages[0];
      batteryVoltages[2] = convertToVoltage(rawValues[3]) - batteryVoltages[0] - batteryVoltages[1];
      batteryVoltages[3] = convertToVoltage(rawValues[0]) - batteryVoltages[0] - batteryVoltages[1] - batteryVoltages[2];
      capacitorVoltage = convertToVoltage(rawValues[1]);
      accessoryVoltage = convertToVoltage(rawValues[2]);
      auxiliaryVoltages[0] = convertToVoltage(rawValues[6]);
      auxiliaryVoltages[1] = convertToVoltage(rawValues[7]);
      }

   private double convertToVoltage(final String rawValue)
      {
      return convertToDouble(rawValue) / VOLTAGE_CONVERSION_FACTOR;
      }

   public Double getBatteryVoltage(final int batteryId)
      {
      if (batteryId >= 0 && batteryId < SensorBoardConstants.BATTERY_DEVICE_COUNT)
         {
         return batteryVoltages[batteryId];
         }
      throw new IllegalArgumentException("Invalid battery ID [" + batteryId + "], value must be a positive integer less than [" + SensorBoardConstants.BATTERY_DEVICE_COUNT + "]");
      }

   public Double getBatteryVoltage()
      {
      double sum = 0.0;
      for (final Double voltage : batteryVoltages)
         {
         if (voltage == null)
            {
            return null;
            }
         else
            {
            sum += voltage;
            }
         }
      return sum;
      }

   public Double getCapacitorVoltage()
      {
      return capacitorVoltage;
      }

   public Double getAccessoryVoltage()
      {
      return accessoryVoltage;
      }

   public Double getAuxiliaryVoltage(final int auxiliaryDeviceId)
      {
      if (auxiliaryDeviceId >= 0 && auxiliaryDeviceId < SensorBoardConstants.AUXILIARY_DEVICE_COUNT)
         {
         return auxiliaryVoltages[auxiliaryDeviceId];
         }
      throw new IllegalArgumentException("Invalid auxiliary device ID [" + auxiliaryDeviceId + "], value must be a positive integer less than [" + SensorBoardConstants.AUXILIARY_DEVICE_COUNT + "]");
      }

   @Override
   public boolean equals(final Object o)
      {
      if (this == o)
         {
         return true;
         }
      if (o == null || getClass() != o.getClass())
         {
         return false;
         }

      final VoltagesImpl voltages = (VoltagesImpl)o;

      if (Double.compare(voltages.accessoryVoltage, accessoryVoltage) != 0)
         {
         return false;
         }
      if (Double.compare(voltages.capacitorVoltage, capacitorVoltage) != 0)
         {
         return false;
         }
      if (!Arrays.equals(auxiliaryVoltages, voltages.auxiliaryVoltages))
         {
         return false;
         }
      if (!Arrays.equals(batteryVoltages, voltages.batteryVoltages))
         {
         return false;
         }

      return true;
      }

   @Override
   public int hashCode()
      {
      int result;
      long temp;
      result = batteryVoltages != null ? Arrays.hashCode(batteryVoltages) : 0;
      temp = capacitorVoltage != +0.0d ? Double.doubleToLongBits(capacitorVoltage) : 0L;
      result = 31 * result + (int)(temp ^ (temp >>> 32));
      temp = accessoryVoltage != +0.0d ? Double.doubleToLongBits(accessoryVoltage) : 0L;
      result = 31 * result + (int)(temp ^ (temp >>> 32));
      result = 31 * result + (auxiliaryVoltages != null ? Arrays.hashCode(auxiliaryVoltages) : 0);
      return result;
      }

   @Override
   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("Voltages");
      sb.append("{batteryVoltages=").append(batteryVoltages == null ? "null" : "");
      for (int i = 0; batteryVoltages != null && i < batteryVoltages.length; ++i)
         {
         sb.append(i == 0 ? "" : ", ").append(batteryVoltages[i]);
         }
      sb.append(", capacitorVoltage=").append(capacitorVoltage);
      sb.append(", accessoryVoltage=").append(accessoryVoltage);
      sb.append(", auxiliaryVoltages=").append(auxiliaryVoltages == null ? "null" : "");
      for (int i = 0; auxiliaryVoltages != null && i < auxiliaryVoltages.length; ++i)
         {
         sb.append(i == 0 ? "" : ", ").append(auxiliaryVoltages[i]);
         }
      sb.append('}');
      return sb.toString();
      }
   }
