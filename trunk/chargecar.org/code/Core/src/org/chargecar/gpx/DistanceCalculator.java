package org.chargecar.gpx;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface DistanceCalculator
   {
   /**
    * Returns the distance between the given {@link TrackPoint}s in meters.
    */
   Double compute2DDistance(final TrackPoint t1, final TrackPoint t2);
   }