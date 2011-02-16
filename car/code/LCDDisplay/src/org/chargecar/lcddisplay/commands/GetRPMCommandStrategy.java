package org.chargecar.lcddisplay.commands;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import edu.cmu.ri.createlab.serial.SerialPortCommandResponse;
import edu.cmu.ri.createlab.util.ByteUtils;
import org.chargecar.lcddisplay.LCDProxy;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class GetRPMCommandStrategy extends ReturnValueCommandStrategy<Integer>
   {
   /** The command character used to request the RPMMenuItemAction value */
private static final byte COMMAND_PREFIX = 'S';

   private final byte[] command;

   public GetRPMCommandStrategy()
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

   public Integer convertResult(final SerialPortCommandResponse result)
      {
      if (result != null && result.wasSuccessful())
         {
         final byte[] responseData = result.getData();
         final ByteBuffer bb = ByteBuffer.wrap(responseData).order(ByteOrder.LITTLE_ENDIAN);

         if (responseData != null && responseData.length == SIZE_IN_BYTES_OF_EXPECTED_RESPONSE)
            {
                return (int)(bb.getShort());
            }
         }
      return null;
      }
   }
