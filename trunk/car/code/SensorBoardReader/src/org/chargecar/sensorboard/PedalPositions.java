package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface PedalPositions extends SensorBoardData
   {
   double getThrottlePosition();

   double getRegenBrakePosition();

   String toLoggingString();
   }