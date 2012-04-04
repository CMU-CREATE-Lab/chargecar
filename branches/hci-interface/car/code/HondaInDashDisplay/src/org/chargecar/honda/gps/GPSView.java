package org.chargecar.honda.gps;

import java.util.PropertyResourceBundle;
import org.chargecar.honda.StreamingSerialPortDeviceView;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GPSView extends StreamingSerialPortDeviceView<GPSEvent>
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(GPSView.class.getName());

   protected void handleEventInGUIThread(final GPSEvent eventData)
      {
      // do nothing
      }
   }
