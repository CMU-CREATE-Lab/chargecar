package org.chargecar.honda.bms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.honda.StreamingSerialPortDeviceModel;

/**
 * <p>
 * <code>BMSModel</code> keeps track of BMS data.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class BMSModel extends StreamingSerialPortDeviceModel<BMSEvent, BMSEvent>
   {
   private static final Log LOG = LogFactory.getLog(BMSModel.class);

   private final byte[] dataSynchronizationLock = new byte[0];

   public BMSEvent update(final BMSEvent data)
      {
      synchronized (dataSynchronizationLock)
         {
         if (LOG.isInfoEnabled())
            {
            // TODO: switch to toLoggingString()
            LOG.info(data);
            }

         publishEventToListeners(data);

         return data;
         }
      }
   }
