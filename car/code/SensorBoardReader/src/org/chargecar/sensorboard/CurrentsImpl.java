package org.chargecar.sensorboard;

import java.util.Arrays;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class CurrentsImpl extends SensorBoardDataImpl implements Currents
   {
   private static final int ACCESSORY_CURRENT_CONVERSION_FACTOR = 15;
   private static final int MOTOR_CURRENT_CONVERSION_FACTOR = 5;

   private static final double[] DC_OFFSETS = new double[]{-18.8,       // battery
                                                           0,           // aux 1
                                                           -44.3,       // accessory
                                                           -6.9,        // motor 1
                                                           -6.9,        // motor 2
                                                           -10.9,       // motor 3
                                                           -9.75,       // motor 4
                                                           0,           // aux 2
                                                           -4.5};       // capacitor

   private final Double batteryCurrent;
   private final Double capacitorCurrent;
   private final Double accessoryCurrent;
   private final Double[] motorCurrents = new Double[SensorBoardConstants.MOTOR_DEVICE_COUNT];
   private final Double[] auxiliaryCurrents = new Double[SensorBoardConstants.AUXILIARY_DEVICE_COUNT];

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
      for (int i = 0; i < auxiliaryCurrents.length; i++)
         {
         auxiliaryCurrents[i] = currents.getAuxiliaryCurrent(i);
         }
      }

   public CurrentsImpl(final String[] rawValues)
      {
      batteryCurrent = convertToDouble(rawValues[0]) + DC_OFFSETS[0];
      auxiliaryCurrents[0] = convertToDouble(rawValues[1]) + DC_OFFSETS[1];
      accessoryCurrent = (convertToDouble(rawValues[2]) + DC_OFFSETS[2]) / ACCESSORY_CURRENT_CONVERSION_FACTOR;
      motorCurrents[0] = (convertToDouble(rawValues[3]) + DC_OFFSETS[3]) / MOTOR_CURRENT_CONVERSION_FACTOR;
      motorCurrents[1] = (convertToDouble(rawValues[4]) + DC_OFFSETS[4]) / MOTOR_CURRENT_CONVERSION_FACTOR;
      motorCurrents[2] = (convertToDouble(rawValues[5]) + DC_OFFSETS[5]) / MOTOR_CURRENT_CONVERSION_FACTOR;
      motorCurrents[3] = (convertToDouble(rawValues[6]) + DC_OFFSETS[6]) / MOTOR_CURRENT_CONVERSION_FACTOR;
      auxiliaryCurrents[1] = convertToDouble(rawValues[7]) + DC_OFFSETS[7];
      capacitorCurrent = convertToDouble(rawValues[8]) + DC_OFFSETS[8];
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

   public Double getAuxiliaryCurrent(final int auxiliaryDeviceId)
      {
      if (auxiliaryDeviceId >= 0 && auxiliaryDeviceId < SensorBoardConstants.AUXILIARY_DEVICE_COUNT)
         {
         return auxiliaryCurrents[auxiliaryDeviceId];
         }
      throw new IllegalArgumentException("Invalid auxiliary device ID [" + auxiliaryDeviceId + "], value must be a positive integer less than [" + SensorBoardConstants.AUXILIARY_DEVICE_COUNT + "]");
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
      if (!Arrays.equals(auxiliaryCurrents, currents.auxiliaryCurrents))
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
      result = 31 * result + (auxiliaryCurrents != null ? Arrays.hashCode(auxiliaryCurrents) : 0);
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
      sb.append(", auxiliaryCurrents=").append(auxiliaryCurrents == null ? "null" : "");
      for (int i = 0; auxiliaryCurrents != null && i < auxiliaryCurrents.length; ++i)
         {
         sb.append(i == 0 ? "" : ", ").append(auxiliaryCurrents[i]);
         }
      sb.append('}');
      return sb.toString();
      }
   }
