package org.chargecar.honda.motorcontroller;

import java.util.Date;

/**
 * <p>
 * <code>MotorControllerEvent</code> does something...
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class MotorControllerEvent
   {
   private static final String TO_STRING_DELIMITER = "\t";

   private final Date timestamp;
   private final int rpm;

   public MotorControllerEvent(final Date timestamp, final int rpm)
      {
      this.timestamp = timestamp;
      this.rpm = rpm;
      }

   public long getTimestampMilliseconds()
      {
      return timestamp.getTime();
      }

   public int getRPM()
      {
      return rpm;
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

      final MotorControllerEvent that = (MotorControllerEvent)o;

      if (rpm != that.rpm)
         {
         return false;
         }
      if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null)
         {
         return false;
         }

      return true;
      }

   @Override
   public int hashCode()
      {
      int result = timestamp != null ? timestamp.hashCode() : 0;
      result = 31 * result + rpm;
      return result;
      }

   @Override
   public String toString()
      {
      return toString("timestamp=",
                      ", rpm=");
      }

   public String toLoggingString()
      {
      return toString("", TO_STRING_DELIMITER);
      }

   private String toString(final String field1, final String field2)
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("MotorControllerEvent");
      sb.append("{");
      sb.append(field1).append(timestamp.getTime());
      sb.append(field2).append(rpm);
      sb.append('}');
      return sb.toString();
      }
   }
