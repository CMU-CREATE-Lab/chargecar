package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class SensorBoardDataImpl implements SensorBoardData
   {
   private final long timestamp = System.currentTimeMillis();

   public final long getTimestampMilliseconds()
      {
      return timestamp;
      }
   }
