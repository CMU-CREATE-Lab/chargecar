package org.chargecar.serial.streaming;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface StreamingSerialPortEventPublisher<E>
   {
   void publishEvent(final E event);
   }