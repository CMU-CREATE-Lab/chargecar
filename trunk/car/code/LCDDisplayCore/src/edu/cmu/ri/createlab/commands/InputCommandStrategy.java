package edu.cmu.ri.createlab.commands;

import edu.cmu.ri.createlab.LCDProxy;
import edu.cmu.ri.createlab.serial.SerialPortCommandResponse;
import edu.cmu.ri.createlab.util.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class InputCommandStrategy extends ReturnValueCommandStrategy<int[]>
   {
   /** The command character used to request the RPMMenuItemAction value */
   private static final byte COMMAND_PREFIX = 'I';

   private final byte[] command;

   public InputCommandStrategy()
      {
      this.command = new byte[]{COMMAND_PREFIX,
                                ByteUtils.intToUnsignedByte(LCDProxy.SEQUENCE_NUMBER.next())};
      }

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 2;

   protected int getSizeOfExpectedResponse()
      {
      return SIZE_IN_BYTES_OF_EXPECTED_RESPONSE;
      }

   protected byte[] getCommand()
      {
      return command.clone();
      }

   public int[] convertResult(final SerialPortCommandResponse result)
      {
      if (result != null && result.wasSuccessful())
         {
         final byte[] responseData = result.getData();
         final ByteBuffer bb = ByteBuffer.wrap(responseData).order(ByteOrder.LITTLE_ENDIAN);

         if (responseData != null && responseData.length == SIZE_IN_BYTES_OF_EXPECTED_RESPONSE)
            {
            return new int[]{ByteUtils.unsignedByteToInt(bb.get(0)),
                             ByteUtils.unsignedByteToInt(bb.get(1))};
            }
         }

      return null;
      }
   }
