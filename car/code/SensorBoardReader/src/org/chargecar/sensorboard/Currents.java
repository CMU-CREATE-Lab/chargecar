package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface Currents
   {
   double getBatteryCurrent();

   double getCapacitorCurrent();

   double getAccessoryCurrent();

   double getMotorCurrent(int motorId);

   double getAuxiliaryCurrent(int auxiliaryDeviceId);
   }