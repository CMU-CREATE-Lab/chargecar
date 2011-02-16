package org.chargecar.lcddisplay;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import edu.cmu.ri.createlab.util.runtime.LifecycleManager;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.log4j.Logger;
import org.chargecar.honda.StreamingSerialPortDeviceController;
import org.chargecar.honda.StreamingSerialPortDeviceView;
import org.chargecar.honda.bms.BMSAndEnergy;
import org.chargecar.honda.bms.BMSController;
import org.chargecar.honda.bms.BMSModel;
import org.chargecar.honda.gps.GPSController;
import org.chargecar.honda.gps.GPSEvent;
import org.chargecar.honda.gps.GPSModel;
import org.chargecar.honda.motorcontroller.MotorControllerController;
import org.chargecar.honda.motorcontroller.MotorControllerEvent;
import org.chargecar.honda.motorcontroller.MotorControllerModel;
import org.chargecar.honda.sensorboard.SensorBoardController;
import org.chargecar.honda.sensorboard.SensorBoardEvent;
import org.chargecar.honda.sensorboard.SensorBoardModel;
import org.chargecar.serial.streaming.StreamingSerialPortDeviceConnectionStateListener;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public class SensorBoard {
    private static final Logger LOG = Logger.getLogger(SensorBoard.class);
    public static final String SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX = SensorBoard.class.getName() + ".serial_port.";

    private static class LazyHolder {
        private static final SensorBoard INSTANCE = new SensorBoard();

        private LazyHolder() { }
    }

    public static SensorBoard getInstance() {
        return LazyHolder.INSTANCE;
    }

    private BMSAndEnergy bmsAndEnergy = null;
    private GPSEvent gpsEvent = null;
    private MotorControllerEvent motorControllerEvent = null;
    private SensorBoardEvent sensorBoardEvent = null;

    private SensorBoard() {
        final Map<String, String> deviceToSerialPortNameMap = new HashMap<String, String>(4);
        deviceToSerialPortNameMap.put("bms", System.getProperty(SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX + "bms", null));
        deviceToSerialPortNameMap.put("gps", System.getProperty(SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX + "gps", null));
        deviceToSerialPortNameMap.put("motor-controller", System.getProperty(SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX + "motor-controller", null));
        deviceToSerialPortNameMap.put("sensor-board", System.getProperty(SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX + "sensor-board", null));

        // create the models
        final BMSModel bmsModel = new BMSModel();
        final GPSModel gpsModel = new GPSModel();
        final MotorControllerModel motorControllerModel = new MotorControllerModel();
        final SensorBoardModel sensorBoardModel = new SensorBoardModel();

        // create the controllers
        final BMSController bmsController = BMSController.create(deviceToSerialPortNameMap.get("bms"), bmsModel);
        final GPSController gpsController = GPSController.create(deviceToSerialPortNameMap.get("gps"), gpsModel);
        final MotorControllerController motorControllerController = MotorControllerController.create(deviceToSerialPortNameMap.get("motor-controller"), motorControllerModel);
        final SensorBoardController sensorBoardController = SensorBoardController.create(deviceToSerialPortNameMap.get("sensor-board"), sensorBoardModel);

        final LifecycleManager lifecycleManager = new MyLifecycleManager(bmsController,
                gpsController,
                motorControllerController,
                sensorBoardController);

        final BMSView bmsView = new BMSView();
        final GPSView gpsView = new GPSView();
        final MotorControllerView motorControllerView = new MotorControllerView();
        final SensorBoardView sensorBoardView = new SensorBoardView();

        // add the various views as data event listeners to the models
        bmsModel.addEventListener(bmsView);
        gpsModel.addEventListener(gpsView);
        motorControllerModel.addEventListener(motorControllerView);
        sensorBoardModel.addEventListener(sensorBoardView);

        // add a listener to each model which starts the reading upon connection establishment
        bmsModel.addStreamingSerialPortDeviceConnectionStateListener(
                new StreamingSerialPortDeviceConnectionStateListener() {
                    public void handleConnectionStateChange(final boolean isConnected) {
                        if (isConnected) {
                            bmsController.startReading();
                        }
                    }
                });
        gpsModel.addStreamingSerialPortDeviceConnectionStateListener(
                new StreamingSerialPortDeviceConnectionStateListener() {
                    public void handleConnectionStateChange(final boolean isConnected) {
                        if (isConnected) {
                            gpsController.startReading();
                        }
                    }
                });
        motorControllerModel.addStreamingSerialPortDeviceConnectionStateListener(
                new StreamingSerialPortDeviceConnectionStateListener() {
                    public void handleConnectionStateChange(final boolean isConnected) {
                        if (isConnected) {
                            motorControllerController.startReading();
                        }
                    }
                });
        sensorBoardModel.addStreamingSerialPortDeviceConnectionStateListener(
                new StreamingSerialPortDeviceConnectionStateListener() {
                    public void handleConnectionStateChange(final boolean isConnected) {
                        if (isConnected) {
                            sensorBoardController.startReading();
                        }
                    }
                });

        lifecycleManager.startup();
    }

    public BMSAndEnergy getBmsAndEnergy() {
        return bmsAndEnergy;
    }

    public GPSEvent getGpsEvent() {
        return gpsEvent;
    }

    public MotorControllerEvent getMotorControllerEvent() {
        return motorControllerEvent;
    }

    public SensorBoardEvent getSensorBoardEvent() {
        return sensorBoardEvent;
    }

    private final class BMSView extends StreamingSerialPortDeviceView<BMSAndEnergy> {
        protected void handleEventInGUIThread(final BMSAndEnergy bmsAndEnergy) {
            SensorBoard.this.bmsAndEnergy = bmsAndEnergy;
        }
    }

    private final class GPSView extends StreamingSerialPortDeviceView<GPSEvent> {
        protected void handleEventInGUIThread(final GPSEvent gpsData) {
            SensorBoard.this.gpsEvent = gpsData;
        }
    }

    private final class MotorControllerView extends StreamingSerialPortDeviceView<MotorControllerEvent> {
        protected void handleEventInGUIThread(final MotorControllerEvent motorControllerData) {
            SensorBoard.this.motorControllerEvent = motorControllerData;
        }
    }

    private final class SensorBoardView extends StreamingSerialPortDeviceView<SensorBoardEvent> {
        protected void handleEventInGUIThread(final SensorBoardEvent sensorBoardData) {
            SensorBoard.this.sensorBoardEvent = sensorBoardData;
        }
    }

    private static class MyLifecycleManager implements LifecycleManager {
        private final Runnable startupRunnable;
        private final Runnable shutdownRunnable;
        private final ExecutorService executor = Executors.newCachedThreadPool(new DaemonThreadFactory("MyLifecycleManager.executor"));

        private MyLifecycleManager(final BMSController bmsController,
                                   final GPSController gpsController,
                                   final MotorControllerController motorControllerController,
                                   final SensorBoardController sensorBoardController) {
            startupRunnable =
                    new Runnable() {
                        private void connect(final String deviceName, final StreamingSerialPortDeviceController controller) {
                            if (controller == null) {
                                LOG.info("SensorBoard$MyLifecycleManager.run(): Controller for the " + deviceName + " given to the LifecycleManager constructor was null, so data won't be read.");
                            } else {
                                executor.submit(
                                        new Runnable() {
                                            public void run() {
                                                LOG.info("SensorBoard$MyLifecycleManager.run(): Attempting to establish a connection to the " + deviceName + "...");
                                                controller.connect();
                                            }
                                        });
                            }
                        }

                        public void run() {
                            connect("BMS", bmsController);
                            connect("GPS", gpsController);
                            connect("Motor Controller", motorControllerController);
                            connect("Sensor Board", sensorBoardController);
                        }
                    };

            shutdownRunnable =
                    new Runnable() {
                        private void disconnect(final String deviceName, final StreamingSerialPortDeviceController controller) {
                            if (controller == null) {
                                LOG.info("SensorBoard$MyLifecycleManager.run(): Controller for the " + deviceName + " given to the LifecycleManager constructor was null, so we won't try to shut it down.");
                            } else {
                                executor.submit(
                                        new Runnable() {
                                            public void run() {
                                                LOG.info("SensorBoard$MyLifecycleManager.run(): Disconnecting from the " + deviceName + "...");
                                                controller.disconnect();
                                            }
                                        });
                            }
                        }

                        public void run() {
                            disconnect("BMS", bmsController);
                            disconnect("GPS", gpsController);
                            disconnect("Motor Controller", motorControllerController);
                            disconnect("Sensor Board", sensorBoardController);

                            System.exit(0);
                        }
                    };
        }

        public void startup() {
            LOG.debug("LifecycleManager.startup()");

            executor.submit(startupRunnable);
        }

        public void shutdown() {
            LOG.debug("LifecycleManager.shutdown()");

            executor.submit(shutdownRunnable);
        }
    }
}
