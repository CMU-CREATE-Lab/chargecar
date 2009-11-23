package org.chargecar.gpx;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface DistanceCalculator
   {
   /**
    * Returns the distance between the given {@link TrackPoint}s in meters.  Returns 0 if either (or both)
    * {@link TrackPoint} is <code>null</code>.
    */
   double compute2DDistance(final TrackPoint t1, final TrackPoint t2);
   }