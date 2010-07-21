package org.chargecar.honda.sensorboard;

import java.util.Date;
import org.chargecar.serial.streaming.BaseStreamingSerialPortEvent;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SensorBoardEvent extends BaseStreamingSerialPortEvent
   {
   private final double motorTemperature;
   private final double controllerTemperature;
   private final int throttleValue;
   private final int regenValue;

   public SensorBoardEvent(final Date timestamp, final double motorTemperature, final double controllerTemperature, final int throttleValue, final int regenValue)
      {
      super(timestamp);
      this.motorTemperature = motorTemperature;
      this.controllerTemperature = controllerTemperature;
      this.throttleValue = throttleValue;
      this.regenValue = regenValue;
      }

   public double getMotorTemperature()
      {
      return motorTemperature;
      }

   public double getControllerTemperature()
      {
      return controllerTemperature;
      }

   public int getThrottleValue()
      {
      return throttleValue;
      }

   public int getRegenValue()
      {
      return regenValue;
      }

   @Override
   public boolean equals(final Object o)
      {
      if (this == o)
         {
         return true;
         }
      if (o == null || getClass() != o.getClass())
         {
         return false;
         }
      if (!super.equals(o))
         {
         return false;
         }

      final SensorBoardEvent that = (SensorBoardEvent)o;

      if (Double.compare(that.controllerTemperature, controllerTemperature) != 0)
         {
         return false;
         }
      if (Double.compare(that.motorTemperature, motorTemperature) != 0)
         {
         return false;
         }
      if (regenValue != that.regenValue)
         {
         return false;
         }
      if (throttleValue != that.throttleValue)
         {
         return false;
         }

      return true;
      }

   @Override
   public int hashCode()
      {
      int result = super.hashCode();
      long temp;
      temp = motorTemperature != +0.0d ? Double.doubleToLongBits(motorTemperature) : 0L;
      result = 31 * result + (int)(temp ^ (temp >>> 32));
      temp = controllerTemperature != +0.0d ? Double.doubleToLongBits(controllerTemperature) : 0L;
      result = 31 * result + (int)(temp ^ (temp >>> 32));
      result = 31 * result + throttleValue;
      result = 31 * result + regenValue;
      return result;
      }

   @Override
   public String toString()
      {
      return toString("timestamp=",
                      ", motorTemp=",
                      ", controllerTemp=",
                      ", throttle=",
                      ", regen=");
      }

   public String toLoggingString()
      {
      return toString("", TO_STRING_DELIMITER, TO_STRING_DELIMITER, TO_STRING_DELIMITER, TO_STRING_DELIMITER);
      }

   private String toString(final String field1, final String field2, final String field3, final String field4, final String field5)
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("SensorBoardEvent");
      sb.append("{");
      sb.append(field1).append(getTimestampMilliseconds());
      sb.append(field2).append(motorTemperature);
      sb.append(field3).append(controllerTemperature);
      sb.append(field4).append(throttleValue);
      sb.append(field5).append(regenValue);
      sb.append('}');
      return sb.toString();
      }
   }
