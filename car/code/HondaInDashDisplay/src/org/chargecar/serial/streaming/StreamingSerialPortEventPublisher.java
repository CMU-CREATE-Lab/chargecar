package org.chargecar.serial.streaming;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface StreamingSerialPortEventPublisher<E>
   {
   void addStreamingSerialPortEventListener(final StreamingSerialPortEventListener<E> listener);

   void removeStreamingSerialPortEventListener(final StreamingSerialPortEventListener<E> listener);
   }
