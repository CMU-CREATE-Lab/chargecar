package org.chargecar.serial.streaming;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface StreamingSerialPortEventListener<E>
   {
   void handleEvent(final E sensorBoardEvent);
   }