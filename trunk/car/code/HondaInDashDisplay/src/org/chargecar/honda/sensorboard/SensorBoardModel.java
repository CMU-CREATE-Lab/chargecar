package org.chargecar.honda.sensorboard;

import org.apache.log4j.Logger;
import org.chargecar.honda.StreamingSerialPortDeviceModel;

/**
 * <p>
 * <code>SensorBoardModel</code> keeps track of sensor board data.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class SensorBoardModel extends StreamingSerialPortDeviceModel<SensorBoardEvent, SensorBoardEvent>
   {
   private static final Logger LOG = Logger.getLogger(SensorBoardModel.class);

   private final byte[] dataSynchronizationLock = new byte[0];

   public SensorBoardEvent update(final SensorBoardEvent data)
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
