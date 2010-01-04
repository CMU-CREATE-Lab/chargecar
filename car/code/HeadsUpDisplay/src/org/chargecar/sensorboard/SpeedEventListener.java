package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface SpeedEventListener
   {
   void handleEvent(final SpeedAndOdometry speedAndOdometry);
   }