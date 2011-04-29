package org.chargecar.lcddisplay;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class LCDConstants {
    /**
     * display input values
     */
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

    /**
     * display output values
     */
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

    /**
     * clear/set states
     */
    public static final char SET_STATE = 'T';
    public static final char CLEAR_STATE = 'F';

    /**
     * temperature sensors
     */
    public static final int CONTROLLER_TEMPERATURE = 0;
    public static final int MOTOR_TEMPERATURE = 1;

    /**
     * states
     */
    public static final int TRUE = 84;
    public static final int FALSE = 70;

    /**
     * temperature constants
     */
    public static final double KELVIN_FREEZING_POINT = 273.15;
    public static final int CELSIUS_FREEZING_POINT = 32;
    public static final double CELSIUS_TO_FAHRENHEIT_CONSTANT = 1.8;


    /**
     * display row/column constants
     */
    public static final int NUM_ROWS = 4;
    public static final int NUM_COLS = 20;
    public static final String BLANK_LINE = "                    "; //20 spaces

    /**
     * saved properties
     */
    public static final String DEFAULT_PROPERTIES_FILE = "default.properties";
    public static final String APP_PROPERTIES_FILE = "app.properties";
    public static final String APP_PROPERTIES_FILE_BACKUP1 = "app_bk1.properties";
    public static final String APP_PROPERTIES_FILE_BACKUP2 = "app_bk2.properties";
    public static final List<String[]> DEFAULT_PROPERTIES = new ArrayList<String[]>();
    static {
        String[] property = {"batteryHeaterTurnOnValue", "10"};
        DEFAULT_PROPERTIES.add(property);
        property = new String[]{"lifetimeChargingTime", "0.0"};
        DEFAULT_PROPERTIES.add(property);
        property = new String[]{"lifetimeDrivingTime", "0.0"};
        DEFAULT_PROPERTIES.add(property);
        property = new String[]{"lifetimeEfficiency", "0.0"};
        DEFAULT_PROPERTIES.add(property);
        property = new String[]{"lifetimeDistanceTraveled", "0.0"};
        DEFAULT_PROPERTIES.add(property);
        property = new String[]{"lifetimeEnergyRegen", "0.0"};
        DEFAULT_PROPERTIES.add(property);
        property = new String[]{"lifetimeEnergyDischarge", "0.0"};
        DEFAULT_PROPERTIES.add(property);
        property = new String[]{"lifetimeEnergyConsumed", "0.0"};
        DEFAULT_PROPERTIES.add(property);
        property = new String[]{"costOfElectricity", ".11"};
        DEFAULT_PROPERTIES.add(property);
        property = new String[]{"costOfGas", "3.638"};
        DEFAULT_PROPERTIES.add(property);
        property = new String[]{"carMpg", "31"};
        DEFAULT_PROPERTIES.add(property);
        property = new String[]{"accessoryButtonOne", "A/C"};
        DEFAULT_PROPERTIES.add(property);
        property = new String[]{"accessoryButtonTwo", "Heat"};
        DEFAULT_PROPERTIES.add(property);
        property = new String[]{"accessoryButtonThree", "P/S"};
        DEFAULT_PROPERTIES.add(property);
    }
    public static final int NUM_PROPERTIES = 14;
    
    public static final double SECONDS_TO_HOURS = 0.000277777778;
    public static final double METERS_TO_MILES = 0.000621371192;
    /**
     * menu choices
     */
    public static final String SELECTED_OPTION = String.valueOf((char) 255);
    public static final String UNSELECTED_OPTION = String.valueOf((char) 219);


    public static final List<String> ACCESSORY_BUTTON_STATES = new ArrayList<String>();
    static {
        String state = "A/C";
        ACCESSORY_BUTTON_STATES.add(state);
        state = "Heat";
        ACCESSORY_BUTTON_STATES.add(state);
        state = "P/S";
        ACCESSORY_BUTTON_STATES.add(state);
    }

    //public static final String LOG_PATH = "C:\\Documents and Settings\\Paul\\My Documents\\CMU\\CREATE\\projects\\chargecar\\cars\\display\\LCD6\\chargecar\\car\\logs";
    public static final String LOG_PATH = "/home/chargecar/ChargeCar/trunk/car/logs";
    //public static final String OUTPUT_LOG_PATH = "E:\\ChargeCar_logs";
    public static final String USB_UNMOUNT_PATH = "/dev/sdb";
    public static final String USB_UNMOUNT_PATH2 = "/dev/sdc";
    public static final String USB_ROOT_PATH = "/media";

    public static final String LOCAL_LCD_SOFTWARE_PATH = "/home/chargecar/ChargeCar/trunk/car/";
    public static final String USB_LCD_SOFTWARE_PATH = "/chargecar/car";

    /**
     * Prefix for system property used to store the name of the serial port for a particular subsystem.
     */
    public static final String SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX = LCDConstants.class.getName() + ".serial_port.";

    private LCDConstants() {
        // private to prevent instantiation
    }
}
