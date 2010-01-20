package org.chargecar.sensorboard.serial.proxy;

import org.chargecar.sensorboard.Currents;
import org.chargecar.sensorboard.CurrentsImpl;

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
   }