package org.chargecar.sensorboard.serial.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class IsCapacitorOverVoltageCommandStrategy extends ChargeCarSerialDeviceReturnValueCommandStrategy<Boolean>
   {
   private static final Log LOG = LogFactory.getLog(IsCapacitorOverVoltageCommandStrategy.class);

   /** The command character used to request the speed. */
   private static final String COMMAND_PREFIX = "S";

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 7;

   /** The expected number of values in the response */
   private static final int NUM_EXPECTED_VALUES_IN_RESPONSE = 1;

   private final byte[] command;

   IsCapacitorOverVoltageCommandStrategy()
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

   protected Boolean convertResponseHelper(final String[] values)
      {
      Boolean value = null;
      try
         {
         value = Integer.parseInt(values[0]) == 0;
         }
      catch (NumberFormatException e)
         {
         LOG.error("IsCapacitorOverVoltageCommandStrategy.convertResponseHelper(): NumberFormatException while converting [" + values[0] + "] to an int.  Returning [" + value + "] instead.", e);
         }
      return value;
      }

   protected int getNumberOfExpectedValuesInResponse()
      {
      return NUM_EXPECTED_VALUES_IN_RESPONSE;
      }
   }