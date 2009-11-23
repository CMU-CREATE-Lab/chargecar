package org.chargecar.gpx;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class AbstractDistanceCalculator implements DistanceCalculator
   {
   public static final double RADIUS_OF_EARTH_IN_METERS = 6371000;

   public abstract double compute2DDistance(final TrackPoint t1, final TrackPoint t2);
   }