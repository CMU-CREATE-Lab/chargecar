package edu.cmu.ri.createlab.commands;

import edu.cmu.ri.createlab.LCDProxy;
import edu.cmu.ri.createlab.serial.CreateLabSerialDeviceNoReturnValueCommandStrategy;
import edu.cmu.ri.createlab.util.ByteUtils;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */

public final class OutputCommandStrategy extends CreateLabSerialDeviceNoReturnValueCommandStrategy
   {
private final byte[] command;
   private static final byte COMMAND_PREFIX = 'O';

   public OutputCommandStrategy(final int outputNumber, final int state)
      {
      this.command = new byte[]{COMMAND_PREFIX,
                                ByteUtils.intToUnsignedByte(outputNumber),
                                ByteUtils.intToUnsignedByte(state),
                                ByteUtils.intToUnsignedByte(LCDProxy.SEQUENCE_NUMBER.next())};
      }

   protected byte[] getCommand()
      {
      return command.clone();
      }
   }
