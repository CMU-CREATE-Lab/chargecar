package org.chargecar.lcddisplay;

import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.serial.CreateLabSerialDeviceNoReturnValueCommandStrategy;
import edu.cmu.ri.createlab.serial.SerialDeviceCommandExecutionQueue;
import edu.cmu.ri.createlab.serial.SerialDeviceNoReturnValueCommandExecutor;
import edu.cmu.ri.createlab.serial.SerialDeviceReturnValueCommandExecutor;
import edu.cmu.ri.createlab.serial.config.*;
import edu.cmu.ri.createlab.util.commandexecution.CommandExecutionFailureHandler;
import edu.cmu.ri.createlab.util.sequence.BoundedSequenceNumber;
import edu.cmu.ri.createlab.util.sequence.SequenceNumber;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.chargecar.gpx.DistanceCalculator;
import org.chargecar.gpx.GPSCoordinate;
import org.chargecar.gpx.SphericalLawOfCosinesDistanceCalculator;
import org.chargecar.honda.bms.BMSAndEnergy;
import org.chargecar.honda.gps.GPSEvent;
import org.chargecar.lcddisplay.commands.*;
import org.chargecar.lcddisplay.helpers.GPSHelper;
import org.chargecar.lcddisplay.lcd.LCDEvent;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class LCDProxy implements LCD {
    private static final Logger LOG = Logger.getLogger(LCDProxy.class);
    private static final Logger DATA_LOG = Logger.getLogger("DataLog");

    private final byte[] dataSynchronizationLock = new byte[0];

    private static final String APPLICATION_NAME = "LCDProxy";
    private static final int DELAY_IN_MILLISECONDS_BETWEEN_PEER_PINGS = 50;
    private static final int DELAY_IN_SECONDS_BETWEEN_BATTERY_HEATER_CHECK = 30;
    private static final int DELAY_IN_SECONDS_BETWEEN_LCD_EVENT_POLL = 1;
    private static final int DELAY_IN_MILISECONDS_BETWEEN_BRAKELIGHT_POLL = 100;
    private static final int DELAY_IN_SECONDS_BETWEEN_BACKUPONE_POLL = 15;
    private static final int DELAY_IN_SECONDS_BETWEEN_BACKUPTWO_POLL = 60;

    public static final SequenceNumber SEQUENCE_NUMBER = new BoundedSequenceNumber(0, 255);
    private boolean heaterOn = false;
    private boolean brakeLightOn = false;
    private Properties savedProperties = null;
    private String savedPropertiesFileName = LCDConstants.DEFAULT_PROPERTIES_FILE;
    private double tripDistance = 0.0;
    private double chargingTime = 0.0;
    private double drivingTime = 0.0;
    private final AtomicInteger markValue = new AtomicInteger(0);
    private GPSCoordinate previousTrackPoint = null;
    private final DistanceCalculator distanceCalculator = SphericalLawOfCosinesDistanceCalculator.getInstance();
    private Date previousDate = null;


    private static class LazyHolder {
        private static final LCDCreator INSTANCE = new LCDCreator();

        private LazyHolder() {
        }
    }

    public static LCD getInstance() {
        return LazyHolder.INSTANCE.getLCD();
    }

    /**
     * Tries to create a <code>LCDProxy</code> for the the serial port specified by the given
     * <code>serialPortName</code>. Returns <code>null</code> if the connection could not be established.
     *
     * @param serialPortName - the name of the serial port device which should be used to establish the connection
     * @throws IllegalArgumentException if the <code>serialPortName</code> is <code>null</code>
     */
    public static LCDProxy create(final String serialPortName) {
        // a little error checking...
        if (serialPortName == null) {
            throw new IllegalArgumentException("The serial port name may not be null");
        }

        // create the serial port configuration
        final SerialIOConfiguration config = new SerialIOConfiguration(serialPortName,
                BaudRate.BAUD_57600,
                CharacterSize.EIGHT,
                Parity.NONE,
                StopBits.ONE,
                FlowControl.NONE);

        try {
            // create the serial port command queue
            final SerialDeviceCommandExecutionQueue commandQueue = SerialDeviceCommandExecutionQueue.create(APPLICATION_NAME, config);

            // see whether its creation was successful
            if (commandQueue == null) {
                if (LOG.isEnabledFor(Level.ERROR)) {
                    LOG.error("Failed to open serial port '" + serialPortName + "'");
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Serial port '" + serialPortName + "' opened.");
                }

                // now try to do the handshake with the CarLCD to establish communication
                final boolean wasHandshakeSuccessful = commandQueue.executeAndReturnStatus(new HandshakeCommandStrategy());

                // see if the handshake was a success
                if (wasHandshakeSuccessful) {
                    LOG.info("CarLCD handshake successful!");

                    // now create and return the proxy
                    return new LCDProxy(commandQueue, serialPortName);
                } else {
                    LOG.error("Failed to handshake with CarLCD");
                }

                // the handshake failed, so shutdown the command queue to release the serial port
                commandQueue.shutdown();
            }
        } catch (Exception e) {
            LOG.error("Exception while trying to create the LCDProxy", e);
        }

        return null;
    }

    private final SerialDeviceCommandExecutionQueue commandQueue;
    private final String serialPortName;
    private final CreateLabSerialDeviceNoReturnValueCommandStrategy disconnectCommandStrategy = new DisconnectCommandStrategy();

    private final SerialDeviceNoReturnValueCommandExecutor noReturnValueCommandExecutor;
    private final SerialDeviceReturnValueCommandExecutor<Double> doubleReturnValueCommandExecutor;
    private final SerialDeviceReturnValueCommandExecutor<Integer> integerReturnValueCommandExecutor;
    private final SerialDeviceReturnValueCommandExecutor<int[]> intArrayReturnValueCommandExecutor;

    private final LCDPinger lcdPinger = new LCDPinger();
    private final ScheduledExecutorService peerPingScheduler = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("LCDProxy.peerPingScheduler"));
    private final ScheduledFuture<?> peerPingScheduledFuture;
    private final ScheduledFuture<?> batteryHeaterScheduledFuture;
    private final ScheduledFuture<?> lcdEventScheduledFuture;
    private final ScheduledFuture<?> brakeLightScheduledFuture;
    private final ScheduledFuture<?> propertiesBackupOneScheduledFuture;
    private final ScheduledFuture<?> propertiesBackupTwoScheduledFuture;
    private final Collection<CreateLabDevicePingFailureEventListener> createLabDevicePingFailureEventListeners = new HashSet<CreateLabDevicePingFailureEventListener>();
    private final Set<ButtonPanelEventListener> buttonPanelEventListeners = new HashSet<ButtonPanelEventListener>();

    private LCDProxy(final SerialDeviceCommandExecutionQueue commandQueue, final String serialPortName) {
        this.commandQueue = commandQueue;
        this.serialPortName = serialPortName;

        final CommandExecutionFailureHandler commandExecutionFailureHandler =
                new CommandExecutionFailureHandler() {
                    public void handleExecutionFailure() {
                        lcdPinger.forceFailure();
                    }
                };

        noReturnValueCommandExecutor = new SerialDeviceNoReturnValueCommandExecutor(commandQueue, commandExecutionFailureHandler);
        doubleReturnValueCommandExecutor = new SerialDeviceReturnValueCommandExecutor<Double>(commandQueue, commandExecutionFailureHandler);
        integerReturnValueCommandExecutor = new SerialDeviceReturnValueCommandExecutor<Integer>(commandQueue, commandExecutionFailureHandler);
        intArrayReturnValueCommandExecutor = new SerialDeviceReturnValueCommandExecutor<int[]>(commandQueue, commandExecutionFailureHandler);

        // schedule periodic peer pings
        peerPingScheduledFuture = peerPingScheduler.scheduleWithFixedDelay(new LCDButtonPoller(),
                DELAY_IN_MILLISECONDS_BETWEEN_PEER_PINGS, // delay before first ping
                DELAY_IN_MILLISECONDS_BETWEEN_PEER_PINGS, // delay between pings
                TimeUnit.MILLISECONDS);

        batteryHeaterScheduledFuture = peerPingScheduler.scheduleWithFixedDelay(new LCDBatteryHeaterPoller(),
                5, // delay before first ping
                DELAY_IN_SECONDS_BETWEEN_BATTERY_HEATER_CHECK, // delay between pings
                TimeUnit.SECONDS);

        lcdEventScheduledFuture = peerPingScheduler.scheduleWithFixedDelay(new LCDEventPoller(),
                5, // delay before first ping
                DELAY_IN_SECONDS_BETWEEN_LCD_EVENT_POLL, // delay between pings
                TimeUnit.SECONDS);

        brakeLightScheduledFuture = peerPingScheduler.scheduleWithFixedDelay(new LCDBrakeLightPoller(),
                1, // delay before first ping
                DELAY_IN_MILISECONDS_BETWEEN_BRAKELIGHT_POLL, // delay between pings
                TimeUnit.MILLISECONDS);

        propertiesBackupOneScheduledFuture = peerPingScheduler.scheduleWithFixedDelay(new LCDPropertiesBackupOnePoller(),
                5, // delay before first ping
                DELAY_IN_SECONDS_BETWEEN_BACKUPONE_POLL, // delay between pings
                TimeUnit.SECONDS);

        propertiesBackupTwoScheduledFuture = peerPingScheduler.scheduleWithFixedDelay(new LCDPropertiesBackupTwoPoller(),
                5, // delay before first ping
                DELAY_IN_SECONDS_BETWEEN_BACKUPTWO_POLL, // delay between pings
                TimeUnit.SECONDS);
    }

    public String getPortName() {
        return serialPortName;
    }

    public void addCreateLabDevicePingFailureEventListener(final CreateLabDevicePingFailureEventListener listener) {
        if (listener != null) {
            createLabDevicePingFailureEventListeners.add(listener);
        }
    }

    public void removeCreateLabDevicePingFailureEventListener(final CreateLabDevicePingFailureEventListener listener) {
        if (listener != null) {
            createLabDevicePingFailureEventListeners.remove(listener);
        }
    }

    @Override
    public void addButtonPanelEventListener(final ButtonPanelEventListener listener) {
        if (listener != null) {
            buttonPanelEventListeners.add(listener);
        }
    }

    @Override
    public void removeButtonPanelEventListener(final ButtonPanelEventListener listener) {
        if (listener != null) {
            buttonPanelEventListeners.remove(listener);
        }
    }
    //chargecar begin

    public boolean setText(final int row, final int column, final String displayString) {
        return noReturnValueCommandExecutor.execute(new DisplayCommandStrategy(row, column, displayString));
    }

    public boolean setText(final int row, final int column, final String displayString, final boolean doAscii) {
        return noReturnValueCommandExecutor.execute(new DisplayCommandStrategy(row, column, displayString, true));
    }

    public Double getControllerTemperatureInKelvin() {
        return doubleReturnValueCommandExecutor.execute(new GetTemperatureCommandStrategy(LCDConstants.CONTROLLER_TEMPERATURE));
    }

    public Double getMotorTemperatureInKelvin() {
        return doubleReturnValueCommandExecutor.execute(new GetTemperatureCommandStrategy(LCDConstants.MOTOR_TEMPERATURE));
    }

    public Double getTemperatureInCelsius(final double temperatureInKelvin) {
        return temperatureInKelvin - LCDConstants.KELVIN_FREEZING_POINT;
    }

    public Double getTemperatureInFahrenheit(final double temperatureInKelvin) {
        return (LCDConstants.CELSIUS_TO_FAHRENHEIT_CONSTANT * getTemperatureInCelsius(temperatureInKelvin)) + LCDConstants.CELSIUS_FREEZING_POINT;
    }

    private int[] getInputs() {
        return intArrayReturnValueCommandExecutor.execute(new InputCommandStrategy());
    }

    public boolean isCarRunning(final int[] inputs) {
        if (inputs != null) {
            final int buttonInputs = inputs[LCDConstants.INPUT_BUTTON_INDEX];
            return (buttonInputs & LCDConstants.CAR_RUNNING_MASK) == LCDConstants.CAR_RUNNING_MASK;     // apply mask to get the car running state
        }
        return false;
    }

    public boolean isCarCharging(final int[] inputs) {
        if (inputs != null) {
            final int buttonInputs = inputs[LCDConstants.INPUT_BUTTON_INDEX];
            return (buttonInputs & LCDConstants.CAR_CHARGING_MASK) == LCDConstants.CAR_CHARGING_MASK;     // apply mask to get the car charging state
        }
        return false;
    }

    public boolean wasUpButtonPressed(final int[] inputs) {
        if (inputs != null) {
            final int buttonInputs = inputs[LCDConstants.INPUT_BUTTON_INDEX];
            return (buttonInputs & LCDConstants.UP_BUTTON_MASK) == LCDConstants.UP_BUTTON_MASK;     // apply mask to get the up button state
        }
        return false;
    }

    public boolean wasDownButtonPressed(final int[] inputs) {
        if (inputs != null) {
            final int buttonInputs = inputs[LCDConstants.INPUT_BUTTON_INDEX];
            return (buttonInputs & LCDConstants.DOWN_BUTTON_MASK) == LCDConstants.DOWN_BUTTON_MASK;     // apply mask to get down button state
        }
        return false;
    }

    public boolean wasLeftButtonPressed(final int[] inputs) {
        if (inputs != null) {
            final int buttonInputs = inputs[LCDConstants.INPUT_BUTTON_INDEX];
            return (buttonInputs & LCDConstants.LEFT_BUTTON_MASK) == LCDConstants.LEFT_BUTTON_MASK;     // apply mask to get left button state
        }
        return false;
    }

    public boolean wasRightButtonPressed(final int[] inputs) {
        if (inputs != null) {
            final int buttonInputs = inputs[LCDConstants.INPUT_BUTTON_INDEX];
            return (buttonInputs & LCDConstants.RIGHT_BUTTON_MASK) == LCDConstants.RIGHT_BUTTON_MASK;     // apply mask to get right button state
        }
        return false;
    }

    public boolean wasSelectButtonPressed(final int[] inputs) {
        if (inputs != null) {
            final int buttonInputs = inputs[LCDConstants.INPUT_BUTTON_INDEX];
            return (buttonInputs & LCDConstants.SELECT_BUTTON_MASK) == LCDConstants.SELECT_BUTTON_MASK;     // apply mask to get select button state
        }
        return false;
    }

    public boolean wasCancelButtonPressed(final int[] inputs) {
        if (inputs != null) {
            final int buttonInputs = inputs[LCDConstants.INPUT_BUTTON_INDEX];
            return (buttonInputs & LCDConstants.CANCEL_BUTTON_MASK) == LCDConstants.CANCEL_BUTTON_MASK;     // apply mask to get cancel button state
        }
        return false;
    }

    public boolean wasAccessoryOneButtonPressed(final int[] inputs) {
        if (inputs != null) {
            final int buttonInputs = inputs[LCDConstants.CAR_STATUS_INDEX];
            return (buttonInputs & LCDConstants.ACCESSORY_BUTTON_ONE_MASK) == LCDConstants.ACCESSORY_BUTTON_ONE_MASK;     // apply mask to get accessory one button state
        }
        return false;
    }

    public boolean wasAccessoryTwoButtonPressed(final int[] inputs) {
        if (inputs != null) {
            final int buttonInputs = inputs[LCDConstants.CAR_STATUS_INDEX];
            return (buttonInputs & LCDConstants.ACCESSORY_BUTTON_TWO_MASK) == LCDConstants.ACCESSORY_BUTTON_TWO_MASK;     // apply mask to get accessory two button state
        }
        return false;
    }

    public boolean wasAccessoryThreeButtonPressed(final int[] inputs) {
        if (inputs != null) {
            final int buttonInputs = inputs[LCDConstants.CAR_STATUS_INDEX];
            return (buttonInputs & LCDConstants.ACCESSORY_BUTTON_THREE_MASK) == LCDConstants.ACCESSORY_BUTTON_THREE_MASK;     // apply mask to get accessory three button state
        }
        return false;
    }

    public boolean turnOnAirConditioning() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.AIR_CONDITIONING, LCDConstants.SET_STATE));
    }

    public boolean turnOnPowerSteering() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.POWER_STEERING, LCDConstants.SET_STATE));
    }

    public boolean turnOnCabinHeat() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.CABIN_HEAT, LCDConstants.SET_STATE));
    }

    public boolean turnOnDisplayBackLight() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.DISPLAY_BACK_LIGHT, LCDConstants.SET_STATE));
    }

    public boolean turnOnBrakeLight() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.BRAKE_LIGHT, LCDConstants.SET_STATE));
    }

    public boolean turnOnBatteryCooling() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.BATTERY_COOLING, LCDConstants.SET_STATE));
    }

    public boolean turnOnBatteryHeating() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.BATTERY_HEATING, LCDConstants.SET_STATE));
    }

    public boolean turnOnAccessoryOneLED() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.ACCESSORY_ONE_LED, LCDConstants.SET_STATE));
    }

    public boolean turnOnAccessoryTwoLED() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.ACCESSORY_TWO_LED, LCDConstants.SET_STATE));
    }

    public boolean turnOnAccessoryThreeLED() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.ACCESSORY_THREE_LED, LCDConstants.SET_STATE));
    }

    public boolean turnOffAirConditioning() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.AIR_CONDITIONING, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffPowerSteering() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.POWER_STEERING, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffCabinHeat() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.CABIN_HEAT, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffDisplayBackLight() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.DISPLAY_BACK_LIGHT, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffBrakeLight() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.BRAKE_LIGHT, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffBatteryCooling() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.BATTERY_COOLING, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffBatteryHeating() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.BATTERY_HEATING, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffAccessoryOneLED() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.ACCESSORY_ONE_LED, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffAccessoryTwoLED() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.ACCESSORY_TWO_LED, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffAccessoryThreeLED() {
        return noReturnValueCommandExecutor.execute(new OutputCommandStrategy(LCDConstants.ACCESSORY_THREE_LED, LCDConstants.CLEAR_STATE));
    }

    public Integer getRPM() {
        return integerReturnValueCommandExecutor.execute(new GetRPMCommandStrategy());
    }

    public Integer getMotorControllerErrorCodes() {
        return integerReturnValueCommandExecutor.execute(new GetErrorCodesCommandStrategy());
    }

    public boolean resetDisplay() {
        return noReturnValueCommandExecutor.execute(new ResetDisplayCommandStrategy());
    }

    public int getBatteryHeaterCutoffTemp() {
        return Integer.parseInt(getSavedProperty("batteryHeaterTurnOnValue"));
    }

    public void setBatteryHeaterCutoffTemp(final int newBatteryHeaterTurnOnValue) {
        synchronized (dataSynchronizationLock) {
            if (savedProperties == null) {
                return;
            }
            //tempertaure in celsius
            setSavedProperty("batteryHeaterTurnOnValue", Integer.toString(newBatteryHeaterTurnOnValue));
        }
    }

    public double getCostOfElectricity() {
        return Double.parseDouble(getSavedProperty("costOfElectricity"));
    }

    public void setCostOfElectricity(final double newCostOfElectricity) {
        synchronized (dataSynchronizationLock) {
            if (savedProperties == null) {
                return;
            }
            setSavedProperty("costOfElectricity", Double.toString(newCostOfElectricity));
        }
    }

    public double getCostOfGas() {
        return Double.parseDouble(getSavedProperty("costOfGas"));
    }

    public void setCostOfGas(final double newCostOfGas) {
        synchronized (dataSynchronizationLock) {
            if (savedProperties == null) {
                return;
            }
            setSavedProperty("costOfGas", Double.toString(newCostOfGas));
        }
    }

    public double getCarMpg() {
        return Double.parseDouble(getSavedProperty("carMpg"));
    }

    public void setCarMpg(final int newCarMpg) {
        synchronized (dataSynchronizationLock) {
            if (savedProperties == null) {
                return;
            }
            setSavedProperty("carMpg", Double.toString(newCarMpg));
        }
    }

    public Map<Object, Object> getPropertiesInstance() {
        synchronized (dataSynchronizationLock) {
            return Collections.unmodifiableMap(savedProperties);
        }
    }

    public String getSavedProperty(final String key) {
        synchronized (dataSynchronizationLock) {
            if (savedProperties == null) {
                return null;
            }
            return savedProperties.getProperty(key);
        }
    }

    public void setSavedProperty(final String key, final String value) {
        synchronized (dataSynchronizationLock) {
            if (savedProperties == null) {
                return;
            }
            savedProperties.setProperty(key, value);
        }
    }

    public boolean openSavedProperties(final String fileName) {
        synchronized (dataSynchronizationLock) {
            savedProperties = new Properties();
            try {
                savedProperties.load(new FileInputStream(fileName));
            } catch (IOException e) {
                LOG.error("Error reading properties file: " + e);
                return false;
            }
            return true;
        }
    }

    public void writeSavedProperties() {
        synchronized (dataSynchronizationLock) {
            if (savedProperties == null) {
                return;
            }
            try {
                //if file does not exist, a new one will be created
                savedProperties.store(new FileOutputStream(LCDConstants.APP_PROPERTIES_FILE), null);
            } catch (IOException e) {
                LOG.error("Error writing to properties file: " + e);
            }
        }
    }

    public void writeSavedPropertiesBackup1() {
        synchronized (dataSynchronizationLock) {
            if (savedProperties == null) {
                return;
            }
            try {
                //if file does not exist, a new one will be created
                savedProperties.store(new FileOutputStream(LCDConstants.APP_PROPERTIES_FILE_BACKUP1), null);
            } catch (IOException e) {
                LOG.error("Error writing to backup1 properties file: " + e);
            }
        }
    }

    public void writeSavedPropertiesBackup2() {
        synchronized (dataSynchronizationLock) {
            if (savedProperties == null) {
                return;
            }
            try {
                //if file does not exist, a new one will be created
                savedProperties.store(new FileOutputStream(LCDConstants.APP_PROPERTIES_FILE_BACKUP2), null);
            } catch (IOException e) {
                LOG.error("Error writing to backup2 properties file: " + e);
            }
        }
    }

    public int getNumberOfSavedProperties() {
        synchronized (dataSynchronizationLock) {
            return savedProperties.size();
        }
    }

    public String getCurrentPropertiesFileName() {
        synchronized (dataSynchronizationLock) {
            return savedPropertiesFileName;
        }
    }

    public void setCurrentPropertiesFileName(final String newPropertiesFileName) {
        synchronized (dataSynchronizationLock) {
            savedPropertiesFileName = newPropertiesFileName;
        }
    }

    public void setTripDistance(final double newTripDistance) {
        tripDistance = newTripDistance;
    }

    public double getTripDistance() {
        return tripDistance;
    }

    public double getChargingTime() {
        return chargingTime;
    }

    public double getDrivingTime() {
        return drivingTime;
    }

    public String getAccessoryButtonOne() {
        return getSavedProperty("accessoryButtonOne");
    }

    public void setAccessoryButtonOne(final String newAccessoryButtonOne) {
        synchronized (dataSynchronizationLock) {
            if (savedProperties == null) {
                return;
            }
            setSavedProperty("accessoryButtonOne", newAccessoryButtonOne);
        }
    }

    public String getAccessoryButtonTwo() {
        return getSavedProperty("accessoryButtonTwo");
    }

    public void setAccessoryButtonTwo(final String newAccessoryButtonTwo) {
        synchronized (dataSynchronizationLock) {
            if (savedProperties == null) {
                return;
            }
            setSavedProperty("accessoryButtonTwo", newAccessoryButtonTwo);
        }
    }

    public String getAccessoryButtonThree() {
        return getSavedProperty("accessoryButtonThree");
    }

    public void setAccessoryButtonThree(final String newAccessoryButtonThree) {
        synchronized (dataSynchronizationLock) {
            if (savedProperties == null) {
                return;
            }
            setSavedProperty("accessoryButtonThree", newAccessoryButtonThree);
        }
    }

    public void writeMarkerToFile() {
        LOG.info("============================================================= MARK " + markValue.getAndIncrement() + " (" + System.currentTimeMillis() + ") =============================================================");
    }
    //chargecar end

    public void disconnect() {
        disconnect(true);
    }

    private void disconnect(final boolean willAddDisconnectCommandToQueue) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("LCDProxy.disconnect(" + willAddDisconnectCommandToQueue + ")");
        }

        // turn off the peer pingers
        try {
            peerPingScheduledFuture.cancel(false);
            batteryHeaterScheduledFuture.cancel(false);
            lcdEventScheduledFuture.cancel(false);
            brakeLightScheduledFuture.cancel(false);
            //propertiesWriteScheduledFuture.cancel(false);
            propertiesBackupOneScheduledFuture.cancel(false);
            propertiesBackupTwoScheduledFuture.cancel(false);
            peerPingScheduler.shutdownNow();

            LOG.debug("LCDProxy.disconnect(): Successfully shut down CarLCD pingers.");
        } catch (Exception e) {
            LOG.error("LCDProxy.disconnect(): Exception caught while trying to shut down peer pingers", e);
        }

        // optionally send goodbye command to the CarLCD
        if (willAddDisconnectCommandToQueue) {
            LOG.debug("LCDProxy.disconnect(): Now attempting to send the disconnect command to the CarLCD");
            try {
                if (commandQueue.executeAndReturnStatus(disconnectCommandStrategy)) {
                    LOG.debug("LCDProxy.disconnect(): Successfully disconnected from the CarLCD.");
                } else {
                    LOG.error("LCDProxy.disconnect(): Failed to disconnect from the CarLCD.");
                }
            } catch (Exception e) {
                LOG.error("Exception caught while trying to execute the disconnect", e);
            }
        }

        // shut down the command queue, which closes the serial port
        try {
            LOG.debug("LCDProxy.disconnect(): shutting down the SerialDeviceCommandExecutionQueue...");
            commandQueue.shutdown();
            LOG.debug("LCDProxy.disconnect(): done shutting down the SerialDeviceCommandExecutionQueue");
        } catch (Exception e) {
            LOG.error("LCDProxy.disconnect(): Exception while trying to shut down the SerialDeviceCommandExecutionQueue", e);
        }
    }

    /* private class LCDPropertiesPoller implements Runnable {
        public void run() {
            writeSavedProperties();
        }
    }*/

    private class LCDPropertiesBackupOnePoller implements Runnable {
        public void run() {
            writeSavedPropertiesBackup1();
        }
    }

    private class LCDPropertiesBackupTwoPoller implements Runnable {
        public void run() {
            writeSavedPropertiesBackup2();
        }
    }

    private class LCDBrakeLightPoller implements Runnable {
        public void run() {
            final BMSManager bmsManager = BMSManager.getInstance();
            final BMSAndEnergy bmsData = (bmsManager == null) ? null : bmsManager.getData();
            final LCD lcd = LCDProxy.getInstance();

            if (bmsManager == null || bmsData == null) {
                LOG.error("LCDBrakeLightPoller.performAction(): bms is null");
                return;
            } else if (lcd == null) {
                LOG.error("LCDBrakeLightPoller.run(): lcd is null");
                return;
            }

            if (bmsData.getEnergyEquation().getKilowattHoursDelta() < 0) {
                lcd.turnOnBrakeLight();
                brakeLightOn = true;
            } else {
                if (brakeLightOn) {
                    lcd.turnOffBrakeLight();
                    brakeLightOn = false;
                }
            }
        }
    }

    private class LCDEventPoller implements Runnable {
        private double previousTripEnergyRegen = 0.0;
        private double previousTripEnergyDischarge = 0.0;
        private double previousTripEnergyConsumed = 0.0;
        private double previousTripAmpHours = 0.0;

        public void run() {
            final LCD lcd = LCDProxy.getInstance();
            final BMSManager bmsManager = BMSManager.getInstance();
            final BMSAndEnergy bmsData = (bmsManager == null) ? null : bmsManager.getData();
            final GPSManager gpsManager = GPSManager.getInstance();
            final GPSEvent gpsData = (gpsManager == null) ? null : gpsManager.getData();

            if (lcd == null) {
                LOG.error("LCDBatteryHeaterPoller.run(): lcd is null");
                return;
            }

            final int[] rawValues = getInputs();
            final boolean isRunning = lcd.isCarRunning(rawValues);
            final boolean isCharging = lcd.isCarCharging(rawValues);
            double lifetimeDistanceTraveled = Double.parseDouble(getSavedProperty("lifetimeDistanceTraveled"));
            double tripDistanceTraveled = Double.parseDouble(getSavedProperty("tripDistanceTraveled"));

            final double motorControllerTemperature = lcd.getTemperatureInCelsius(lcd.getControllerTemperatureInKelvin());
            final double motorTemperature = lcd.getTemperatureInCelsius(lcd.getMotorTemperatureInKelvin());
            final int motorControllerErrorCodes = lcd.getMotorControllerErrorCodes();
            final int rpm = lcd.getRPM();

            final LCDEvent lcdEvent = new LCDEvent(new Date(), isRunning, isCharging, motorControllerTemperature, motorTemperature, motorControllerErrorCodes, rpm);
            if (DATA_LOG.isInfoEnabled()) {
                DATA_LOG.info(lcdEvent.toLoggingString());
            }

            final Date currentDate = new Date();
            final double timeInSeconds;
            if (previousDate == null) {
                timeInSeconds = 1;
            } else {
                timeInSeconds = (currentDate.getTime() - previousDate.getTime()) / 1000.0; //time comes in in milliseconds
            }
            previousDate = currentDate;

            if (isCharging) {
                chargingTime += timeInSeconds;
                final double lifetimeChargingTime = Double.parseDouble(getSavedProperty("lifetimeChargingTime")) + timeInSeconds;
                setSavedProperty("lifetimeChargingTime", String.valueOf(lifetimeChargingTime));
            } else if (isRunning) {
                drivingTime += timeInSeconds;
                final double lifetimeDrivingTime = Double.parseDouble(getSavedProperty("lifetimeDrivingTime")) + timeInSeconds;
                setSavedProperty("lifetimeDrivingTime", String.valueOf(lifetimeDrivingTime));

                //don't keep track if we are basiccally idle
                final double powerFlowInKw = (bmsData.getBmsState().getPackTotalVoltage() * bmsData.getBmsState().getLoadCurrentAmps()) / 1000;
                if (Math.abs(powerFlowInKw) >= 0.56) {
                    if ((gpsManager != null && gpsData != null) && (gpsData.getLatitude() != null && gpsData.getLongitude() != null)) {
                        final List<Double> latLng = GPSHelper.degreeDecimalMinutesToDecimalDegrees(gpsData.getLatitude(), gpsData.getLongitude());
                        final GPSCoordinate currentTrackPoint = new GPSCoordinate(String.valueOf(latLng.get(1)), String.valueOf(latLng.get(0)));
                        if (previousTrackPoint == null) {
                            previousTrackPoint = currentTrackPoint;
                        }

                        final double distanceInMiles = distanceCalculator.compute2DDistance(previousTrackPoint, currentTrackPoint) * LCDConstants.METERS_TO_MILES;
                        previousTrackPoint = currentTrackPoint;

                        //account for gps location jumps; note that distance is calculated every second
                        //TODO: this logic can drastically be improved; we lose tunnels
                        if (distanceInMiles < .01 && !Double.isNaN(distanceInMiles)) {
                            tripDistance += distanceInMiles;
                            lifetimeDistanceTraveled += distanceInMiles;
                            tripDistanceTraveled += distanceInMiles;
                            setSavedProperty("lifetimeDistanceTraveled", String.valueOf(lifetimeDistanceTraveled));
                            setSavedProperty("tripDistanceTraveled", String.valueOf(tripDistanceTraveled));
                        }
                    }

                    double kwhDelta;

                    //regen
                    final double tripEnergyRegen = bmsData.getEnergyEquation().getKilowattHoursRegen();
                    setSavedProperty("tripEnergyRegen", String.valueOf(tripEnergyRegen));
                    try {
                        previousTripEnergyRegen = Double.parseDouble(getSavedProperty("tripEnergyRegen"));
                    } catch (NumberFormatException nfe) {
                        LOG.error("LCDProxy.LCDEventPoller(): " + nfe.getMessage());
                        previousTripEnergyRegen = tripEnergyRegen;
                    }
                    kwhDelta = tripEnergyRegen - previousTripEnergyRegen;
                    final double lifetimeEnergyRegen = Double.parseDouble(getSavedProperty("lifetimeEnergyRegen")) + kwhDelta;
                    setSavedProperty("lifetimeEnergyRegen", String.valueOf(lifetimeEnergyRegen));

                    //discharge
                    final double tripEnergyDischarge = bmsData.getEnergyEquation().getKilowattHoursUsed();
                    setSavedProperty("tripEnergyDischarge", String.valueOf(tripEnergyDischarge));
                    try {
                        previousTripEnergyDischarge = Double.parseDouble(getSavedProperty("tripEnergyDischarge"));
                    } catch (NumberFormatException nfe) {
                        LOG.error("LCDProxy.LCDEventPoller(): " + nfe.getMessage());
                        previousTripEnergyDischarge = tripEnergyDischarge;
                    }
                    kwhDelta = tripEnergyDischarge - previousTripEnergyDischarge;
                    final double lifetimeEnergyDischarge = Double.parseDouble(getSavedProperty("lifetimeEnergyDischarge")) + kwhDelta;
                    setSavedProperty("lifetimeEnergyDischarge", String.valueOf(lifetimeEnergyDischarge));

                    //total
                    final double tripEnergyConsumed = bmsData.getEnergyEquation().getKilowattHours();
                    setSavedProperty("tripEnergyConsumed", String.valueOf(tripEnergyConsumed));
                    try {
                        previousTripEnergyConsumed = Double.parseDouble(getSavedProperty("tripEnergyConsumed"));
                    } catch (NumberFormatException nfe) {
                        LOG.error("LCDProxy.LCDEventPoller(): " + nfe.getMessage());
                        previousTripEnergyConsumed = tripEnergyConsumed;
                    }
                    kwhDelta = tripEnergyConsumed - previousTripEnergyConsumed;
                    final double lifetimeEnergyConsumed = Double.parseDouble(getSavedProperty("lifetimeEnergyConsumed")) + kwhDelta;
                    setSavedProperty("lifetimeEnergyConsumed", String.valueOf(lifetimeEnergyConsumed));

                    //efficiencies
                    final double lifetimeEfficiency = lifetimeDistanceTraveled / lifetimeEnergyConsumed;
                    final double tripEfficiency = tripDistanceTraveled / tripEnergyConsumed;
                    setSavedProperty("lifetimeEfficiency", String.valueOf(lifetimeEfficiency));
                    setSavedProperty("tripEfficiency", String.valueOf(tripEfficiency));

                    //amp-hours
                    final double tripAmpHours = (tripEnergyConsumed * 1000) / bmsData.getBmsState().getPackTotalVoltage();
                    setSavedProperty("tripAmpHours", String.valueOf(tripAmpHours));
                    try {
                        previousTripAmpHours = Double.parseDouble(getSavedProperty("tripAmpHours"));
                    } catch (NumberFormatException nfe) {
                        LOG.error("LCDProxy.LCDEventPoller(): " + nfe.getMessage());
                        previousTripAmpHours = tripAmpHours;
                    }
                    final double ampHoursDelta = tripAmpHours - previousTripAmpHours;
                    final double lifetimeAmpHours = Double.parseDouble(getSavedProperty("lifetimeAmpHours")) + ampHoursDelta;
                    setSavedProperty("lifetimeAmpHours", String.valueOf(lifetimeAmpHours));

                    //final double kwhDelta = bmsData.getEnergyEquation().getKilowattHoursDelta();
                    //if (kwhDelta < 0) {
                    //final double lifetimeEnergyRegen = Double.parseDouble(getSavedProperty("lifetimeEnergyRegen")) + kwhDelta;
                    //final double tripEnergyRegen = Double.parseDouble(getSavedProperty("tripEnergyRegen")) + kwhDelta;
                    //setSavedProperty("lifetimeEnergyRegen", String.valueOf(lifetimeEnergyRegen));
                    //setSavedProperty("tripEnergyRegen", String.valueOf(tripEnergyRegen));
                    //} else if (kwhDelta > 0) {
                    //final double lifetimeEnergyDischarge = Double.parseDouble(getSavedProperty("lifetimeEnergyDischarge")) + kwhDelta;
                    //final double tripEnergyDischarge = Double.parseDouble(getSavedProperty("tripEnergyDischarge")) + kwhDelta;
                    //setSavedProperty("lifetimeEnergyDischarge", String.valueOf(lifetimeEnergyDischarge));
                    //setSavedProperty("tripEnergyDischarge", String.valueOf(tripEnergyDischarge));
                    //}

                    //final double lifetimeEnergyConsumed = Double.parseDouble(getSavedProperty("lifetimeEnergyConsumed")) + kwhDelta;
                    //final double tripEnergyConsumed = Double.parseDouble(getSavedProperty("tripEnergyConsumed")) + kwhDelta;
                    //setSavedProperty("lifetimeEnergyConsumed", String.valueOf(lifetimeEnergyConsumed));
                    //setSavedProperty("tripEnergyConsumed", String.valueOf(tripEnergyConsumed));

                    //final double lifetimeEfficiency = lifetimeDistanceTraveled / lifetimeEnergyConsumed;
                    //final double tripEfficiency = tripDistanceTraveled / tripEnergyConsumed;
                    //setSavedProperty("lifetimeEfficiency", String.valueOf(lifetimeEfficiency));
                    //setSavedProperty("tripEfficiency", String.valueOf(tripEfficiency));

                    //final double ampHours = (kwhDelta * 1000) / bmsData.getBmsState().getPackTotalVoltage();
                    //final double lifetimeAmpHours = Double.parseDouble(getSavedProperty("lifetimeAmpHours")) + ampHours;
                    //final double tripAmpHours = Double.parseDouble(getSavedProperty("tripAmpHours")) + ampHours;
                    ////final double lifetimeAmpHours = (lifetimeEnergyConsumed * 1000) / bmsData.getBmsState().getPackTotalVoltage();
                    ////final double tripAmpHours = (tripEnergyConsumed * 1000) / bmsData.getBmsState().getPackTotalVoltage();
                    //setSavedProperty("lifetimeAmpHours", String.valueOf(lifetimeAmpHours));
                    //setSavedProperty("tripAmpHours", String.valueOf(tripAmpHours));
                }
            }
            writeSavedProperties();
        }
    }

    private class LCDBatteryHeaterPoller implements Runnable {
        public void run() {
            final LCD lcd = LCDProxy.getInstance();
            final BMSManager manager = BMSManager.getInstance();
            final BMSAndEnergy data = (manager == null) ? null : manager.getData();

            if (manager == null || data == null) {
                LOG.error("LCDBatteryHeaterPoller.run(): bms is null");
                return;
            } else if (lcd == null) {
                LOG.error("LCDBatteryHeaterPoller.run(): lcd is null");
                return;
            }

            final double minBoardTempInCelsius = data.getBmsState().getMinimumCellBoardTemp();
            //final double minBoardTempInFahrenheit = (minBoardTempInCelsius * (1.8) + 32);
            final int[] rawValues = getInputs();
            if (lcd.isCarCharging(rawValues) && minBoardTempInCelsius < getBatteryHeaterCutoffTemp()) {
                LOG.info("getMinimumCellBoardTemp below cutoff " + "(" + getBatteryHeaterCutoffTemp() + ")" + ", turning battery heater on.");
                if (heaterOn) {
                    heaterOn = false;
                    lcd.turnOffBatteryHeating();
                    LOG.info("cycling the heater off");
                } else {
                    heaterOn = true;
                    lcd.turnOnBatteryHeating();
                    LOG.info("cycling the heater on");
                }
            } else {
                if (heaterOn) {
                    heaterOn = false;
                    LOG.info("getMinimumCellBoardTemp above cutoff " + "(" + getBatteryHeaterCutoffTemp() + ")" + ", turning battery heater off.");
                    lcd.turnOffBatteryHeating();
                }
            }
        }
    }

    private class LCDButtonPoller implements Runnable {
        private final ExecutorService executor = Executors.newCachedThreadPool();
        private int[] prevRawValues = null;

        private final Runnable handleOKEventRunnable = new Runnable() {
            public void run() {
                for (final ButtonPanelEventListener listener : buttonPanelEventListeners) {
                    listener.handleOKEvent();
                }
            }
        };
        private final Runnable handleCancelEventRunnable = new Runnable() {
            public void run() {
                for (final ButtonPanelEventListener listener : buttonPanelEventListeners) {
                    listener.handleCancelEvent();
                }
            }
        };
        private final Runnable handleUpEventRunnable = new Runnable() {
            public void run() {
                for (final ButtonPanelEventListener listener : buttonPanelEventListeners) {
                    listener.handleUpEvent();
                }
            }
        };
        private final Runnable handleDownEventRunnable = new Runnable() {
            public void run() {
                for (final ButtonPanelEventListener listener : buttonPanelEventListeners) {
                    listener.handleDownEvent();
                }
            }
        };
        private final Runnable handleLeftEventRunnable = new Runnable() {
            public void run() {
                for (final ButtonPanelEventListener listener : buttonPanelEventListeners) {
                    listener.handleLeftEvent();
                }
            }
        };
        private final Runnable handleRightEventRunnable = new Runnable() {
            public void run() {
                for (final ButtonPanelEventListener listener : buttonPanelEventListeners) {
                    listener.handleRightEvent();
                }
            }
        };
        private final Runnable handleAccessoryOneEventRunnable = new Runnable() {
            public void run() {
                for (final ButtonPanelEventListener listener : buttonPanelEventListeners) {
                    listener.handleAccessoryOneEvent();
                }
            }
        };
        private final Runnable handleAccessoryTwoEventRunnable = new Runnable() {
            public void run() {
                for (final ButtonPanelEventListener listener : buttonPanelEventListeners) {
                    listener.handleAccessoryTwoEvent();
                }
            }
        };
        private final Runnable handleAccessoryThreeEventRunnable = new Runnable() {
            public void run() {
                for (final ButtonPanelEventListener listener : buttonPanelEventListeners) {
                    listener.handleAccessoryThreeEvent();
                }
            }
        };

        public void run() {
            try {
                final int[] rawValues = getInputs();
                final boolean pingSuccessful = (rawValues != null);

                if (!pingSuccessful) {
                    handlePingFailure();
                    return;
                }

                final int numRawValues = rawValues.length;
                final int[] tmpRawValues = new int[numRawValues];
                boolean isSame = true;

                //check previous input call with current input call
                //if they are the same, just return
                //this handles the user holding down the button or
                //the button polling happening too quickly
                if (prevRawValues != null) {
                    for (int i = 0; i < numRawValues; i++) {
                        tmpRawValues[i] = rawValues[i] ^ prevRawValues[i];
                        if (tmpRawValues[i] != 0) {
                            isSame = false;
                            break;
                        }
                    }
                }

                prevRawValues = rawValues;
                if (isSame) {
                    return;
                }

                if (wasUpButtonPressed(rawValues)) {
                    LOG.trace("wasUpButtonPressed");
                    executeEventHandler(handleUpEventRunnable);
                } else if (wasDownButtonPressed(rawValues)) {
                    LOG.trace("wasDownButtonPressed");
                    executeEventHandler(handleDownEventRunnable);
                } else if (wasLeftButtonPressed(rawValues)) {
                    LOG.trace("wasLeftButtonPressed");
                    executeEventHandler(handleLeftEventRunnable);
                } else if (wasRightButtonPressed(rawValues)) {
                    LOG.trace("wasRightButtonPressed");
                    executeEventHandler(handleRightEventRunnable);
                } else if (wasSelectButtonPressed(rawValues)) {
                    LOG.trace("wasSelectButtonPressed");
                    executeEventHandler(handleOKEventRunnable);
                } else if (wasCancelButtonPressed(rawValues)) {
                    LOG.trace("wasCancelButtonPressed");
                    executeEventHandler(handleCancelEventRunnable);
                } else if (wasAccessoryOneButtonPressed(rawValues)) {
                    LOG.trace("wasAccessoryOneButtonPressed");
                    executeEventHandler(handleAccessoryOneEventRunnable);
                } else if (wasAccessoryTwoButtonPressed(rawValues)) {
                    LOG.trace("wasAccessoryTwoButtonPressed");
                    executeEventHandler(handleAccessoryTwoEventRunnable);
                } else if (wasAccessoryThreeButtonPressed(rawValues)) {
                    LOG.trace("wasAccessoryThreeButtonPressed");
                    executeEventHandler(handleAccessoryThreeEventRunnable);
                }
            } catch (Exception e) {
                LOG.error("LCDProxy$LCDButtonPoll.run(): Exception caught while executing the button poller", e);
            }
        }

        private void executeEventHandler(final Runnable runnable) {
            try {
                executor.execute(runnable);
            } catch (Exception e) {
                LOG.error("Exception caught while trying to execute button event handler", e);
            }
        }

        private void handlePingFailure() {
            try {
                LOG.debug("LCDProxy$LCDButtonPoller.handlePingFailure(): Peer ping failed.  Attempting to disconnect...");
                disconnect(false);
                LOG.debug("LCDProxy$LCDButtonPoller.handlePingFailure(): Done disconnecting from the CarLCD");
            } catch (Exception e) {
                LOG.error("LCDProxy$LCDButtonPoller.handlePingFailure(): Exeption caught while trying to disconnect from the CarLCD", e);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("LCDProxy$LCDButtonPoller.handlePingFailure(): Notifying " + createLabDevicePingFailureEventListeners.size() + " listeners of ping failure...");
            }
            for (final CreateLabDevicePingFailureEventListener listener : createLabDevicePingFailureEventListeners) {
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("LCDProxy$LCDButtonPoller.handlePingFailure(): Notifying " + listener);
                    }
                    listener.handlePingFailureEvent();
                } catch (Exception e) {
                    LOG.error("LCDProxy$LCDButtonPoller.handlePingFailure(): Exeption caught while notifying SerialDevicePingFailureEventListener", e);
                }
            }
        }

        private void forceFailure() {
            handlePingFailure();
        }
    }

    private class LCDPinger implements Runnable {
        public void run() {
            try {
                // for pings, we simply get the RPMMenuItemAction
                final boolean pingSuccessful = (getRPM() != null);

                // if the ping failed, then we know we have a problem so disconnect (which
                // probably won't work) and then notify the listeners
                if (!pingSuccessful) {
                    handlePingFailure();
                }
            } catch (Exception e) {
                LOG.error("LCDProxy$LCDPinger.run(): Exception caught while executing the peer pinger", e);
            }
        }

        private void handlePingFailure() {
            try {
                LOG.debug("LCDProxy$LCDPinger.handlePingFailure(): Peer ping failed.  Attempting to disconnect...");
                disconnect(false);
                LOG.debug("LCDProxy$LCDPinger.handlePingFailure(): Done disconnecting from the CarLCD");
            } catch (Exception e) {
                LOG.error("LCDProxy$LCDPinger.handlePingFailure(): Exeption caught while trying to disconnect from the CarLCD", e);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("LCDProxy$LCDPinger.handlePingFailure(): Notifying " + createLabDevicePingFailureEventListeners.size() + " listeners of ping failure...");
            }
            for (final CreateLabDevicePingFailureEventListener listener : createLabDevicePingFailureEventListeners) {
                try {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("   LCDProxy$LCDPinger.handlePingFailure(): Notifying " + listener);
                    }
                    listener.handlePingFailureEvent();
                } catch (Exception e) {
                    LOG.error("LCDProxy$LCDPinger.handlePingFailure(): Exeption caught while notifying SerialDevicePingFailureEventListener", e);
                }
            }
        }

        private void forceFailure() {
            handlePingFailure();
        }
    }
}
