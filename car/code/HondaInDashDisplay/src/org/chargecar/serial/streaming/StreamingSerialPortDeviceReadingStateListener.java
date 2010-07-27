package org.chargecar.serial.streaming;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface StreamingSerialPortDeviceReadingStateListener
   {
   void handleReadingStateChange(final boolean isReading);
   }