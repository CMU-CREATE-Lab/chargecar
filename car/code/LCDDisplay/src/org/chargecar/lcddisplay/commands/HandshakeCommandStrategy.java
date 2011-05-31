package org.chargecar.lcddisplay.commands;

import edu.cmu.ri.createlab.serial.CreateLabSerialDeviceHandshakeCommandStrategy;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class HandshakeCommandStrategy extends CreateLabSerialDeviceHandshakeCommandStrategy
   {
   /** The pattern of characters to look for in the CarLCD's startup mode "song" */
   private static final byte[] STARTUP_MODE_SONG_CHARACTERS = {'C', 'V'};

   /** The pattern of characters to send to put the CarLCD into receive mode. */
   private static final byte[] RECEIVE_MODE_CHARACTERS = {'I', 'C'};

   public HandshakeCommandStrategy()
      {
      super(5000, DEFAULT_SLURP_TIMEOUT_MILLIS, DEFAULT_MAX_NUMBER_OF_RETRIES);
      }

   @Override
   protected byte[] getReceiveModeCharacters()
      {
      return RECEIVE_MODE_CHARACTERS.clone();
      }

   @Override
   protected byte[] getStartupModeCharacters()
      {
      return STARTUP_MODE_SONG_CHARACTERS.clone();
      }
   }
