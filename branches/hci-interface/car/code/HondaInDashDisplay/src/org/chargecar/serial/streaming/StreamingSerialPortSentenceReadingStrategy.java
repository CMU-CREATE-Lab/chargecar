package org.chargecar.serial.streaming;

import java.io.IOException;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface StreamingSerialPortSentenceReadingStrategy
   {
   boolean isDataAvailable() throws IOException;

   /**
    * Returns a <code>byte</code> array containing the next sentence, not including the sentence delimiter(s).  Throws
    * an IOException if the end of stream has been reached or if an I/O error occurs.
    */
   byte[] getNextSentence() throws IOException;
   }