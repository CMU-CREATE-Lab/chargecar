package org.chargecar.sensorboard.serial.proxy;

import java.util.Arrays;
import org.chargecar.sensorboard.Currents;
import org.chargecar.sensorboard.SensorBoardConstants;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class GetCurrentsCommandStrategy extends ChargeCarSerialDeviceReturnValueCommandStrategy<Currents>
   {
   /** The command character used to request the currents. */
   private static final String COMMAND_PREFIX = "C";

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 64;

   /** The expected number of values in the response */
   private static final int NUM_EXPECTED_VALUES_IN_RESPONSE = 9;

   private static final int ACCESSORY_CURRENT_CONVERSION_FACTOR = 15;
   private static final int MOTOR_CURRENT_CONVERSION_FACTOR = 5;

   private static final double[] DC_OFFSETS = new double[]{-8.0, -9.4, -39.4, -2.73, -2.13, 0.8, 5.87, 0, 0};

   private final byte[] command;

   GetCurrentsCommandStrategy()
      {
      this.command = COMMAND_PREFIX.getBytes();
      }

   protected int getSizeOfExpectedResponse()
      {
      return SIZE_IN_BYTES_OF_EXPECTED_RESPONSE;
      }

   protected byte[] getCommand()
      {
      return command.clone();
      }

   protected Currents convertResponseHelper(final String[] values)
      {
      return new CurrentsImpl(values);
      }

   protected int getNumberOfExpectedValuesInResponse()
      {
      return NUM_EXPECTED_VALUES_IN_RESPONSE;
      }

   private final class CurrentsImpl implements Currents
      {
      private final double batteryCurrent;
      private final double capacitorCurrent;
      private final double accessoryCurrent;
      private final double[] motorCurrents = new double[SensorBoardConstants.MOTOR_DEVICE_COUNT];
      private final double[] auxiliaryCurrents = new double[SensorBoardConstants.AUXILIARY_DEVICE_COUNT];

      private CurrentsImpl(final String[] rawValues)
         {
         batteryCurrent = convertToDouble(rawValues[0]) + DC_OFFSETS[0];
         capacitorCurrent = convertToDouble(rawValues[1]) + DC_OFFSETS[1];
         accessoryCurrent = (convertToDouble(rawValues[2]) + DC_OFFSETS[2]) / ACCESSORY_CURRENT_CONVERSION_FACTOR;
         motorCurrents[0] = (convertToDouble(rawValues[3]) + DC_OFFSETS[3]) / MOTOR_CURRENT_CONVERSION_FACTOR;
         motorCurrents[1] = (convertToDouble(rawValues[4]) + DC_OFFSETS[4]) / MOTOR_CURRENT_CONVERSION_FACTOR;
         motorCurrents[2] = (convertToDouble(rawValues[5]) + DC_OFFSETS[5]) / MOTOR_CURRENT_CONVERSION_FACTOR;
         motorCurrents[3] = (convertToDouble(rawValues[6]) + DC_OFFSETS[6]) / MOTOR_CURRENT_CONVERSION_FACTOR;
         auxiliaryCurrents[0] = convertToDouble(rawValues[7]) + DC_OFFSETS[7];
         auxiliaryCurrents[1] = convertToDouble(rawValues[8]) + DC_OFFSETS[8];
         }

      public double getBatteryCurrent()
         {
         return batteryCurrent;
         }

      public double getCapacitorCurrent()
         {
         return capacitorCurrent;
         }

      public double getAccessoryCurrent()
         {
         return accessoryCurrent;
         }

      public double getMotorCurrent(final int motorId)
         {
         if (motorId >= 0 && motorId < SensorBoardConstants.MOTOR_DEVICE_COUNT)
            {
            return motorCurrents[motorId];
            }
         throw new IllegalArgumentException("Invalid motor ID [" + motorId + "], value must be a positive integer less than [" + SensorBoardConstants.MOTOR_DEVICE_COUNT + "]");
         }

      public double getAuxiliaryCurrent(final int auxiliaryDeviceId)
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
   }