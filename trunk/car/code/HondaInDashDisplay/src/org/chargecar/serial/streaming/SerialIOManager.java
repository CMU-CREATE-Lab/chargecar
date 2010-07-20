package org.chargecar.serial.streaming;

import java.io.IOException;
import edu.cmu.ri.createlab.serial.SerialPortException;
import edu.cmu.ri.createlab.serial.SerialPortIOHelper;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface SerialIOManager
   {
   boolean connect() throws SerialPortException, IOException;

   SerialPortIOHelper getSerialPortIoHelper();

   void disconnect();
   }
