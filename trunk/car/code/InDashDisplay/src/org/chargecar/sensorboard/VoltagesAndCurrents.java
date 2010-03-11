package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface VoltagesAndCurrents
   {
   Voltages getVoltages();

   Currents getCurrents();

   boolean isCapacitorOverVoltage();
   }