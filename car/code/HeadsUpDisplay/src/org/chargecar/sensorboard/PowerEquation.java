package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface PowerEquation
   {
   double getKilowattHours();

   double getKilowattHoursUsed();

   double getKilowattHoursRegen();
   }