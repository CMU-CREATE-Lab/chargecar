package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
interface EventListener<T>
   {
   void handleEvent(final T eventData);
   }