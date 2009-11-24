package org.chargecar.sensorboard.serial.proxy;

import edu.cmu.ri.createlab.serial.CreateLabSerialDeviceReturnValueCommandStrategy;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class GetSpeedCommandStrategy extends CreateLabSerialDeviceReturnValueCommandStrategy
   {
   /** The command character used to request the speed. */
   private static final String COMMAND_PREFIX = "S";

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 16;

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
   }
