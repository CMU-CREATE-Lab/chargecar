package org.chargecar.sensorboard.serial.proxy;

import org.apache.log4j.Logger;
import org.chargecar.sensorboard.SensorBoardDataImpl;
import org.chargecar.sensorboard.Speed;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class GetSpeedCommandStrategy extends ChargeCarSerialDeviceReturnValueCommandStrategy<Speed>
   {
   private static final Logger LOG = Logger.getLogger(GetSpeedCommandStrategy.class);

   /** The command character used to request the speed. */
   private static final String COMMAND_PREFIX = "S";

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 7;

   /** The expected number of values in the response */
   private static final int NUM_EXPECTED_VALUES_IN_RESPONSE = 1;

   private final byte[] command;

   GetSpeedCommandStrategy()
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

   protected Speed convertResponseHelper(final String[] values)
      {
      Integer speed = null;
      try
         {
         speed = Integer.parseInt(values[0]);
         }
      catch (NumberFormatException e)
         {
         LOG.error("GetSpeedCommandStrategy.convertResponseHelper(): NumberFormatException while converting [" + values[0] + "] to an int.  Returning [" + speed + "] instead.", e);
         }
      return new SpeedImpl(speed);
      }

   protected int getNumberOfExpectedValuesInResponse()
      {
      return NUM_EXPECTED_VALUES_IN_RESPONSE;
      }

   private final class SpeedImpl extends SensorBoardDataImpl implements Speed
      {
      private final Integer speed;

      private SpeedImpl(final Integer speed)
         {
         this.speed = speed;
         }

      public Integer getSpeed()
         {
         return speed;
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

         final SpeedImpl speed1 = (SpeedImpl)o;

         if (speed != null ? !speed.equals(speed1.speed) : speed1.speed != null)
            {
            return false;
            }

         return true;
         }

      @Override
      public int hashCode()
         {
         return speed != null ? speed.hashCode() : 0;
         }

      @Override
      public String toString()
         {
         final StringBuilder sb = new StringBuilder();
         sb.append("Speed");
         sb.append("{speed=").append(speed);
         sb.append('}');
         return sb.toString();
         }
      }
   }
