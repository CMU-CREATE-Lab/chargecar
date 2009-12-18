package org.chargecar.sensorboard.serial.proxy;

import edu.cmu.ri.createlab.serial.device.SerialDeviceProxy;
import edu.cmu.ri.createlab.serial.device.SerialDeviceProxyCreator;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SensorBoardSerialDeviceProxyCreator implements SerialDeviceProxyCreator
   {
   public SerialDeviceProxy createSerialDeviceProxy(final String serialPortName)
      {
      return SensorBoardProxy.create(serialPortName);
      }
   }
