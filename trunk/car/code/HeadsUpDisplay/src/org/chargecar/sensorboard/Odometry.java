package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface Odometry
   {
   double getOdometer();

   /** Returns the change (in miles) in the odometer since the last update. */
   double getOdometerDelta();

   double getTripOdometer1();

   double getTripOdometer2();
   }