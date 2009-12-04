package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface Voltages
   {
   double getBatteryVoltage(int batteryId);

   double getCapacitorVoltage();

   double getAccessoryVoltage();

   double getAuxiliaryVoltage(final int auxiliaryDeviceId);
   }