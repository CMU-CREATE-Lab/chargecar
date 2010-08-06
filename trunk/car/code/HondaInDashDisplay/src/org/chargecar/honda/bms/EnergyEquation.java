package org.chargecar.honda.bms;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface EnergyEquation
   {
   double getKilowattHours();

   /** Returns the change in kilowatt hours since the last update. */
   double getKilowattHoursDelta();

   double getKilowattHoursUsed();

   double getKilowattHoursRegen();
   }