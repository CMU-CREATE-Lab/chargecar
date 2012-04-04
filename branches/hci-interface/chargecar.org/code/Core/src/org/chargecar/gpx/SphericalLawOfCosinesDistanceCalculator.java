package org.chargecar.gpx;

/**
 * <p>
 * <code>SphericalLawOfCosinesDistanceCalculator</code> helps compute the distance between two {@link GPSCoordinate}s
 * using the spherical law of cosines.  See
 * <a href="http://www.movable-type.co.uk/scripts/latlong.html">http://www.movable-type.co.uk/scripts/latlong.html</a>
 * for more info.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class SphericalLawOfCosinesDistanceCalculator extends AbstractDistanceCalculator
   {
   private static final DistanceCalculator INSTANCE = new SphericalLawOfCosinesDistanceCalculator();

   public static DistanceCalculator getInstance()
      {
      return INSTANCE;
      }

   private SphericalLawOfCosinesDistanceCalculator()
      {
      // private to prevent instantiation
      }

   public double compute2DDistance(final GPSCoordinate c1, final GPSCoordinate c2)
      {
      if (c1 != null && c2 != null && !c1.isNull() && !c2.isNull())
         {
         final double t1LatitudeRadians = Math.toRadians(c1.getLatitude());
         final double t2LatitudeRadians = Math.toRadians(c2.getLatitude());
         return Math.acos(Math.sin(t1LatitudeRadians) * Math.sin(t2LatitudeRadians) +
                          Math.cos(t1LatitudeRadians) * Math.cos(t2LatitudeRadians) *
                          Math.cos(Math.toRadians(c2.getLongitude() - c1.getLongitude()))) * RADIUS_OF_EARTH_IN_METERS;
         }
      return 0.0;
      }
   }
