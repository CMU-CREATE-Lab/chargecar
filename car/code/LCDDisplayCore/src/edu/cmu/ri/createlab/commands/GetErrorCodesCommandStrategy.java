package edu.cmu.ri.createlab.commands;

import edu.cmu.ri.createlab.LCDProxy;
import edu.cmu.ri.createlab.serial.SerialPortCommandResponse;
import edu.cmu.ri.createlab.util.ByteUtils;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class GetErrorCodesCommandStrategy extends ReturnValueCommandStrategy<Integer>
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

   protected int getSizeOfExpectedResponse()
      {
      return SIZE_IN_BYTES_OF_EXPECTED_RESPONSE;
      }

   protected byte[] getCommand()
      {
      return command.clone();
      }

   public Integer convertResult(final SerialPortCommandResponse result)
      {
      if (result != null && result.wasSuccessful())
         {
         final byte[] responseData = result.getData();

         if (responseData != null && responseData.length == SIZE_IN_BYTES_OF_EXPECTED_RESPONSE)
            {
                return ByteUtils.unsignedByteToInt(responseData[0]);
            }
         }

      return null;
      }
   }
