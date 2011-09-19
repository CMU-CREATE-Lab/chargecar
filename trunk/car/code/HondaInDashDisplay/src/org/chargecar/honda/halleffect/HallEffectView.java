package org.chargecar.honda.halleffect;

import org.chargecar.honda.StreamingSerialPortDeviceView;

import java.util.PropertyResourceBundle;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class HallEffectView extends StreamingSerialPortDeviceView<HallEffectEvent>
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(HallEffectView.class.getName());

   protected void handleEventInGUIThread(final HallEffectEvent eventData)
      {
      // do nothing
      }
   }
