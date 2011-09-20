package org.chargecar.lcddisplay;

import edu.cmu.ri.createlab.display.character.menu.CharacterDisplayMenu;
import edu.cmu.ri.createlab.menu.DefaultMenuStatusManager;
import edu.cmu.ri.createlab.menu.Menu;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import edu.cmu.ri.createlab.serial.SerialPortEnumerator;
import org.apache.log4j.Logger;
import org.chargecar.lcddisplay.helpers.PostgresqlConnect;

import java.util.SortedSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class ChargeCarLCD {
    private static final Logger LOG = Logger.getLogger(ChargeCarLCD.class);
    private static boolean accessoryOneOn = false;
    private static boolean accessoryTwoOn = false;
    private static boolean accessoryThreeOn = false;
    private static SortedSet<String> availableSerialPorts = null;

    public static void main(final String[] args) {
        new ChargeCarLCD();
    }

    private ChargeCarLCD() {
        // create the LCD panel and button panel
        final LCDPanel lcdPanel = new LCDPanel(4, 20);

        // create the menu status manager
        final MenuStatusManager menuStatusManager = new DefaultMenuStatusManager();
        availableSerialPorts = SerialPortEnumerator.getAvailableSerialPorts();

        LOG.debug("ChargeCarLCD(): about to connect to the BMS and GPS...");
        // call .getInstance() on the various managers to kick off connection establishment to them
        HallEffectManager.getInstance();
        BMSManager.getInstance();
        GPSManager.getInstance();

        LOG.debug("ChargeCarLCD(): about to call LCDProxy.getInstance()...");
        final LCD lcd = LCDProxy.getInstance();

        //always turn on the display back light when the program starts
        lcd.turnOnDisplayBackLight();

        //load in properties file
        LOG.debug("ChargeCarLCD(): about to read in properties file...");
        lcd.setCurrentPropertiesFileName(LCDConstants.APP_PROPERTIES_FILE);
        final int numProperties = LCDConstants.DEFAULT_PROPERTIES.size();
        if (!lcd.openSavedProperties(LCDConstants.APP_PROPERTIES_FILE) || (lcd.getNumberOfSavedProperties() < numProperties)) {
            LOG.debug("ChargeCarLCD(): Failed to load app properties file. Opening first backup...");
            lcd.setCurrentPropertiesFileName(LCDConstants.APP_PROPERTIES_FILE_BACKUP1);
            if (!lcd.openSavedProperties(LCDConstants.APP_PROPERTIES_FILE_BACKUP1) || (lcd.getNumberOfSavedProperties() < numProperties)) {
                LOG.debug("ChargeCarLCD(): Failed to load first backup of app properties file. Opening second backup...");
                lcd.setCurrentPropertiesFileName(LCDConstants.APP_PROPERTIES_FILE_BACKUP2);
                if (!lcd.openSavedProperties(LCDConstants.APP_PROPERTIES_FILE_BACKUP2) || (lcd.getNumberOfSavedProperties() < numProperties)) {
                    LOG.debug("ChargeCarLCD(): Failed to load second backup of app properties file. Opening default properties file...");
                    lcd.setCurrentPropertiesFileName(LCDConstants.DEFAULT_PROPERTIES_FILE);
                    if (!lcd.openSavedProperties(LCDConstants.DEFAULT_PROPERTIES_FILE) || (lcd.getNumberOfSavedProperties() < numProperties)) {
                        LOG.debug("ChargeCarLCD(): Failed to load default properties file. Creating new default properties file...");
                        for (int i = 0; i < numProperties; i++) {
                            final String[] property = LCDConstants.DEFAULT_PROPERTIES.get(i);
                            final String key = property[0];
                            final String value = property[1];
                            lcd.setSavedProperty(key, value);
                        }
                    }
                }
            }
        }
        //now matter what file was opened, start writing to the app properties file
        lcd.setCurrentPropertiesFileName(LCDConstants.APP_PROPERTIES_FILE);
        //lcd.openSavedProperties(LCDConstants.APP_PROPERTIES_FILE);

        //add button panel listener
        lcd.addButtonPanelEventListener(new MyButtonPanelEventListener(menuStatusManager));

        // build the menu
        LOG.debug("ChargeCarLCD(): about to build the menu...");
        final Menu menu = CharacterDisplayMenu.create("/org/chargecar/lcddisplay/menu/menu.xml", menuStatusManager, lcdPanel);

        // set the default text on the LCD
        lcdPanel.setText(menu.getWelcomeText());

        // If there's welcome text, then delay the start of the menu manager so that the initial welcome screen is visible
        // for a bit
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(
                new Runnable() {
                    public void run() {
                        menuStatusManager.setActiveMenuItem(menu.getDefaultMenuItem());
                    }
                },
                menu.hasWelcomeText() ? 2 : 0,
                TimeUnit.SECONDS);

        //connect to the postgreSQL database
        PostgresqlConnect.getInstance();
    }

    public static SortedSet<String> getAvailableSerialPorts() {
        return availableSerialPorts;
    }

    public static void removeAvailableSerialPort(final String portName) {
        availableSerialPorts.remove(portName);
    }

    private static class MyButtonPanelEventListener implements ButtonPanelEventListener {
        private final MenuStatusManager menuStatusManager;

        private MyButtonPanelEventListener(final MenuStatusManager menuStatusManager) {
            this.menuStatusManager = menuStatusManager;
        }

        public void handleOKEvent() {
            menuStatusManager.handleStartEvent();
        }

        public void handleCancelEvent() {
            menuStatusManager.handleStopEvent();
        }

        public void handleUpEvent() {
            menuStatusManager.handleUpEvent();
        }

        public void handleRightEvent() {
            menuStatusManager.handleRightEvent();
        }

        public void handleDownEvent() {
            menuStatusManager.handleDownEvent();
        }

        public void handleLeftEvent() {
            menuStatusManager.handleLeftEvent();
        }

        @Override
        public void handleAccessoryOneEvent() {
            final LCD lcd = LCDProxy.getInstance();
            lcd.writeMarkerToFile();
            lcd.turnOnAccessoryOneLED();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            lcd.turnOffAccessoryOneLED();
            /*if (accessoryOneOn) {
                accessoryOneOn = false;
                lcd.turnOffAccessoryOneLED();
                if (lcd.getAccessoryButtonOne().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(0))) {
                    lcd.turnOffAirConditioning();
                } else if (lcd.getAccessoryButtonOne().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(1))) {
                    lcd.turnOffCabinHeat();
                } else if (lcd.getAccessoryButtonOne().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(2))) {
                    lcd.turnOffPowerSteering();
                }
                //lcd.turnOffAirConditioning();
            } else {
                accessoryOneOn = true;
                lcd.turnOnAccessoryOneLED();
                if (lcd.getAccessoryButtonOne().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(0))) {
                    lcd.turnOnAirConditioning();
                } else if (lcd.getAccessoryButtonOne().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(1))) {
                    lcd.turnOnCabinHeat();
                } else if (lcd.getAccessoryButtonOne().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(2))) {
                    lcd.turnOnPowerSteering();
                }
                //lcd.turnOnAirConditioning();
            }
            //menuStatusManager.handleAccessoryOneEvent();*/
        }

        @Override
        public void handleAccessoryTwoEvent() {
            final LCD lcd = LCDProxy.getInstance();
            if (accessoryTwoOn) {
                accessoryTwoOn = false;
                lcd.turnOffAccessoryTwoLED();
                if (lcd.getAccessoryButtonTwo().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(0))) {
                    lcd.turnOffAirConditioning();
                } else if (lcd.getAccessoryButtonTwo().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(1))) {
                    lcd.turnOffCabinHeat();
                } else if (lcd.getAccessoryButtonTwo().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(2))) {
                    lcd.turnOffPowerSteering();
                }
                //lcd.turnOffCabinHeat();
            } else {
                accessoryTwoOn = true;
                lcd.turnOnAccessoryTwoLED();
                if (lcd.getAccessoryButtonTwo().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(0))) {
                    lcd.turnOnAirConditioning();
                } else if (lcd.getAccessoryButtonTwo().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(1))) {
                    lcd.turnOnCabinHeat();
                } else if (lcd.getAccessoryButtonTwo().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(2))) {
                    lcd.turnOnPowerSteering();
                }
                //lcd.turnOnCabinHeat();
            }
            //menuStatusManager.handleAccessoryTwoEvent();
        }

        @Override
        public void handleAccessoryThreeEvent() {
            final LCD lcd = LCDProxy.getInstance();
            if (accessoryThreeOn) {
                accessoryThreeOn = false;
                lcd.turnOffAccessoryThreeLED();
                if (lcd.getAccessoryButtonThree().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(0))) {
                    lcd.turnOffAirConditioning();
                } else if (lcd.getAccessoryButtonThree().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(1))) {
                    lcd.turnOffCabinHeat();
                } else if (lcd.getAccessoryButtonThree().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(2))) {
                    lcd.turnOffPowerSteering();
                }
                //lcd.turnOffPowerSteering();
            } else {
                accessoryThreeOn = true;
                lcd.turnOnAccessoryThreeLED();
                if (lcd.getAccessoryButtonThree().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(0))) {
                    lcd.turnOnAirConditioning();
                } else if (lcd.getAccessoryButtonThree().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(1))) {
                    lcd.turnOnCabinHeat();
                } else if (lcd.getAccessoryButtonThree().equalsIgnoreCase(LCDConstants.ACCESSORY_BUTTON_STATES.get(2))) {
                    lcd.turnOnPowerSteering();
                }
                //lcd.turnOnPowerSteering();
            }
            //menuStatusManager.handleAccessoryThreeEvent();
        }
    }
}
