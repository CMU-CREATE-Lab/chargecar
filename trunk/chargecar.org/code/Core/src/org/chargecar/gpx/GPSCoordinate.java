package org.chargecar.gpx;

import edu.cmu.ri.createlab.util.StringUtils;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class GPSCoordinate
   {
   private final Double longitude;
   private final Double latitude;

   /**
    * Creates a GPSCoordinate representing the given values.  Empty strings for any of the values will be converted to
    * <code>null</code> values.
    */
   public GPSCoordinate(final String longitude, final String latitude)
      {
      this.longitude = StringUtils.convertStringToDouble(longitude);
      this.latitude = StringUtils.convertStringToDouble(latitude);
      }

   /** Creates a GPSCoordinate representing the given values. */
   public GPSCoordinate(final Double longitude, final Double latitude)
      {
      this.longitude = longitude;
      this.latitude = latitude;
      }

   public Double getLongitude()
      {
      return longitude;
      }

   public Double getLatitude()
      {
      return latitude;
      }

   /**
    * Returns <code>true</code> if either (or both) the latitude or longitude is <code>null</code>, <code>false</code>
    * otherwise.
    */
   public boolean isNull()
      {
      return latitude == null || longitude == null;
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

      final GPSCoordinate that = (GPSCoordinate)o;

      if (latitude != null ? !latitude.equals(that.latitude) : that.latitude != null)
         {
         return false;
         }
      if (longitude != null ? !longitude.equals(that.longitude) : that.longitude != null)
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
      return result;
      }

   @Override
   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("GPSCoordinate");
      sb.append("{longitude=").append(longitude);
      sb.append(", latitude=").append(latitude);
      sb.append('}');
      return sb.toString();
      }
   }
