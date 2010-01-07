package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface Voltages extends SensorBoardData
   {
   double getBatteryVoltage(int batteryId);

   double getBatteryVoltage();

   double getCapacitorVoltage();

   double getAccessoryVoltage();

   double getAuxiliaryVoltage(final int auxiliaryDeviceId);
   }