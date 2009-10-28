package org.chargecar.gpx;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class AbstractDistanceCalculator implements DistanceCalculator
   {
   public static final double RADIUS_OF_EARTH_IN_KM = 6371;

   public abstract Double compute2DDistance(final TrackPoint t1, final TrackPoint t2);
   }