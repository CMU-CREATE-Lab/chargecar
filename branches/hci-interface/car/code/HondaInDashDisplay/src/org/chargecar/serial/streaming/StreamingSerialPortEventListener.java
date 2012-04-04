package org.chargecar.serial.streaming;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface StreamingSerialPortEventListener<E> extends StreamingSerialPortDeviceConnectionStateListener,
                                                             StreamingSerialPortDeviceReadingStateListener
   {
   void handleDataEvent(final E event);
   }