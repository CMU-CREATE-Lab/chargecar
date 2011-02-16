package edu.cmu.ri.createlab;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import edu.cmu.ri.createlab.commands.DisconnectCommandStrategy;
import edu.cmu.ri.createlab.commands.DisplayCommandStrategy;
import edu.cmu.ri.createlab.commands.GetErrorCodesCommandStrategy;
import edu.cmu.ri.createlab.commands.GetRPMCommandStrategy;
import edu.cmu.ri.createlab.commands.GetTemperatureCommandStrategy;
import edu.cmu.ri.createlab.commands.HandshakeCommandStrategy;
import edu.cmu.ri.createlab.commands.InputCommandStrategy;
import edu.cmu.ri.createlab.commands.OutputCommandStrategy;
import edu.cmu.ri.createlab.commands.ResetDisplayCommandStrategy;
import edu.cmu.ri.createlab.commands.ReturnValueCommandStrategy;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.serial.CreateLabSerialDeviceNoReturnValueCommandStrategy;
import edu.cmu.ri.createlab.serial.SerialPortCommandExecutionQueue;
import edu.cmu.ri.createlab.serial.SerialPortCommandResponse;
import edu.cmu.ri.createlab.serial.config.BaudRate;
import edu.cmu.ri.createlab.serial.config.CharacterSize;
import edu.cmu.ri.createlab.serial.config.FlowControl;
import edu.cmu.ri.createlab.serial.config.Parity;
import edu.cmu.ri.createlab.serial.config.SerialIOConfiguration;
import edu.cmu.ri.createlab.serial.config.StopBits;
import edu.cmu.ri.createlab.util.BoundedSequenceNumber;
import edu.cmu.ri.createlab.util.SequenceNumber;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class LCDProxy implements LCD {
    private static final Logger LOG = Logger.getLogger(LCDProxy.class);

    public static final String APPLICATION_NAME = "LCDProxy";
    private static final int DELAY_IN_SECONDS_BETWEEN_PEER_PINGS = 5;
    private static final int DELAY_IN_MILLISECONDS_BETWEEN_PEER_PINGS = 50;
    public static final SequenceNumber SEQUENCE_NUMBER = new BoundedSequenceNumber(0, 255);

    private static class LazyHolder {
        private static final LCDCreator INSTANCE = new LCDCreator();

        private LazyHolder() { }
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
            final SerialPortCommandExecutionQueue commandQueue = SerialPortCommandExecutionQueue.create(APPLICATION_NAME, config);

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

    private final SerialPortCommandExecutionQueue commandQueue;
    private final String serialPortName;
    private final CreateLabSerialDeviceNoReturnValueCommandStrategy disconnectCommandStrategy = new DisconnectCommandStrategy();

    private final NoReturnValueCommandExecutor noReturnValueCommandExecutor = new NoReturnValueCommandExecutor();
    private final LCDPinger lcdPinger = new LCDPinger();
    private final LCDButtonPoller lcdButtonPoller = new LCDButtonPoller();
    private final ScheduledExecutorService peerPingScheduler = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("LCDProxy.peerPingScheduler"));
    private final ScheduledFuture<?> peerPingScheduledFuture;
    private final Collection<CreateLabDevicePingFailureEventListener> createLabDevicePingFailureEventListeners = new HashSet<CreateLabDevicePingFailureEventListener>();
    private final Set<ButtonPanelEventListener> buttonPanelEventListeners = new HashSet<ButtonPanelEventListener>();

    private LCDProxy(final SerialPortCommandExecutionQueue commandQueue, final String serialPortName) {
        this.commandQueue = commandQueue;
        this.serialPortName = serialPortName;

        // schedule periodic peer pings
        peerPingScheduledFuture = peerPingScheduler.scheduleWithFixedDelay(lcdButtonPoller,
                DELAY_IN_MILLISECONDS_BETWEEN_PEER_PINGS, // delay before first ping
                DELAY_IN_MILLISECONDS_BETWEEN_PEER_PINGS, // delay between pings
                TimeUnit.MILLISECONDS);
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
        return noReturnValueCommandExecutor.executeAndReturnStatus(new DisplayCommandStrategy(row, column, displayString));
    }

    public Double getControllerTemperatureInKelvin() {
        return new ReturnValueCommandExecutor<Double>(new GetTemperatureCommandStrategy(LCDConstants.CONTROLLER_TEMPERATURE)).execute();
    }

    public Double getMotorTemperatureInKelvin() {
        return new ReturnValueCommandExecutor<Double>(new GetTemperatureCommandStrategy(LCDConstants.MOTOR_TEMPERATURE)).execute();
    }

    public Double getTemperatureInCelsius(final double temperatureInKelvin) {
        return temperatureInKelvin - LCDConstants.KELVIN_FREEZING_POINT;
    }

    public Double getTemperatureInFahrenheit(final double temperatureInKelvin) {
        return (LCDConstants.CELSIUS_TO_FAHRENHEIT_CONSTANT * getTemperatureInCelsius(temperatureInKelvin)) + LCDConstants.CELSIUS_FREEZING_POINT;
    }

    private int[] getInputs() {
        return new ReturnValueCommandExecutor<int[]>(new InputCommandStrategy()).execute();
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
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.AIR_CONDITIONING, LCDConstants.SET_STATE));
    }

    public boolean turnOnPowerSteering() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.POWER_STEERING, LCDConstants.SET_STATE));
    }

    public boolean turnOnCabinHeat() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.CABIN_HEAT, LCDConstants.SET_STATE));
    }

    public boolean turnOnDisplayBackLight() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.DISPLAY_BACK_LIGHT, LCDConstants.SET_STATE));
    }

    public boolean turnOnBrakeLight() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.BRAKE_LIGHT, LCDConstants.SET_STATE));
    }

    public boolean turnOnBatteryCooling() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.BATTERY_COOLING, LCDConstants.SET_STATE));
    }

    public boolean turnOnBatteryHeating() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.BATTERY_HEATING, LCDConstants.SET_STATE));
    }

    public boolean turnOnAccessoryOneLED() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.ACCESSORY_ONE_LED, LCDConstants.SET_STATE));
    }

    public boolean turnOnAccessoryTwoLED() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.ACCESSORY_TWO_LED, LCDConstants.SET_STATE));
    }

    public boolean turnOnAccessoryThreeLED() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.ACCESSORY_THREE_LED, LCDConstants.SET_STATE));
    }

    public boolean turnOffAirConditioning() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.AIR_CONDITIONING, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffPowerSteering() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.POWER_STEERING, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffCabinHeat() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.CABIN_HEAT, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffDisplayBackLight() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.DISPLAY_BACK_LIGHT, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffBrakeLight() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.BRAKE_LIGHT, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffBatteryCooling() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.BATTERY_COOLING, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffBatteryHeating() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.BATTERY_HEATING, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffAccessoryOneLED() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.ACCESSORY_ONE_LED, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffAccessoryTwoLED() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.ACCESSORY_TWO_LED, LCDConstants.CLEAR_STATE));
    }

    public boolean turnOffAccessoryThreeLED() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new OutputCommandStrategy(LCDConstants.ACCESSORY_THREE_LED, LCDConstants.CLEAR_STATE));
    }

    public Integer getRPM() {
        return new ReturnValueCommandExecutor<Integer>(new GetRPMCommandStrategy()).execute();
    }

    public Integer getMotorControllerErrorCodes() {
        return new ReturnValueCommandExecutor<Integer>(new GetErrorCodesCommandStrategy()).execute();
    }

    public boolean resetDisplay() {
        return noReturnValueCommandExecutor.executeAndReturnStatus(new ResetDisplayCommandStrategy());
    }
//chargecar end

    public void disconnect() {
        disconnect(true);
    }

    private void disconnect(final boolean willAddDisconnectCommandToQueue) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("LCDProxy.disconnect(" + willAddDisconnectCommandToQueue + ")");
        }

        // turn off the peer pinger
        try {
            peerPingScheduledFuture.cancel(false);
            peerPingScheduler.shutdownNow();
            LOG.debug("LCDProxy.disconnect(): Successfully shut down CarLCD pinger.");
        } catch (Exception e) {
            LOG.error("LCDProxy.disconnect(): Exception caught while trying to shut down peer pinger", e);
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
            LOG.debug("LCDProxy.disconnect(): shutting down the SerialPortCommandExecutionQueue...");
            commandQueue.shutdown();
            LOG.debug("LCDProxy.disconnect(): done shutting down the SerialPortCommandExecutionQueue");
        } catch (Exception e) {
            LOG.error("LCDProxy.disconnect(): Exception while trying to shut down the SerialPortCommandExecutionQueue", e);
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
                if (isSame) return;


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

    private final class ReturnValueCommandExecutor<T> {
        private final ReturnValueCommandStrategy<T> commandStrategy;

        private ReturnValueCommandExecutor(final ReturnValueCommandStrategy<T> commandStrategy) {
            this.commandStrategy = commandStrategy;
        }

        private T execute() {
            try {
                final SerialPortCommandResponse response = commandQueue.execute(commandStrategy);
                return commandStrategy.convertResult(response);
            } catch (Exception e) {
                LOG.error("Exception caught while trying to execute a command", e);
                lcdPinger.forceFailure();
            }

            return null;
        }
    }

    private final class NoReturnValueCommandExecutor {
        private boolean executeAndReturnStatus(final CreateLabSerialDeviceNoReturnValueCommandStrategy commandStrategy) {
            try {
                return commandQueue.executeAndReturnStatus(commandStrategy);
            } catch (Exception e) {
                LOG.error("Exception caught while trying to execute a command", e);
                lcdPinger.forceFailure();
            }

            return false;
        }
    }
}
