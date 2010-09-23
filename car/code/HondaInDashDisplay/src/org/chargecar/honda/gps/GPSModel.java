package org.chargecar.honda.gps;

import org.apache.log4j.Logger;
import org.chargecar.honda.StreamingSerialPortDeviceModel;

/**
 * <p>
 * <code>GPSModel</code> keeps track of GPS data.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GPSModel extends StreamingSerialPortDeviceModel<GPSEvent, GPSEvent>
   {
   private static final Logger LOG = Logger.getLogger(GPSModel.class);

   private final byte[] dataSynchronizationLock = new byte[0];

   public GPSEvent update(final GPSEvent data)
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
