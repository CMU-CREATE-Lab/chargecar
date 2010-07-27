package org.chargecar.serial.streaming;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface StreamingSerialPortDeviceReadingStatePublisher
   {
   void addStreamingSerialPortDeviceReadingStateListener(final StreamingSerialPortDeviceReadingStateListener listener);

   void removeStreamingSerialPortDeviceReadingStateListener(final StreamingSerialPortDeviceReadingStateListener listener);
   }