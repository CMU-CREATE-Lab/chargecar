package org.chargecar.sensorboard.serial.proxy;

import edu.cmu.ri.createlab.serial.SerialPortCommandResponse;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class GetSpeedCommandStrategy extends ChargeCarSerialDeviceReturnValueCommandStrategy<Integer>
   {
   /** The command character used to request the speed. */
   private static final String COMMAND_PREFIX = "S";

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 7;

   private final byte[] command;
   private final ResponseConversionStrategy<Integer> responseConversionStrategy =
         new ResponseConversionStrategy<Integer>()
         {
         public Integer convert(final String[] values)
            {
            return Integer.parseInt(values[0].trim());
            }
         };

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

   public Integer convertResponse(final SerialPortCommandResponse response)
      {
      return convertResponse(response, responseConversionStrategy);
      }
   }
