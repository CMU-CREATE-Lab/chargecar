package org.chargecar.honda.gps;

import java.util.PropertyResourceBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.honda.StreamingSerialPortDeviceView;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GPSView extends StreamingSerialPortDeviceView<GPSEvent>
   {
   private static final Log LOG = LogFactory.getLog(GPSView.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(GPSView.class.getName());

   protected void handleEventInGUIThread(final GPSEvent eventData)
      {
      LOG.debug("GPSView.handleEventInGUIThread()");

      // TODO
      }
   }
