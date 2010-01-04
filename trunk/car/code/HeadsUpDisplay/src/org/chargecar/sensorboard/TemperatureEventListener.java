package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface TemperatureEventListener
   {
   void handleEvent(final Temperatures temperatures);
   }
