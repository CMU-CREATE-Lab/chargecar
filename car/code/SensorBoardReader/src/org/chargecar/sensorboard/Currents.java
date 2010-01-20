package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface Currents extends SensorBoardData
   {
   Double getBatteryCurrent();

   Double getCapacitorCurrent();

   Double getAccessoryCurrent();

   Double getMotorCurrent(int motorId);

   Double getAuxiliaryCurrent(int auxiliaryDeviceId);
   }