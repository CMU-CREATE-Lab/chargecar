package org.chargecar.sensorboard.serial.proxy;

import java.util.Arrays;
import org.chargecar.sensorboard.SensorBoardConstants;
import org.chargecar.sensorboard.SensorBoardDataImpl;
import org.chargecar.sensorboard.Temperatures;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class GetTemperaturesCommandStrategy extends ChargeCarSerialDeviceReturnValueCommandStrategy<Temperatures>
   {
   /** The command character used to request the temperatures. */
   private static final String COMMAND_PREFIX = "T";

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 78;

   /** The expected number of values in the response */
   private static final int NUM_EXPECTED_VALUES_IN_RESPONSE = 11;

   private final byte[] command;

   GetTemperaturesCommandStrategy()
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

   protected Temperatures convertResponseHelper(final String[] values)
      {
      return new TemperaturesImpl(values);
      }

   protected int getNumberOfExpectedValuesInResponse()
      {
      return NUM_EXPECTED_VALUES_IN_RESPONSE;
      }

   private final class TemperaturesImpl extends SensorBoardDataImpl implements Temperatures
      {
      private final double[] motorTemperatures = new double[SensorBoardConstants.MOTOR_DEVICE_COUNT];
      private final double capacitorTemperature;
      private final double batteryTemperature;
      private final double[] motorControllerTemperatures = new double[SensorBoardConstants.MOTOR_CONTROLLER_DEVICE_COUNT];
      private final double outsideTemperature;
      private final double[] auxiliaryTemperatures = new double[SensorBoardConstants.AUXILIARY_DEVICE_COUNT];

      private TemperaturesImpl(final String[] rawValues)
         {
         motorTemperatures[0] = convertToDouble(rawValues[0]);
         motorTemperatures[1] = convertToDouble(rawValues[1]);
         motorTemperatures[2] = convertToDouble(rawValues[2]);
         motorTemperatures[3] = convertToDouble(rawValues[3]);
         capacitorTemperature = computeCapacitorTemperature(convertToDouble(rawValues[4]));
         batteryTemperature = convertToDouble(rawValues[5]);
         motorControllerTemperatures[0] = convertToDouble(rawValues[6]);
         motorControllerTemperatures[1] = convertToDouble(rawValues[7]);
         outsideTemperature = convertToDouble(rawValues[8]);
         auxiliaryTemperatures[0] = convertToDouble(rawValues[9]);
         auxiliaryTemperatures[1] = convertToDouble(rawValues[10]);
         }

      private double computeCapacitorTemperature(final double rawValue)
         {
         return -15.477 * Math.pow(rawValue, 3) + 88.368 * Math.pow(rawValue, 2) - 216.24 * rawValue + 294.35;
         }

      public double getMotorTemperature(final int motorId)
         {
         if (motorId >= 0 && motorId < SensorBoardConstants.MOTOR_DEVICE_COUNT)
            {
            return motorTemperatures[motorId];
            }
         throw new IllegalArgumentException("Invalid motor ID [" + motorId + "], value must be a positive integer less than [" + SensorBoardConstants.MOTOR_DEVICE_COUNT + "]");
         }

      public double getMotorControllerTemperature(final int motorControllerId)
         {
         if (motorControllerId >= 0 && motorControllerId < SensorBoardConstants.MOTOR_CONTROLLER_DEVICE_COUNT)
            {
            return motorControllerTemperatures[motorControllerId];
            }
         throw new IllegalArgumentException("Invalid motor controller ID [" + motorControllerId + "], value must be a positive integer less than [" + SensorBoardConstants.MOTOR_CONTROLLER_DEVICE_COUNT + "]");
         }

      public double getCapacitorTemperature()
         {
         return capacitorTemperature;
         }

      public double getBatteryTemperature()
         {
         return batteryTemperature;
         }

      public double getOutsideTemperature()
         {
         return outsideTemperature;
         }

      public double getAuxiliaryTemperature(final int auxiliaryDeviceId)
         {
         if (auxiliaryDeviceId >= 0 && auxiliaryDeviceId < SensorBoardConstants.AUXILIARY_DEVICE_COUNT)
            {
            return auxiliaryTemperatures[auxiliaryDeviceId];
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

         final TemperaturesImpl that = (TemperaturesImpl)o;

         if (Double.compare(that.batteryTemperature, batteryTemperature) != 0)
            {
            return false;
            }
         if (Double.compare(that.capacitorTemperature, capacitorTemperature) != 0)
            {
            return false;
            }
         if (Double.compare(that.outsideTemperature, outsideTemperature) != 0)
            {
            return false;
            }
         if (!Arrays.equals(auxiliaryTemperatures, that.auxiliaryTemperatures))
            {
            return false;
            }
         if (!Arrays.equals(motorControllerTemperatures, that.motorControllerTemperatures))
            {
            return false;
            }
         if (!Arrays.equals(motorTemperatures, that.motorTemperatures))
            {
            return false;
            }

         return true;
         }

      public int hashCode()
         {
         int result;
         long temp;
         result = motorTemperatures != null ? Arrays.hashCode(motorTemperatures) : 0;
         temp = capacitorTemperature != +0.0d ? Double.doubleToLongBits(capacitorTemperature) : 0L;
         result = 31 * result + (int)(temp ^ (temp >>> 32));
         temp = batteryTemperature != +0.0d ? Double.doubleToLongBits(batteryTemperature) : 0L;
         result = 31 * result + (int)(temp ^ (temp >>> 32));
         result = 31 * result + (motorControllerTemperatures != null ? Arrays.hashCode(motorControllerTemperatures) : 0);
         temp = outsideTemperature != +0.0d ? Double.doubleToLongBits(outsideTemperature) : 0L;
         result = 31 * result + (int)(temp ^ (temp >>> 32));
         result = 31 * result + (auxiliaryTemperatures != null ? Arrays.hashCode(auxiliaryTemperatures) : 0);
         return result;
         }

      public String toString()
         {
         final StringBuilder sb = new StringBuilder();
         sb.append("Temperatures");
         sb.append("{motorTemperatures=").append(motorTemperatures == null ? "null" : "");
         for (int i = 0; motorTemperatures != null && i < motorTemperatures.length; ++i)
            {
            sb.append(i == 0 ? "" : ", ").append(motorTemperatures[i]);
            }
         sb.append(", capacitorTemperature=").append(capacitorTemperature);
         sb.append(", batteryTemperature=").append(batteryTemperature);
         sb.append(", motorControllerTemperatures=").append(motorControllerTemperatures == null ? "null" : "");
         for (int i = 0; motorControllerTemperatures != null && i < motorControllerTemperatures.length; ++i)
            {
            sb.append(i == 0 ? "" : ", ").append(motorControllerTemperatures[i]);
            }
         sb.append(", outsideTemperature=").append(outsideTemperature);
         sb.append(", auxiliaryTemperatures=").append(auxiliaryTemperatures == null ? "null" : "");
         for (int i = 0; auxiliaryTemperatures != null && i < auxiliaryTemperatures.length; ++i)
            {
            sb.append(i == 0 ? "" : ", ").append(auxiliaryTemperatures[i]);
            }
         sb.append('}');
         return sb.toString();
         }
      }
   }