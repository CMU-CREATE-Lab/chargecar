package org.chargecar.honda.gps;

import java.util.Date;
import org.chargecar.serial.streaming.BaseStreamingSerialPortEvent;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GPSEvent extends BaseStreamingSerialPortEvent
   {
   private final String latitude;
   private final String longitude;
   private final Integer numSatellitesBeingTracked;
   private final Integer elevationInFeet;

   public static GPSEvent createLocationEvent(final Date timestamp, final String latitude, final String longitude, final int numSatellitesBeingTracked)
      {
      return new GPSEvent(timestamp, latitude, longitude, numSatellitesBeingTracked, null);
      }

   public static GPSEvent createElevationEvent(final Date timestamp, final int elevationInFeet)
      {
      return new GPSEvent(timestamp, null, null, null, elevationInFeet);
      }

   private GPSEvent(final Date timestamp, final String latitude, final String longitude, final Integer numSatellitesBeingTracked, final Integer elevationInFeet)
      {
      super(timestamp);
      this.latitude = latitude;
      this.longitude = longitude;
      this.numSatellitesBeingTracked = numSatellitesBeingTracked;
      this.elevationInFeet = elevationInFeet;
      }

   public String getLatitude()
      {
      return latitude;
      }

   public String getLongitude()
      {
      return longitude;
      }

   public Integer getNumSatellitesBeingTracked()
      {
      return numSatellitesBeingTracked;
      }

   public Integer getElevationInFeet()
      {
      return elevationInFeet;
      }

   public boolean isLocationEvent()
      {
      return elevationInFeet == null;
      }

   public boolean isElevationEvent()
      {
      return elevationInFeet != null;
      }

   @Override
   public String toString()
      {
      return toString("timestamp=",
                      ", latitude=",
                      ", longitude=",
                      ", numSatellites=",
                      ", elevation=");
      }

   public String toLoggingString()
      {
      return toString("", TO_STRING_DELIMITER, TO_STRING_DELIMITER, TO_STRING_DELIMITER, TO_STRING_DELIMITER);
      }

   private String toString(final String field1, final String field2, final String field3, final String field4, final String field5)
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("GPSEvent:").append(isLocationEvent() ? "Location" : "Elevation");
      sb.append("{");
      sb.append(field1).append(getTimestampMilliseconds());
      if (isLocationEvent())
         {
         sb.append(field2).append(latitude);
         sb.append(field3).append(longitude);
         sb.append(field4).append(numSatellitesBeingTracked);
         }
      else
         {
         sb.append(field5).append(elevationInFeet);
         }
      sb.append('}');
      return sb.toString();
      }
   }
