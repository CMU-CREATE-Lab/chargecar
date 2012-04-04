package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface Voltages extends SensorBoardData
   {
   Double getBatteryVoltage(final int batteryId);

   Double getBatteryVoltage();

   Double getCapacitorVoltage();

   Double getAccessoryVoltage();
   }