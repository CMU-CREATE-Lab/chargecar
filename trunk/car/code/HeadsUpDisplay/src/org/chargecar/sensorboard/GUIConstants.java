package org.chargecar.sensorboard;

import java.awt.Color;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class GUIConstants
   {
   public static final Color DEFAULT_METER_COLOR = new Color(200, 200, 220);
   public static final Color METER_WARNING_COLOR = new Color(255, 150, 150);

   public static final Color AUXILIARY_DEVICE_METER_COLOR = DEFAULT_METER_COLOR;
   public static final Color ACCESSSORY_METER_COLOR = DEFAULT_METER_COLOR;
   public static final Color BATTERY_METER_COLOR = new Color(200, 200, 220);
   public static final Color CAPACITOR_METER_COLOR = new Color(150, 220, 150);
   public static final Color MOTOR_METER_COLOR = new Color(230, 230, 150);
   public static final Color MOTOR_CONTROLLER_METER_COLOR = new Color(180, 180, 180);

   private GUIConstants()
      {
      // private to prevent instantiation
      }
   }
