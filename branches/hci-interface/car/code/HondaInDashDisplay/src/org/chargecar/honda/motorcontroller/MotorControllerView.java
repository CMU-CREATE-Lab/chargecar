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

   private final ChargeGauge<Integer> rpmGauge = new ChargeGauge<Integer>(ChargeGauge.TYPE_RPM);

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
				rpmGauge.setValue(1600, HondaConstants.RED);
            }
         else
            {
            	rpmGauge.setValue(eventData.getRPM() == null ? null : eventData.getRPM());
            }
         }
      else
         {
         rpmGauge.setValue(null);
         }
      }
   }
