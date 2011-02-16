package org.chargecar.lcddisplay;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class LCDConstants
   {
   /** display input values */
   public static final int CAR_RUNNING_MASK = 1;
   public static final int CAR_CHARGING_MASK = 2;
   public static final int UP_BUTTON_MASK = 4;
   public static final int DOWN_BUTTON_MASK = 8;
   public static final int LEFT_BUTTON_MASK = 16;
   public static final int RIGHT_BUTTON_MASK = 32;
   public static final int SELECT_BUTTON_MASK = 64;
   public static final int CANCEL_BUTTON_MASK = 128;
   public static final int ACCESSORY_BUTTON_ONE_MASK = 32;
   public static final int ACCESSORY_BUTTON_TWO_MASK = 64;
   public static final int ACCESSORY_BUTTON_THREE_MASK = 128;

   public static final int INPUT_BUTTON_INDEX = 0;
   public static final int CAR_STATUS_INDEX = 1;

   /** display output values */
   public static final int AIR_CONDITIONING = 0;
   public static final int POWER_STEERING = 1;
   public static final int CABIN_HEAT = 2;
   public static final int DISPLAY_BACK_LIGHT = 3;
   public static final int BRAKE_LIGHT = 4;
   public static final int BATTERY_COOLING = 5;
   public static final int BATTERY_HEATING = 6;
   public static final int ACCESSORY_ONE_LED = 13;
   public static final int ACCESSORY_TWO_LED = 14;
   public static final int ACCESSORY_THREE_LED = 15;

   /** clear/set states */
   public static final char SET_STATE = 'T';
   public static final char CLEAR_STATE = 'F';

   /** temperature sensors */
   public static final int CONTROLLER_TEMPERATURE = 0;
   public static final int MOTOR_TEMPERATURE = 1;

   /** states */
   public static final int TRUE = 84;
   public static final int FALSE = 70;

   /** temperature constants */
   public static final double KELVIN_FREEZING_POINT = 273.15;
   public static final int CELSIUS_FREEZING_POINT = 32;
   public static final double CELSIUS_TO_FAHRENHEIT_CONSTANT = 1.8;


   /** display row/column constants*/
   public static final int NUM_ROWS = 4;
   public static final int NUM_COLS = 20;
   public static final String BLANK_LINE = "                    "; //20 spaces

   private LCDConstants()
      {
      // private to prevent instantiation
      }
   }
