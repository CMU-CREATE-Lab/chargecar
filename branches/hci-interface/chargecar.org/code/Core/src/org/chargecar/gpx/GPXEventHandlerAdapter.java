package org.chargecar.gpx;

/**
 * <p>
 * <code>GPXEventHandlerAdapter</code> is an abstract, no-op {@link GPXEventHandler} which enables subclasses to choose
 * which methods to implement instead of having to implement the entire {@link GPXEventHandler} interface.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class GPXEventHandlerAdapter implements GPXEventHandler
   {
   public void handleGPXBegin(final String gpxCreator)
      {
      // do nothing
      }

   public void handleTrackBegin(final String trackName)
      {
      // do nothing
      }

   public void handleTrackSegmentBegin()
      {
      // do nothing
      }

   public void handleTrackPoint(final TrackPoint trackPoint)
      {
      // do nothing
      }

   public void handleTrackSegmentEnd()
      {
      // do nothing
      }

   public void handleTrackEnd(final String trackName)
      {
      // do nothing
      }

   public void handleGPXEnd()
      {
      // do nothing
      }
   }
