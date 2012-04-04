package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface SpeedAndOdometry extends Speed, Odometry
   {
   String toLoggingString();
   }