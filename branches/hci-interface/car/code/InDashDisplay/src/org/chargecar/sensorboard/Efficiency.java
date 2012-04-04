package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface Efficiency
   {
   double getBatteryEfficiency();

   String toLoggingString();
   }