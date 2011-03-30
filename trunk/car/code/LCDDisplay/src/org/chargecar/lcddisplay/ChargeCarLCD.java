package org.chargecar.lcddisplay;

import edu.cmu.ri.createlab.display.character.menu.CharacterDisplayMenu;
import edu.cmu.ri.createlab.menu.DefaultMenuStatusManager;
import edu.cmu.ri.createlab.menu.Menu;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;
import org.chargecar.lcddisplay.helpers.PostgresqlConnect;

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

    public static void main(final String[] args) {
        for (final String arg : args) {
            final String[] keyValue = arg.split("=");
            if (keyValue.length == 2) {
                LOG.debug("Associating [" + keyValue[0] + "] with serial port [" + keyValue[1] + "]");
                System.setProperty(LCDConstants.SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX + keyValue[0].toLowerCase(), keyValue[1]);
            } else {
                LOG.info("Ignoring unexpected switch [" + arg + "]");
            }
        }

        new ChargeCarLCD();
    }

    private ChargeCarLCD() {
        // create the LCD panel and button panel
        final LCDPanel lcdPanel = new LCDPanel(4, 20);

        // create the menu status manager
        final MenuStatusManager menuStatusManager = new DefaultMenuStatusManager();

        LOG.debug("ChargeCarLCD(): about to connect to the BMS and GPS...");
        // call .getInstance() on the various managers to kick off connection establishment to them
        BMSManager.getInstance();
        GPSManager.getInstance();

        LOG.debug("ChargeCarLCD(): about to call LCDProxy.getInstance()...");
        final LCD lcd = LCDProxy.getInstance();

        //always turn on the display back light when the program starts
        lcd.turnOnDisplayBackLight();

        //load in properties file
        LOG.debug("ChargeCarLCD(): about to read in properties file...");
        lcd.setCurrentPropertiesFileName(LCDConstants.APP_PROPERTIES_FILE);
        if (!lcd.openSavedProperties(LCDConstants.APP_PROPERTIES_FILE) || (lcd.getNumberOfSavedProperties() < LCDConstants.NUM_PROPERTIES)) {
            LOG.debug("ChargeCarLCD(): Failed to load app properties file. Opening first backup...");
            lcd.setCurrentPropertiesFileName(LCDConstants.APP_PROPERTIES_FILE_BACKUP1);
            if (!lcd.openSavedProperties(LCDConstants.APP_PROPERTIES_FILE_BACKUP1) || (lcd.getNumberOfSavedProperties() < LCDConstants.NUM_PROPERTIES)) {
                LOG.debug("ChargeCarLCD(): Failed to load first backup of app properties file. Opening second backup...");
                lcd.setCurrentPropertiesFileName(LCDConstants.APP_PROPERTIES_FILE_BACKUP1);
                if (!lcd.openSavedProperties(LCDConstants.APP_PROPERTIES_FILE_BACKUP1) || (lcd.getNumberOfSavedProperties() < LCDConstants.NUM_PROPERTIES)) {
                    LOG.debug("ChargeCarLCD(): Failed to load second backup of app properties file. Opening default properties file...");
                    lcd.setCurrentPropertiesFileName(LCDConstants.DEFAULT_PROPERTIES_FILE);
                    if (!lcd.openSavedProperties(LCDConstants.DEFAULT_PROPERTIES_FILE) || (lcd.getNumberOfSavedProperties() < LCDConstants.NUM_PROPERTIES)) {
                        LOG.debug("ChargeCarLCD(): Failed to load default properties file. Creating new default properties file...");
                        for (int i = 0; i < LCDConstants.NUM_PROPERTIES; i++) {
                            final String[] property = LCDConstants.DEFAULT_PROPERTIES.get(i);
                            final String key = property[0];
                            final String value = property[1];
                            lcd.setSavedProperty(key, value);
                            lcd.writeSavedProperties();
                        }
                    }
                }
            }
        }
        //now matter what file was opened, start writing to the app properties file
        lcd.setCurrentPropertiesFileName(LCDConstants.APP_PROPERTIES_FILE);
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

            if (accessoryOneOn) {
                accessoryOneOn = false;
                lcd.turnOffAccessoryOneLED();
                lcd.turnOffAirConditioning();
            } else {
                accessoryOneOn = true;
                lcd.turnOnAccessoryOneLED();
                lcd.turnOnAirConditioning();
            }
            //menuStatusManager.handleAccessoryOneEvent();
        }

        @Override
        public void handleAccessoryTwoEvent() {
            final LCD lcd = LCDProxy.getInstance();
            if (accessoryTwoOn) {
                accessoryTwoOn = false;
                lcd.turnOffAccessoryTwoLED();
                lcd.turnOffCabinHeat();
            } else {
                accessoryTwoOn = true;
                lcd.turnOnAccessoryTwoLED();
                lcd.turnOnCabinHeat();
            }
            //menuStatusManager.handleAccessoryTwoEvent();
        }

        @Override
        public void handleAccessoryThreeEvent() {
            final LCD lcd = LCDProxy.getInstance();
            if (accessoryThreeOn) {
                accessoryThreeOn = false;
                lcd.turnOffAccessoryThreeLED();
                lcd.turnOffPowerSteering();
            } else {
                accessoryThreeOn = true;
                lcd.turnOnAccessoryThreeLED();
                lcd.turnOnPowerSteering();
            }
            //menuStatusManager.handleAccessoryThreeEvent();
        }
    }
}
