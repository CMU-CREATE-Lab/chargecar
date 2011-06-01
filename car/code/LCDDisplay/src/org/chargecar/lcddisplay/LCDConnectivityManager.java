package org.chargecar.lcddisplay;

import edu.cmu.ri.createlab.device.connectivity.BaseCreateLabDeviceConnectivityManager;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class LCDConnectivityManager extends BaseCreateLabDeviceConnectivityManager<LCD> {
    private static final Logger LOG = Logger.getLogger(LCDConnectivityManager.class);

    protected LCD scanForDeviceAndCreateProxy() {
        LOG.debug("LCDConnectivityManager.scanForDeviceAndCreateProxy()");

        // If the user specified one or more serial ports, then just start trying to connect to it/them.  Otherwise,
        // check each available serial port for the target serial device, and connect to the first one found.  This
        // makes connection time much faster for when you know the name of the serial port.
        /*final SortedSet<String> availableSerialPorts;
     if (SerialPortEnumerator.didUserDefineSetOfSerialPorts())
        {
        availableSerialPorts = SerialPortEnumerator.getSerialPorts();
        }
     else
        {
        availableSerialPorts = SerialPortEnumerator.getAvailableSerialPorts();
        }*/

        // try the serial ports
        if ((ChargeCarLCD.getAvailableSerialPorts() != null) && (!ChargeCarLCD.getAvailableSerialPorts().isEmpty())) {
            for (final String portName : ChargeCarLCD.getAvailableSerialPorts()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("LCDConnectivityManager.scanForDeviceAndCreateProxy(): checking serial port [" + portName + "]");
                }

                final LCD proxy = LCDProxy.create(portName);

                if (proxy == null) {
                    LOG.debug("LCDConnectivityManager.scanForDeviceAndCreateProxy(): connection failed, maybe it's not the device we're looking for?");
                } else {
                    LOG.debug("LCDConnectivityManager.scanForDeviceAndCreateProxy(): connection established, returning proxy!");
                    ChargeCarLCD.removeAvailableSerialPort(portName);
                    return proxy;
                }
            }
        } else {
            LOG.debug("LCDConnectivityManager.scanForDeviceAndCreateProxy(): No available serial ports, returning null.");
        }

        return null;
    }
}
