package org.chargecar.honda.bms;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface BMSAndEnergy
   {
   BMSEvent getBmsState();

   EnergyEquation getEnergyEquation();
   }