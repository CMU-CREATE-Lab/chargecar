package org.chargecar.lcddisplay.commands;

import edu.cmu.ri.createlab.serial.CreateLabSerialDeviceReturnValueCommandStrategy;
import edu.cmu.ri.createlab.serial.SerialDeviceCommandResponse;
import edu.cmu.ri.createlab.util.ByteUtils;
import org.chargecar.lcddisplay.LCDProxy;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class GetErrorCodesCommandStrategy extends CreateLabSerialDeviceReturnValueCommandStrategy<Integer>
   {
   /** The command character used to request error codes from the motor controller */
   private static final byte COMMAND_PREFIX = 'E';

   private final byte[] command;

   public GetErrorCodesCommandStrategy()
      {
      this.command = new byte[]{COMMAND_PREFIX,
                                ByteUtils.intToUnsignedByte(LCDProxy.SEQUENCE_NUMBER.next())};
      }

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 1;

   @Override
   protected int getSizeOfExpectedResponse()
      {
      return SIZE_IN_BYTES_OF_EXPECTED_RESPONSE;
      }

   @Override
   protected byte[] getCommand()
      {
      return command.clone();
      }

   @Override
   public Integer convertResponse(final SerialDeviceCommandResponse response)
      {
      if (response != null && response.wasSuccessful())
         {
         final byte[] responseData = response.getData();

         if (responseData != null && responseData.length == SIZE_IN_BYTES_OF_EXPECTED_RESPONSE)
            {
            return ByteUtils.unsignedByteToInt(responseData[0]);
            }
         }

      return null;
      }
   }
