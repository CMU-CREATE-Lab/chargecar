package edu.cmu.ri.createlab.commands;

import edu.cmu.ri.createlab.serial.CreateLabSerialDeviceNoReturnValueCommandStrategy;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class ResetDisplayCommandStrategy extends CreateLabSerialDeviceNoReturnValueCommandStrategy
   {
   /** The command character used to put the display back into start-up mode */
   private static final byte[] COMMAND = {'R'};

   protected byte[] getCommand()
      {
      return COMMAND.clone();
      }
   }
