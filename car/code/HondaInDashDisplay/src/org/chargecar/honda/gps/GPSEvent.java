package org.chargecar.honda.gps;

import java.util.Date;
import org.chargecar.serial.streaming.BaseStreamingSerialPortEvent;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GPSEvent extends BaseStreamingSerialPortEvent
   {
   private static final double FEET_PER_METER = 3.2808399;

   private final String latitude;
   private final String longitude;
   private final Integer numSatellitesBeingTracked;
   private final long elevationInFeet;
   private final boolean isLocationEvent;

   public static GPSEvent createLocationEvent(final Date timestamp, final String latitude, final String longitude, final int numSatellitesBeingTracked, final float elevationInMeters)
      {
      return new GPSEvent(true, timestamp, latitude, longitude, numSatellitesBeingTracked, Math.round(elevationInMeters * FEET_PER_METER));
      }

   public static GPSEvent createElevationEvent(final Date timestamp, final int elevationInFeet)
      {
      return new GPSEvent(false, timestamp, null, null, null, elevationInFeet);
      }

   private GPSEvent(final boolean isLocationEvent, final Date timestamp, final String latitude, final String longitude, final Integer numSatellitesBeingTracked, final long elevationInFeet)
      {
      super(timestamp);
      this.isLocationEvent = isLocationEvent;
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

   public long getElevationInFeet()
      {
      return elevationInFeet;
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
      sb.append("GPSEvent:").append(isLocationEvent ? "Location" : "Elevation");
      sb.append("{");
      sb.append(field1).append(getTimestampMilliseconds());
      if (isLocationEvent)
         {
         sb.append(field2).append(latitude);
         sb.append(field3).append(longitude);
         sb.append(field4).append(numSatellitesBeingTracked);
         }
      sb.append(field5).append(elevationInFeet);
      sb.append('}');
      return sb.toString();
      }
   }
