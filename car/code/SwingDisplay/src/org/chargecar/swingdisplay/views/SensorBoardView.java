package org.chargecar.honda.sensorboard;

import java.util.PropertyResourceBundle;
import org.chargecar.honda.Gauge;
import org.chargecar.honda.StreamingSerialPortDeviceView;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SensorBoardView extends StreamingSerialPortDeviceView<SensorBoardEvent>
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(SensorBoardView.class.getName());

   private final Gauge<Double> motorTempGauge = new Gauge<Double>(RESOURCES.getString("label.motor-temp"), "%6.2f");
   private final Gauge<Double> motorControllerTempGauge = new Gauge<Double>(RESOURCES.getString("label.motor-controller-temp"), "%6.2f");

   public Gauge<Double> getMotorTempGauge()
      {
      return motorTempGauge;
      }

   public Gauge<Double> getMotorControllerTempGauge()
      {
      return motorControllerTempGauge;
      }

   protected void handleEventInGUIThread(final SensorBoardEvent eventData)
      {
      if (eventData != null)
         {
         motorTempGauge.setValue(eventData.getMotorTemperature());
         motorControllerTempGauge.setValue(eventData.getControllerTemperature());
         }
      else
         {
         motorTempGauge.setValue(null);
         motorControllerTempGauge.setValue(null);
         }
      }
   }
