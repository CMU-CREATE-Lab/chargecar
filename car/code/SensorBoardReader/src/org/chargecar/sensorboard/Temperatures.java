package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface Temperatures
   {
   double getMotorTemperature(int motorId);

   double getMotorControllerTemperature(int motorControllerId);

   double getCapacitorTemperature();

   double getBatteryTemperature();

   double getOutsideTemperature();

   double getAuxiliaryTemperature(int auxiliaryDeviceId);
   }