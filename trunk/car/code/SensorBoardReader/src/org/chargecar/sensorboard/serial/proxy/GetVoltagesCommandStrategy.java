package org.chargecar.sensorboard.serial.proxy;

import java.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.sensorboard.SensorBoardConstants;
import org.chargecar.sensorboard.Voltages;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class GetVoltagesCommandStrategy extends ChargeCarSerialDeviceReturnValueCommandStrategy<Voltages>
   {
   private static final Log LOG = LogFactory.getLog(GetVoltagesCommandStrategy.class);

   /** The command character used to request the voltages. */
   private static final String COMMAND_PREFIX = "V";

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 49;

   /** The expected number of values in the response */
   private static final int NUM_EXPECTED_VALUES_IN_RESPONSE = 8;

   private static final double VOLTAGE_CONVERSION_FACTOR = 52.11;

   private final byte[] command;

   GetVoltagesCommandStrategy()
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

   protected Voltages convertResponseHelper(final String[] values)
      {
      return new VoltagesImpl(values);
      }

   protected int getNumberOfExpectedValuesInResponse()
      {
      return NUM_EXPECTED_VALUES_IN_RESPONSE;
      }

   private static final class VoltagesImpl implements Voltages
      {
      private final double[] batteryVoltages = new double[SensorBoardConstants.BATTERY_DEVICE_COUNT];
      private final double capacitorVoltage;
      private final double accessoryVoltage;
      private final double[] auxiliaryVoltages = new double[SensorBoardConstants.AUXILIARY_DEVICE_COUNT];

      private VoltagesImpl(final String[] rawValues)
         {
         batteryVoltages[0] = convertToVoltage(rawValues[5]);
         batteryVoltages[1] = convertToVoltage(rawValues[4]) - batteryVoltages[0];
         batteryVoltages[2] = convertToVoltage(rawValues[3]) - batteryVoltages[1];
         batteryVoltages[3] = convertToVoltage(rawValues[0]) - batteryVoltages[2];
         capacitorVoltage = convertToVoltage(rawValues[1]);
         accessoryVoltage = convertToVoltage(rawValues[2]);
         auxiliaryVoltages[0] = convertToVoltage(rawValues[6]);
         auxiliaryVoltages[1] = convertToVoltage(rawValues[7]);
         }

      private double convertToVoltage(final String rawValue)
         {
         double tempValue = 0.0;
         try
            {
            tempValue = Double.parseDouble(rawValue) / VOLTAGE_CONVERSION_FACTOR;
            }
         catch (NumberFormatException e)
            {
            LOG.error("GetVoltagesCommandStrategy$VoltagesImpl.convertToVoltage(): NumberFormatException while converting [" + rawValue + "] to a double.", e);
            }
         return tempValue;
         }

      public double getBatteryVoltage(final int batteryId)
         {
         if (batteryId >= 0 && batteryId < SensorBoardConstants.BATTERY_DEVICE_COUNT)
            {
            return batteryVoltages[batteryId];
            }
         throw new IllegalArgumentException("Invalid battery ID [" + batteryId + "], value must be a positive integer less than [" + SensorBoardConstants.BATTERY_DEVICE_COUNT + "]");
         }

      public double getCapacitorVoltage()
         {
         return capacitorVoltage;
         }

      public double getAccessoryVoltage()
         {
         return accessoryVoltage;
         }

      public double getAuxiliaryVoltage(final int auxiliaryDeviceId)
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
         sb.append("VoltagesImpl");
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
   }