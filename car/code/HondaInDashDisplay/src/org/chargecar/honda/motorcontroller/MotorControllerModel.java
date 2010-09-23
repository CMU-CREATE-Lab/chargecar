package org.chargecar.honda.motorcontroller;

import org.apache.log4j.Logger;
import org.chargecar.honda.StreamingSerialPortDeviceModel;

/**
 * <p>
 * <code>MotorControllerModel</code> keeps track of motor controller data.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class MotorControllerModel extends StreamingSerialPortDeviceModel<MotorControllerEvent, MotorControllerEvent>
   {
   private static final Logger LOG = Logger.getLogger(MotorControllerModel.class);

   private final byte[] dataSynchronizationLock = new byte[0];

   public MotorControllerEvent update(final MotorControllerEvent data)
      {
      synchronized (dataSynchronizationLock)
         {
         if (LOG.isInfoEnabled())
            {
            LOG.info(data.toLoggingString());
            }

         publishEventToListeners(data);

         return data;
         }
      }
   }
