package edu.cmu.ri.createlab.commands;

import edu.cmu.ri.createlab.serial.CreateLabSerialDeviceNoReturnValueCommandStrategy;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class DisconnectCommandStrategy extends CreateLabSerialDeviceNoReturnValueCommandStrategy
   {
   /** The pattern of characters to disconnect from the display and put it back into startup mode. */
   private static final byte[] COMMAND = {'Q'};

   protected byte[] getCommand()
      {
      return COMMAND.clone();
      }
   }
