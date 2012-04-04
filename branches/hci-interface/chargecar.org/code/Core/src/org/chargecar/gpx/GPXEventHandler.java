package org.chargecar.gpx;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface GPXEventHandler
   {
   void handleGPXBegin(final String gpxCreator);

   void handleTrackBegin(final String trackName);

   void handleTrackSegmentBegin();

   void handleTrackPoint(final TrackPoint trackPoint);

   void handleTrackSegmentEnd();

   void handleTrackEnd(final String trackName);

   void handleGPXEnd();
   }