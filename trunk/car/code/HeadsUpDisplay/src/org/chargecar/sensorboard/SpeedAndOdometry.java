package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface SpeedAndOdometry extends Speed
   {
   double getOdometer();

   double getTripOdometer1();

   double getTripOdometer2();
   }