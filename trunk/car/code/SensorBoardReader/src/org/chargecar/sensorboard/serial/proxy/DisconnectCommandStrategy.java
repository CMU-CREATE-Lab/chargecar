package org.chargecar.sensorboard.serial.proxy;

import edu.cmu.ri.createlab.serial.CreateLabSerialDeviceNoReturnValueCommandStrategy;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class DisconnectCommandStrategy extends CreateLabSerialDeviceNoReturnValueCommandStrategy
   {
   /** The pattern of characters to disconnect from the finch and put it back into startup mode. */
   private static final byte[] COMMAND = {'R'};

   protected byte[] getCommand()
      {
      return COMMAND.clone();
      }
   }
