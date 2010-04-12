package org.chargecar.sensorboard;

/**
 * <p>
 * <code>SensorBoardConstants</code> defines various constact for the sensor board.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SensorBoardConstants
   {
   public static final double HOURS_PER_MILLISECOND = 1 / 3600000.0;

   public static final int MOTOR_DEVICE_COUNT = 4;

   public static final int MOTOR_CONTROLLER_DEVICE_COUNT = 2;

   public static final int BATTERY_DEVICE_COUNT = 4;

   private SensorBoardConstants()
      {
      // private to prevent instantiation
      }
   }
