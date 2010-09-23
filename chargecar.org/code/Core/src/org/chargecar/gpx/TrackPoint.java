package org.chargecar.gpx;

import java.util.Date;
import edu.cmu.ri.createlab.util.StringUtils;
import org.jdom.Element;
import org.joda.time.DateTime;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class TrackPoint extends GPSCoordinate
   {
   private final String timestamp;
   private final Double elevation;

   /**
    * Creates a TrackPoint representing the given values.  Empty strings for any of the values will be converted to
    * <code>null</code> values.
    */
   public TrackPoint(final String longitude, final String latitude, final String timestamp, final String elevation)
   {
   super(longitude, latitude);
   this.timestamp = "".equals(timestamp) ? null : timestamp;
   this.elevation = StringUtils.convertStringToDouble(elevation);
   }

   /**
    * Copy constructor, but with a different elevation.
    */
   public TrackPoint(final TrackPoint trackPoint, final Double elevation)
   {
   super(trackPoint.getLongitude(), trackPoint.getLatitude());
   this.timestamp = trackPoint.timestamp;
   this.elevation = elevation;
   }

   public String getTimestamp()
      {
      return timestamp;
      }

   public Date getTimestampAsDate()
      {
      return UTCHelper.getUTCTimestampAsDate(timestamp);
      }

   public DateTime getTimestampAsDateTime()
      {
      return UTCHelper.getUTCTimestampAsDateTime(timestamp);
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
      if (!super.equals(o))
         {
         return false;
         }

      final TrackPoint that = (TrackPoint)o;

      if (elevation != null ? !elevation.equals(that.elevation) : that.elevation != null)
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
      int result = super.hashCode();
      result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
      result = 31 * result + (elevation != null ? elevation.hashCode() : 0);
      return result;
      }

   public Element toElement()
      {
      final Element element = new Element(GPXFile.TRACK_POINT_ELEMENT_NAME, GPXFile.GPX_NAMESPACE);
      element.setAttribute(GPXFile.LATITUDE_ATTR, String.valueOf(getLatitude()));
      element.setAttribute(GPXFile.LONGITUDE_ATTR, String.valueOf(getLongitude()));
      final Element elevationElement = new Element(GPXFile.ELEVATION_ELEMENT_NAME, GPXFile.GPX_NAMESPACE);
      elevationElement.addContent(elevation == null ? "0" : String.valueOf(elevation));
      final Element timeElement = new Element(GPXFile.TIME_ELEMENT_NAME, GPXFile.GPX_NAMESPACE);
      timeElement.addContent(timestamp);
      element.addContent(elevationElement);
      element.addContent(timeElement);
      return element;
      }
   }
