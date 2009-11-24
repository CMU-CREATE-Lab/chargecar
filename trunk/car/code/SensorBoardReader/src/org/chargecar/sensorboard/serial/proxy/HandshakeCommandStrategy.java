package org.chargecar.sensorboard.serial.proxy;

import edu.cmu.ri.createlab.serial.CreateLabSerialDeviceHandshakeCommandStrategy;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class HandshakeCommandStrategy extends CreateLabSerialDeviceHandshakeCommandStrategy
   {
   /** The pattern of characters to look for in the sensor board's startup mode "song" */
   private static final byte[] STARTUP_MODE_SONG_CHARACTERS = {'S', 'B'};

   /** The pattern of characters to send to put the sensor board into receive mode. */
   private static final byte[] RECEIVE_MODE_CHARACTERS = {'E', 'V'};

   protected byte[] getReceiveModeCharacters()
      {
      return RECEIVE_MODE_CHARACTERS.clone();
      }

   protected byte[] getStartupModeCharacters()
      {
      return STARTUP_MODE_SONG_CHARACTERS.clone();
      }
   }
