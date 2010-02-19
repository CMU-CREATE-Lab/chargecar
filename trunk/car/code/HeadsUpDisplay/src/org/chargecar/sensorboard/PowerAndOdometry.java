package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface PowerAndOdometry
   {
   Power getPower();

   Odometry getOdometry();
   }