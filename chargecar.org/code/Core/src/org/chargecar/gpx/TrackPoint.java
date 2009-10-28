package org.chargecar.gpx;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class TrackPoint
   {
   private static final Log LOG = LogFactory.getLog(TrackPoint.class);

   private static final DateTimeFormatter isoDateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();
   private static final DateTimeFormatter isoDateTimeFormatterFractionalSeconds = ISODateTimeFormat.dateTime();

   private final Double longitude;
   private final Double latitude;
   private final String timestamp;
   private final Double elevation;

   /**
    * Creates a TrackPoint representing the given values.  Empty strings for any of the values will be converted to
    * null values.
    */
   public TrackPoint(final String longitude, final String latitude, final String timestamp, final String elevation)
      {
      this.longitude = convertStringToDouble(longitude);
      this.latitude = convertStringToDouble(latitude);
      this.timestamp = "".equals(timestamp) ? null : timestamp;
      this.elevation = convertStringToDouble(elevation);
      }

   /**
    * Copy constructor, but with a different elevation.
    */
   public TrackPoint(final TrackPoint trackPoint, final Double elevation)
      {
      this.longitude = trackPoint.longitude;
      this.latitude = trackPoint.latitude;
      this.timestamp = trackPoint.timestamp;
      this.elevation = elevation;
      }

   private Double convertStringToDouble(final String str)
      {
      if (str != null && !"".equals(str))
         {
         try
            {
            return Double.parseDouble(str);
            }
         catch (NumberFormatException e)
            {
            if (LOG.isErrorEnabled())
               {
               LOG.error("NumberFormatException while trying to parse string [" + str + "] as a double", e);
               }
            }
         }
      return null;
      }

   public Double getLongitude()
      {
      return longitude;
      }

   public Double getLatitude()
      {
      return latitude;
      }

   public String getTimestamp()
      {
      return timestamp;
      }

   public Date getTimestampAsDate()
      {
      if (timestamp != null)
         {
         // Parse the timestamp, first trying the fractional second parser, then fall back to the non-fractional
         // second parser if the first fails.
         DateTime dateTime = null;
         try
            {
            dateTime = isoDateTimeFormatterFractionalSeconds.parseDateTime(timestamp);
            }
         catch (Exception e)
            {
            try
               {
               dateTime = isoDateTimeFormatter.parseDateTime(timestamp);
               }
            catch (Exception e1)
               {
               LOG.error("Exception while parsing the timestamp [" + timestamp + "]", e);
               }
            }

         if (dateTime != null)
            {
            return dateTime.toDate();
            }
         }

      return null;
      }

   public Double getElevation()
      {
      return elevation;
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

      final TrackPoint that = (TrackPoint)o;

      if (elevation != null ? !elevation.equals(that.elevation) : that.elevation != null)
         {
         return false;
         }
      if (latitude != null ? !latitude.equals(that.latitude) : that.latitude != null)
         {
         return false;
         }
      if (longitude != null ? !longitude.equals(that.longitude) : that.longitude != null)
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
      int result = longitude != null ? longitude.hashCode() : 0;
      result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
      result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
      result = 31 * result + (elevation != null ? elevation.hashCode() : 0);
      return result;
      }

   @Override
   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("TrackPoint");
      sb.append("{longitude=").append(longitude);
      sb.append(", latitude=").append(latitude);
      sb.append(", timestamp='").append(timestamp).append('\'');
      sb.append(", elevation=").append(elevation);
      sb.append('}');
      return sb.toString();
      }

   public Element toElement()
      {
      final Element element = new Element(GPXFile.TRACK_POINT_ELEMENT_NAME, GPXFile.GPX_NAMESPACE);
      element.setAttribute(GPXFile.LATITUDE_ATTR, String.valueOf(latitude));
      element.setAttribute(GPXFile.LONGITUDE_ATTR, String.valueOf(longitude));
      final Element elevationElement = new Element(GPXFile.ELEVATION_ELEMENT_NAME, GPXFile.GPX_NAMESPACE);
      elevationElement.addContent(elevation == null ? "0" : String.valueOf(elevation));
      final Element timeElement = new Element(GPXFile.TIME_ELEMENT_NAME, GPXFile.GPX_NAMESPACE);
      timeElement.addContent(timestamp);
      element.addContent(elevationElement);
      element.addContent(timeElement);
      return element;
      }
   }
