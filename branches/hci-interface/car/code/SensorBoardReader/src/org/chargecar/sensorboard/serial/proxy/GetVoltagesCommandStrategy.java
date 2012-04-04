package org.chargecar.sensorboard.serial.proxy;

import org.chargecar.sensorboard.Voltages;
import org.chargecar.sensorboard.VoltagesImpl;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class GetVoltagesCommandStrategy extends ChargeCarSerialDeviceReturnValueCommandStrategy<Voltages>
   {
   /** The command character used to request the voltages. */
   private static final String COMMAND_PREFIX = "V";

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 37;

   /** The expected number of values in the response */
   private static final int NUM_EXPECTED_VALUES_IN_RESPONSE = 6;

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
   }