package org.chargecar.lcddisplay;

import org.apache.log4j.Logger;
import org.chargecar.honda.StreamingSerialPortDeviceController;
import org.chargecar.honda.StreamingSerialPortDeviceModel;
import org.chargecar.honda.halleffect.HallEffectController;
import org.chargecar.honda.halleffect.HallEffectEvent;
import org.chargecar.honda.halleffect.HallEffectModel;
import org.chargecar.serial.streaming.StreamingSerialPortDeviceManager;

/**
 * <code>HallEffectManager</code> is a singleton which acts as a front-end for Hall Effect data.  The singleton instance is created
 * lazily and, upon creation, connects to the Hall Effect system.  The serial port name is assumed to be defined in a system
 * property whose key is the concatenation of {@link org.chargecar.lcddisplay.LCDConstants#SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX} and
 * {@link #DEVICE_NAME}.  If no such system property exists, then {@link #getInstance()} will return <code>null</code>.
 * Once a connection is established, it immediately starts reading data.  The most recent data can be obtained via the
 * {@link #getData()} method.
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class HallEffectManager extends StreamingSerialPortDeviceManager<HallEffectEvent, HallEffectEvent> {
    private static final Logger LOG = Logger.getLogger(HallEffectManager.class);
    private static final String DEVICE_NAME = "halleffect";
    private static class LazyHolder {
        private static final HallEffectManager INSTANCE;

        static {
            HallEffectManager hallEffectManager = null;
            boolean wasFound = false;
            int portScanCount = 0;

            while (portScanCount < 5 && !wasFound) {
                // If the user specified one or more serial ports, then just start trying to connect to it/them.  Otherwise,
                // check each available serial port for the target serial device, and connect to the first one found.  This
                // makes connection time much faster for when you know the name of the serial port.
                LOG.debug("HallEffectManager: Hall effect port scan attempt " + portScanCount);
                /*final SortedSet<String> availableSerialPorts;
                if (SerialPortEnumerator.didUserDefineSetOfSerialPorts()) {
                    availableSerialPorts = SerialPortEnumerator.getSerialPorts();
                } else {
                    availableSerialPorts = SerialPortEnumerator.getAvailableSerialPorts();
                }*/

                // try the serial ports
                if ((ChargeCarLCD.getAvailableSerialPorts() != null) && (!ChargeCarLCD.getAvailableSerialPorts().isEmpty())) {
                    for (final String portName : ChargeCarLCD.getAvailableSerialPorts()) {

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("HallEffectManager: checking serial port [" + portName + "]");
                        }

                        final HallEffectModel model = new HallEffectModel();
                        final HallEffectController controller = HallEffectController.create(portName, model);
                        hallEffectManager = new HallEffectManager(model, controller);

                        // sleep until we're reading...
                        int isReadingCount = 0;
                        int getDataCount = 0;
                        do {
                            sleep(100);
                            isReadingCount++;
                        } while (!hallEffectManager.isReading() && isReadingCount < 20);

                        LOG.debug("HallEffectManager isReading: " + hallEffectManager.isReading());

                        do {
                            sleep(100);
                            getDataCount++;
                        } while (hallEffectManager.getData() == null && getDataCount < 20);

                        LOG.debug("HallEffectManager getData: " + hallEffectManager.getData());
                        if (hallEffectManager.getData() == null) {
                            hallEffectManager.shutdown();
                        } else {
                            LOG.debug("HallEffectManager: Valid HallEffect port found.");
                            ChargeCarLCD.removeAvailableSerialPort(portName);
                            wasFound = true;
                            break;
                        }
                    }
                } else {
                    LOG.debug("HallEffectManager: No available serial ports.");
                    sleep(100);
                }
                portScanCount++;
            }

            INSTANCE = hallEffectManager;

        }

        private static void sleep(final int millisToSleep) {
            try {
                Thread.sleep(millisToSleep);
            } catch (InterruptedException e) {
                LOG.error("HallEffectManager.sleep(): sleep interrupted: " + e);
            }
        }

        private LazyHolder() {
            // private to prevent instantiation
        }
    }

    public static HallEffectManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private HallEffectManager(final StreamingSerialPortDeviceModel<HallEffectEvent, HallEffectEvent> model,
                              final StreamingSerialPortDeviceController<HallEffectEvent, HallEffectEvent> controller) {
        super(model, controller);
    }
}
