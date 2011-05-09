package org.chargecar.lcddisplay;

import edu.cmu.ri.createlab.serial.SerialPortEnumerator;
import org.apache.log4j.Logger;
import org.chargecar.honda.StreamingSerialPortDeviceController;
import org.chargecar.honda.StreamingSerialPortDeviceModel;
import org.chargecar.honda.gps.GPSController;
import org.chargecar.honda.gps.GPSEvent;
import org.chargecar.honda.gps.GPSModel;
import org.chargecar.serial.streaming.StreamingSerialPortDeviceManager;

import java.util.SortedSet;

/**
 * <code>GPSManager</code> is a singleton which acts as a front-end for GPS data.  The singleton instance is created
 * lazily and, upon creation, connects to the GPS system.  The serial port name is assumed to be defined in a system
 * property whose key is the concatenation of {@link LCDConstants#SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX} and
 * {@link #DEVICE_NAME}.  If no such system property exists, then {@link #getInstance()} will return <code>null</code>.
 * Once a connection is established, it immediately starts reading data.  The most recent data can be obtained via the
 * {@link #getData()} method.
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GPSManager extends StreamingSerialPortDeviceManager<GPSEvent, GPSEvent> {
    private static final Logger LOG = Logger.getLogger(BMSManager.class);

    private static class LazyHolder {
        private static final GPSManager INSTANCE;

        static {
            GPSManager gpsManager = null;
            boolean wasFound = false;
            int portScanCount = 0;

            while (portScanCount < 5 && !wasFound) {
                // If the user specified one or more serial ports, then just start trying to connect to it/them.  Otherwise,
                // check each available serial port for the target serial device, and connect to the first one found.  This
                // makes connection time much faster for when you know the name of the serial port.
                LOG.debug("GPSManager: GPS port scan attempt " + portScanCount);
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
                            LOG.debug("GPSManager: checking serial port [" + portName + "]");
                        }

                        final GPSModel model = new GPSModel();
                        final GPSController controller = GPSController.create(portName, model);
                        gpsManager = new GPSManager(model, controller);

                        // sleep until we're reading...
                        int isReadingCount = 0;
                        do {
                            sleep(100);
                            isReadingCount++;
                        } while (!gpsManager.isReading() && isReadingCount < 20);

                        if (gpsManager.getData() != null) {
                            LOG.debug("GPSManager: Valid GPS port found.");
                            wasFound = true;
                            break;
                        }
                    }
                } else {
                    LOG.debug("GPSManager: No available serial ports.");
                    sleep(100);
                }
                portScanCount++;
            }

            INSTANCE = gpsManager;
        }

        private static void sleep(final int millisToSleep) {
            try {
                Thread.sleep(millisToSleep);
            } catch (InterruptedException e) {
                LOG.error("GPSManager.sleep(): sleep interrupted: " + e);
            }
        }

        private LazyHolder() {
            // private to prevent instantiation
        }
    }

    public static GPSManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private GPSManager(final StreamingSerialPortDeviceModel<GPSEvent, GPSEvent> model,
                       final StreamingSerialPortDeviceController<GPSEvent, GPSEvent> controller) {
        super(model, controller);
    }
}
