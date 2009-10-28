package org.chargecar.gpx;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface DistanceCalculator
   {
   Double compute2DDistance(final TrackPoint t1, final TrackPoint t2);
   }