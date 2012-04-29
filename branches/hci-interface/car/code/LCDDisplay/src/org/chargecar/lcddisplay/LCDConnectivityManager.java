package org.chargecar.lcddisplay;

import edu.cmu.ri.createlab.device.CreateLabDeviceProxy;
import edu.cmu.ri.createlab.device.connectivity.BaseCreateLabDeviceConnectivityManager;
import org.apache.log4j.Logger;

import edu.cmu.ri.createlab.serial.SerialPortEnumerator;
import java.util.Collections;
import java.util.SortedSet;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class LCDConnectivityManager extends BaseCreateLabDeviceConnectivityManager {
    private static final Logger LOG = Logger.getLogger(LCDConnectivityManager.class);

    protected CreateLabDeviceProxy scanForDeviceAndCreateProxy() {
        LOG.debug("LCDConnectivityManager.scanForDeviceAndCreateProxy()");

        // If the user specified one or more serial ports, then just start trying to connect to it/them.  Otherwise,
        // check each available serial port for the target serial device, and connect to the first one found.  This
        // makes connection time much faster for when you know the name of the serial port.
        final SortedSet<String> availableSerialPorts;
     if (SerialPortEnumerator.didUserDefineSetOfSerialPorts())
        {
        availableSerialPorts = SerialPortEnumerator.getSerialPorts();
        }
     else
        {
        availableSerialPorts = SerialPortEnumerator.getAvailableSerialPorts();
        }
        // try the serial ports
        if (availableSerialPorts != null && !(availableSerialPorts.isEmpty())) {
            for (final String portName : availableSerialPorts) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("LCDConnectivityManager.scanForDeviceAndCreateProxy(): checking serial port [" + portName + "]");
                }

                final CreateLabDeviceProxy proxy = LCDProxy.create(portName);

                if (proxy == null) {
                    LOG.debug("LCDConnectivityManager.scanForDeviceAndCreateProxy(): connection failed, maybe it's not the device we're looking for?");
                } else {
                    LOG.debug("LCDConnectivityManager.scanForDeviceAndCreateProxy(): connection established, returning proxy!");
                    availableSerialPorts.remove(portName);
                    return proxy;
                }
            }
        } else {
            LOG.debug("LCDConnectivityManager.scanForDeviceAndCreateProxy(): No available serial ports, returning null.");
        }

        return null;
    }
}
