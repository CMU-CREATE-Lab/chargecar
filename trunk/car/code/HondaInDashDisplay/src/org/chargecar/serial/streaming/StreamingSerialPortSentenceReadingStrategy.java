package org.chargecar.serial.streaming;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface StreamingSerialPortSentenceReadingStrategy
   {
   /**
    * Returns a <code>byte</code> array containing the next sentence, not including the sentence delimiter(s).
    */
   byte[] getNextSentence();
   }