package org.chargecar.serial.streaming;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface StreamingSerialPortDeviceConnectionStatePublisher
   {
   void addStreamingSerialPortDeviceConnectionStateListener(final StreamingSerialPortDeviceConnectionStateListener listener);

   void removeStreamingSerialPortDeviceConnectionStateListener(final StreamingSerialPortDeviceConnectionStateListener listener);
   }