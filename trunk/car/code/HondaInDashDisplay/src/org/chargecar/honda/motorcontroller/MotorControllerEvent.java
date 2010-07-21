package org.chargecar.honda.motorcontroller;

import java.util.Date;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class MotorControllerEvent
   {
   private static final String TO_STRING_DELIMITER = "\t";

   private final Date timestamp;
   private final Integer rpm;
   private final Integer errorCode;

   public static MotorControllerEvent createNormalEvent(final Date timestamp, final int rpm)
      {
      return new MotorControllerEvent(timestamp, rpm, null);
      }

   public static MotorControllerEvent createErrorEvent(final Date timestamp, final int errorCode)
      {
      return new MotorControllerEvent(timestamp, null, errorCode);
      }

   private MotorControllerEvent(final Date timestamp, final Integer rpm, final Integer errorCode)
      {
      this.timestamp = timestamp;
      this.rpm = rpm;
      this.errorCode = errorCode;
      }

   public long getTimestampMilliseconds()
      {
      return timestamp.getTime();
      }

   public Integer getRPM()
      {
      return rpm;
      }

   public Integer getErrorCode()
      {
      return errorCode;
      }

   public boolean isError()
      {
      return errorCode != null;
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

      if (errorCode != null ? !errorCode.equals(that.errorCode) : that.errorCode != null)
         {
         return false;
         }
      if (rpm != null ? !rpm.equals(that.rpm) : that.rpm != null)
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
      result = 31 * result + (rpm != null ? rpm.hashCode() : 0);
      result = 31 * result + (errorCode != null ? errorCode.hashCode() : 0);
      return result;
      }

   @Override
   public String toString()
      {
      return toString("timestamp=",
                      ", rpm=",
                      ", errorCode=");
      }

   public String toLoggingString()
      {
      return toString("", TO_STRING_DELIMITER, TO_STRING_DELIMITER);
      }

   private String toString(final String field1, final String field2, final String field3)
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("MotorControllerEvent");
      sb.append("{");
      sb.append(field1).append(timestamp.getTime());
      sb.append(field2).append(rpm);
      sb.append(field3).append(errorCode);
      sb.append('}');
      return sb.toString();
      }
   }
