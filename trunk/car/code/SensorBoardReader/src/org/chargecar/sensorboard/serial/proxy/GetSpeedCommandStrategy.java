package org.chargecar.sensorboard.serial.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class GetSpeedCommandStrategy extends ChargeCarSerialDeviceReturnValueCommandStrategy<Integer>
   {
   private static final Log LOG = LogFactory.getLog(GetSpeedCommandStrategy.class);

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

   protected Integer convertResponseHelper(final String[] values)
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
      return speed;
      }

   protected int getNumberOfExpectedValuesInResponse()
      {
      return NUM_EXPECTED_VALUES_IN_RESPONSE;
      }
   }
