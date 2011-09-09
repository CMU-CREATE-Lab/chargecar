package org.chargecar.lcddisplay;

import edu.cmu.ri.createlab.serial.SerialPortEnumerator;
import org.apache.log4j.Logger;
import org.chargecar.honda.StreamingSerialPortDeviceController;
import org.chargecar.honda.StreamingSerialPortDeviceModel;
import org.chargecar.honda.bms.BMSAndEnergy;
import org.chargecar.honda.bms.BMSController;
import org.chargecar.honda.bms.BMSEvent;
import org.chargecar.honda.bms.BMSModel;
import org.chargecar.serial.streaming.StreamingSerialPortDeviceManager;

import java.util.SortedSet;

/**
 * <code>BMSManager</code> is a singleton which acts as a front-end for BMS data.  The singleton instance is created
 * lazily and, upon creation, connects to the BMS system.  The serial port name is assumed to be defined in a system
 * property whose key is the concatenation of {@link LCDConstants#SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX} and
 * {@link #DEVICE_NAME}.  If no such system property exists, then {@link #getInstance()} will return <code>null</code>.
 * Once a connection is established, it immediately starts reading data.  The most recent data can be obtained via the
 * {@link #getData()} method.
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class BMSManager extends StreamingSerialPortDeviceManager<BMSEvent, BMSAndEnergy> {
    private static final Logger LOG = Logger.getLogger(BMSManager.class);
    private static final String DEVICE_NAME = "bms";
    private static class LazyHolder {
        private static final BMSManager INSTANCE;

        static {
            BMSManager bmsManager = null;
            int portScanCount = 0;

            while (portScanCount < 5) {
                // If the user specified one or more serial ports, then just start trying to connect to it/them.  Otherwise,
                // check each available serial port for the target serial device, and connect to the first one found.  This
                // makes connection time much faster for when you know the name of the serial port.
                LOG.debug("BMSManager: BMS port scan attempt " + portScanCount);
                final SortedSet<String> availableSerialPorts;
                if (SerialPortEnumerator.didUserDefineSetOfSerialPorts()) {
                    availableSerialPorts = SerialPortEnumerator.getSerialPorts();
                } else {
                    availableSerialPorts = SerialPortEnumerator.getAvailableSerialPorts();
                }

                // try the serial ports
                if ((availableSerialPorts != null) && (!availableSerialPorts.isEmpty())) {
                    for (final String portName : availableSerialPorts) {

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("BMSManager: checking serial port [" + portName + "]");
                        }

                        final BMSModel model = new BMSModel();
                        final BMSController controller = BMSController.create(portName, model);
                        bmsManager = new BMSManager(model, controller);

                        // sleep until we're reading...
                        int isReadingCount = 0;
                        int getDataCount = 0;
                        do {
                            sleep(100);
                            isReadingCount++;
                        } while (!bmsManager.isReading() && isReadingCount < 20);

                        LOG.debug("BMSManager isReading: " + bmsManager.isReading());

                        do {
                            sleep(100);
                            getDataCount++;
                        } while (bmsManager.getData() == null && getDataCount < 20);

                        LOG.debug("BMSManager getData: " + bmsManager.getData());
                        if (bmsManager.getData() == null) {
                            bmsManager.shutdown();
                        } else {
                            LOG.debug("BMSManager: Valid BMS port found.");
                            break;
                        }
                    }
                } else {
                    LOG.debug("BMSManager: No available serial ports.");
                    sleep(100);
                }
                portScanCount++;
            }

            INSTANCE = bmsManager;

        }

        private static void sleep(final int millisToSleep) {
            try {
                Thread.sleep(millisToSleep);
            } catch (InterruptedException e) {
                LOG.error("BMSManager.sleep(): sleep interrupted: " + e);
            }
        }

        private LazyHolder() {
            // private to prevent instantiation
        }
    }

    public static BMSManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private BMSManager(final StreamingSerialPortDeviceModel<BMSEvent, BMSAndEnergy> model,
                       final StreamingSerialPortDeviceController<BMSEvent, BMSAndEnergy> controller) {
        super(model, controller);
    }
}
