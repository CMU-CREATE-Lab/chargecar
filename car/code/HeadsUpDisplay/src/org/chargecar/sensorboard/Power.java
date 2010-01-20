package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface Power extends VoltagesAndCurrents
   {
   PowerEquation getBatteryPowerEquation();

   PowerEquation getCapacitorPowerEquation();

   PowerEquation getAccessoryPowerEquation();

   Voltages getMinimumVoltages();

   Voltages getMaximumVoltages();

   Currents getMinimumCurrents();

   Currents getMaximumCurrents();
   }