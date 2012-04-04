package org.chargecar.honda.motorcontroller;

import java.util.PropertyResourceBundle;
import org.chargecar.honda.Gauge;
import org.chargecar.honda.HondaConstants;
import org.chargecar.honda.StreamingSerialPortDeviceView;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class MotorControllerView extends StreamingSerialPortDeviceView<MotorControllerEvent>
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(MotorControllerView.class.getName());

   private final Gauge<String> rpmGauge = new Gauge<String>(RESOURCES.getString("label.rpm"), "%s");

   public Gauge<String> getRpmGauge()
      {
      return rpmGauge;
      }

   protected void handleEventInGUIThread(final MotorControllerEvent eventData)
      {
      if (eventData != null)
         {
         if (eventData.isError())
            {
            rpmGauge.setValue(RESOURCES.getString("label.error") + " " + eventData.getErrorCode(), HondaConstants.RED);
            }
         else
            {
            rpmGauge.setValue(eventData.getRPM() == null ? null : eventData.getRPM().toString());
            }
         }
      else
         {
         rpmGauge.setValue(null);
         }
      }
   }
