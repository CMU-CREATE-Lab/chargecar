package org.chargecar.swingdisplay.views;

import java.util.PropertyResourceBundle;
import org.chargecar.honda.Gauge;
import org.chargecar.honda.sensorboard.SensorBoardEvent;
import org.chargecar.swingdisplay.TextGauge;
import org.chargecar.swingdisplay.ChargeGauge;
import org.chargecar.swingdisplay.AbstractGauge;
import org.chargecar.honda.StreamingSerialPortDeviceView;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SensorBoardView extends StreamingSerialPortDeviceView<SensorBoardEvent>
   {
   private final TextGauge<Double> motorTempGauge = new TextGauge<Double>("Motor Temp (C)", "%6.2f");
   private final TextGauge<Double> motorControllerTempGauge = new TextGauge<Double>("Motor Controller (C)", "%6.2f");

   public AbstractGauge<Double> getMotorTempGauge()
      {
      return motorTempGauge;
      }

   public AbstractGauge<Double> getMotorControllerTempGauge()
      {
      return motorControllerTempGauge;
      }

   protected void handleEventInGUIThread(final SensorBoardEvent eventData)
      {
      if (eventData != null)
         {
         motorTempGauge.setValue(eventData.getMotorTemperature(), ChargeGauge.textColor);
         motorControllerTempGauge.setValue(eventData.getControllerTemperature(), ChargeGauge.textColor);
         }
      else
         {
         motorTempGauge.setValue(null);
         motorControllerTempGauge.setValue(null);
         }
      }
   }
