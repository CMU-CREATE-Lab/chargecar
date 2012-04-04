package org.chargecar.lcddisplay.commands;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import edu.cmu.ri.createlab.serial.CreateLabSerialDeviceReturnValueCommandStrategy;
import edu.cmu.ri.createlab.serial.SerialDeviceCommandResponse;
import edu.cmu.ri.createlab.util.ByteUtils;
import org.chargecar.lcddisplay.LCDProxy;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class GetRPMCommandStrategy extends CreateLabSerialDeviceReturnValueCommandStrategy<Integer>
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
         final ByteBuffer bb = ByteBuffer.wrap(responseData).order(ByteOrder.LITTLE_ENDIAN);

         if (responseData != null && responseData.length == SIZE_IN_BYTES_OF_EXPECTED_RESPONSE)
            {
            return (int)(bb.getShort());
            }
         }
      return null;
      }
   }
