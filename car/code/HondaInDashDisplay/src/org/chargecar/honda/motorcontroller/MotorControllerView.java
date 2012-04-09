package org.chargecar.honda.motorcontroller;

import java.util.PropertyResourceBundle;
import org.chargecar.honda.Gauge;
import org.chargecar.honda.ChargeGauge;
import org.chargecar.honda.HondaConstants;
import org.chargecar.honda.StreamingSerialPortDeviceView;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class MotorControllerView extends StreamingSerialPortDeviceView<MotorControllerEvent>
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(MotorControllerView.class.getName());

   private final ChargeGauge<Integer> rpmGauge = new ChargeGauge<Integer>(RESOURCES.getString("label.rpm"), "%s");

   public ChargeGauge<Integer> getRpmGauge()
      {
      return rpmGauge;
      }

   protected void handleEventInGUIThread(final MotorControllerEvent eventData)
      {
      if (eventData != null)
         {
         if (eventData.isError())
            {
           // rpmGauge.setValue(RESOURCES.getString("label.error") + " " + eventData.getErrorCode(), HondaConstants.RED);
				//rpmGauge.setValue(1);
            }
         else
            {
            	rpmGauge.setValue(eventData.getRPM() == null ? null : eventData.getRPM());
				//rpmGauge.setValue(59);
            }
         }
      else
         {
         rpmGauge.setValue(null);
         }
      }
   }
