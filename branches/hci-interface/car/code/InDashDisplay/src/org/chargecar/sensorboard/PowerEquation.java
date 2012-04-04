package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface PowerEquation
   {
   double getKilowattHours();

   /** Returns the change in kilowatt hours since the last update. */
   double getKilowattHoursDelta();

   double getKilowattHoursUsed();

   double getKilowattHoursRegen();
   }