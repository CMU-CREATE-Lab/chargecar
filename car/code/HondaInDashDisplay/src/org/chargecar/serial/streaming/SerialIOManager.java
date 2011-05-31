package org.chargecar.serial.streaming;

import java.io.IOException;
import edu.cmu.ri.createlab.serial.SerialDeviceIOHelper;
import edu.cmu.ri.createlab.serial.SerialPortException;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface SerialIOManager
   {
   boolean connect() throws SerialPortException, IOException;

   SerialDeviceIOHelper getSerialDeviceIOHelper();

   void disconnect();
   }
