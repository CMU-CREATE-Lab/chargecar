package org.chargecar.sensorboard.serial.proxy;

import org.chargecar.sensorboard.PedalPositions;
import org.chargecar.sensorboard.SensorBoardDataImpl;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class GetPedalPositionsCommandStrategy extends ChargeCarSerialDeviceReturnValueCommandStrategy<PedalPositions>
   {
   /** The command character used to request the pedal positions. */
   private static final String COMMAND_PREFIX = "P";

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 13;

   /** The expected number of values in the response */
   private static final int NUM_EXPECTED_VALUES_IN_RESPONSE = 2;

   private final byte[] command;

   GetPedalPositionsCommandStrategy()
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

   protected PedalPositions convertResponseHelper(final String[] values)
      {
      return new PedalPositionsImpl(values);
      }

   protected int getNumberOfExpectedValuesInResponse()
      {
      return NUM_EXPECTED_VALUES_IN_RESPONSE;
      }

   private final class PedalPositionsImpl extends SensorBoardDataImpl implements PedalPositions
      {
      private static final String TO_STRING_DELIMITER = "\t";

      private final double throttlePosition;
      private final double regenBrakePosition;

      private PedalPositionsImpl(final String[] rawValues)
         {
         throttlePosition = Math.max(0, 0.495 * convertToDouble(rawValues[0]) - 2.475);
         regenBrakePosition = Math.max(0, 0.65 * convertToDouble(rawValues[1]) - 33.3);
         }

      public double getThrottlePosition()
         {
         return throttlePosition;
         }

      public double getRegenBrakePosition()
         {
         return regenBrakePosition;
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

         final PedalPositionsImpl that = (PedalPositionsImpl)o;

         if (Double.compare(that.regenBrakePosition, regenBrakePosition) != 0)
            {
            return false;
            }
         if (Double.compare(that.throttlePosition, throttlePosition) != 0)
            {
            return false;
            }

         return true;
         }

      public int hashCode()
         {
         int result;
         long temp;
         temp = throttlePosition != +0.0d ? Double.doubleToLongBits(throttlePosition) : 0L;
         result = (int)(temp ^ (temp >>> 32));
         temp = regenBrakePosition != +0.0d ? Double.doubleToLongBits(regenBrakePosition) : 0L;
         result = 31 * result + (int)(temp ^ (temp >>> 32));
         return result;
         }

      public String toString()
         {
         return toString("throttlePosition=", ", regenBrakePosition=");
         }

      public String toLoggingString()
         {
         return toString("", TO_STRING_DELIMITER);
         }

      private String toString(final String field1, final String field2)
         {
         final StringBuilder sb = new StringBuilder();
         sb.append("PedalPositions");
         sb.append("{");
         sb.append(field1).append(throttlePosition);
         sb.append(field2).append(regenBrakePosition);
         sb.append('}');
         return sb.toString();
         }
      }
   }