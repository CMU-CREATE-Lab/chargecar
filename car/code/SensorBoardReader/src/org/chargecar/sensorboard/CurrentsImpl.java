package org.chargecar.sensorboard;

import java.util.Arrays;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class CurrentsImpl extends SensorBoardDataImpl implements Currents
   {
   private static final double STANDARD_CURRENT_CONVERSION_FACTOR = -1.44;
   private static final int MOTOR_CURRENT_CONVERSION_FACTOR = 5;

   private final Double batteryCurrent;
   private final Double capacitorCurrent;
   private final Double accessoryCurrent;
   private final Double[] motorCurrents = new Double[SensorBoardConstants.MOTOR_DEVICE_COUNT];

   /**
    * Copy constructor
    */
   public CurrentsImpl(final Currents currents)
      {
      batteryCurrent = currents.getBatteryCurrent();
      capacitorCurrent = currents.getCapacitorCurrent();
      accessoryCurrent = currents.getAccessoryCurrent();
      for (int i = 0; i < motorCurrents.length; i++)
         {
         motorCurrents[i] = currents.getMotorCurrent(i);
         }
      }

   public CurrentsImpl(final String[] rawValues)
      {
      final double batteryCurrentTemp = convertToDouble(rawValues[0]) / STANDARD_CURRENT_CONVERSION_FACTOR;
      final double capacitorCurrentTemp = convertToDouble(rawValues[1]) / STANDARD_CURRENT_CONVERSION_FACTOR;
      accessoryCurrent = convertToDouble(rawValues[2]) / STANDARD_CURRENT_CONVERSION_FACTOR;
      motorCurrents[0] = convertToDouble(rawValues[3]) / MOTOR_CURRENT_CONVERSION_FACTOR;
      motorCurrents[1] = convertToDouble(rawValues[4]) / MOTOR_CURRENT_CONVERSION_FACTOR;
      motorCurrents[2] = convertToDouble(rawValues[5]) / MOTOR_CURRENT_CONVERSION_FACTOR;
      motorCurrents[3] = convertToDouble(rawValues[6]) / MOTOR_CURRENT_CONVERSION_FACTOR;

      // adjust battery current
      if (Double.compare(-25, batteryCurrentTemp) < 0 && Double.compare(batteryCurrentTemp, 25) < 0)
         {
         batteryCurrent = -1.5;
         }
      else
         {
         batteryCurrent = batteryCurrentTemp;
         }

      // adjust capacitor current
      if (Double.compare(-25, capacitorCurrentTemp) < 0 && Double.compare(capacitorCurrentTemp, 25) < 0)
         {
         capacitorCurrent = 0.0;
         }
      else
         {
         capacitorCurrent = capacitorCurrentTemp;
         }
      }

   public Double getBatteryCurrent()
      {
      return batteryCurrent;
      }

   public Double getCapacitorCurrent()
      {
      return capacitorCurrent;
      }

   public Double getAccessoryCurrent()
      {
      return accessoryCurrent;
      }

   public Double getMotorCurrent(final int motorId)
      {
      if (motorId >= 0 && motorId < SensorBoardConstants.MOTOR_DEVICE_COUNT)
         {
         return motorCurrents[motorId];
         }
      throw new IllegalArgumentException("Invalid motor ID [" + motorId + "], value must be a positive integer less than [" + SensorBoardConstants.MOTOR_DEVICE_COUNT + "]");
      }

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

      final CurrentsImpl currents = (CurrentsImpl)o;

      if (Double.compare(currents.accessoryCurrent, accessoryCurrent) != 0)
         {
         return false;
         }
      if (Double.compare(currents.batteryCurrent, batteryCurrent) != 0)
         {
         return false;
         }
      if (Double.compare(currents.capacitorCurrent, capacitorCurrent) != 0)
         {
         return false;
         }
      if (!Arrays.equals(motorCurrents, currents.motorCurrents))
         {
         return false;
         }

      return true;
      }

   public int hashCode()
      {
      int result;
      long temp;
      temp = batteryCurrent != +0.0d ? Double.doubleToLongBits(batteryCurrent) : 0L;
      result = (int)(temp ^ (temp >>> 32));
      temp = capacitorCurrent != +0.0d ? Double.doubleToLongBits(capacitorCurrent) : 0L;
      result = 31 * result + (int)(temp ^ (temp >>> 32));
      temp = accessoryCurrent != +0.0d ? Double.doubleToLongBits(accessoryCurrent) : 0L;
      result = 31 * result + (int)(temp ^ (temp >>> 32));
      result = 31 * result + (motorCurrents != null ? Arrays.hashCode(motorCurrents) : 0);
      return result;
      }

   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("Currents");
      sb.append("{batteryCurrent=").append(batteryCurrent);
      sb.append(", capacitorCurrent=").append(capacitorCurrent);
      sb.append(", accessoryCurrent=").append(accessoryCurrent);
      sb.append(", motorCurrents=").append(motorCurrents == null ? "null" : "");
      for (int i = 0; motorCurrents != null && i < motorCurrents.length; ++i)
         {
         sb.append(i == 0 ? "" : ", ").append(motorCurrents[i]);
         }
      sb.append('}');
      return sb.toString();
      }
   }
