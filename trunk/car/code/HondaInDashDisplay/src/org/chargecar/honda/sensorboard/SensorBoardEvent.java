package org.chargecar.honda.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SensorBoardEvent
   {
   private final double motorTemperature;
   private final double controllerTemperature;
   private final int throttleValue;
   private final int regenValue;

   public SensorBoardEvent(final double motorTemperature, final double controllerTemperature, final int throttleValue, final int regenValue)
      {
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

   public int hashCode()
      {
      int result;
      long temp;
      temp = motorTemperature != +0.0d ? Double.doubleToLongBits(motorTemperature) : 0L;
      result = (int)(temp ^ (temp >>> 32));
      temp = controllerTemperature != +0.0d ? Double.doubleToLongBits(controllerTemperature) : 0L;
      result = 31 * result + (int)(temp ^ (temp >>> 32));
      result = 31 * result + throttleValue;
      result = 31 * result + regenValue;
      return result;
      }

   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("SensorBoardEvent");
      sb.append("{motorTemperature=").append(motorTemperature);
      sb.append(", controllerTemperature=").append(controllerTemperature);
      sb.append(", throttleValue=").append(throttleValue);
      sb.append(", regenValue=").append(regenValue);
      sb.append('}');
      return sb.toString();
      }
   }
