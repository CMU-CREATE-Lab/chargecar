package org.chargecar.gpx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * <p>
 * <code>GPXPrivatizer</code> is a singleton class which takes a GPX file and, for each track in the GPX, privatizes it
 * and then returns the new GPX.  Note that privatization may not preserve the original GPX's track segments or any
 * track segment data other than trackpoints, elevations, and timestamps.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GPXPrivatizer
   {
   /** Length (in meters) to remove from the beginning or end of the track. */
   public static final double DEFAULT_PRIVATIZATION_RADIUS_IN_METERS = 160.9344; // one-tenth of a mile

   private static final GPXPrivatizer INSTANCE = new GPXPrivatizer();

   public static GPXPrivatizer getInstance()
      {
      return INSTANCE;
      }

   /**
    * Privatizes the given GPX by removing the default length from both the beginning and the end of the GPX.  The new
    * GPX is returned and the original is never modified.
    *
    * @see #DEFAULT_PRIVATIZATION_RADIUS_IN_METERS
    */
   public Element privatize(final Element gpx) throws IOException, JDOMException
      {
      return privatize(gpx, DEFAULT_PRIVATIZATION_RADIUS_IN_METERS, DEFAULT_PRIVATIZATION_RADIUS_IN_METERS);
      }

   /**
    * Privatizes the given GPX by removing the default length from both the beginning and the end of the GPX.  The new
    * GPX is returned and the original is never modified.
    *
    * @see #DEFAULT_PRIVATIZATION_RADIUS_IN_METERS
    */
   public Element privatize(final Element gpx, final GPSCoordinate location) throws IOException, JDOMException
      {
      return privatize(gpx, location, DEFAULT_PRIVATIZATION_RADIUS_IN_METERS);
      }

   /**
    * Privatizes the given GPX by removing the given lengths from the beginning and the end of the GPX.  The new GPX is
    * returned and the original is never modified.
    *
    * @throws IllegalArgumentException if either the <code>numMetersToRemoveFromBeginning</code> or
    * <code>numMetersToRemoveFromEnd</code> is negative.
    */
   public Element privatize(final Element gpx, final double numMetersToRemoveFromBeginning, final double numMetersToRemoveFromEnd) throws IOException, JDOMException
      {
      if (Double.compare(numMetersToRemoveFromBeginning, 0) < 0 || Double.compare(numMetersToRemoveFromEnd, 0) < 0)
         {
         throw new IllegalArgumentException("Number of meters to remove must be non-negative");
         }
      else
         {
         final GPXReader gpxReader = new GPXReader(gpx);
         final BeginningAndEndPrivatizerGPXEventHandlerAdapter gpxEventHandler = new BeginningAndEndPrivatizerGPXEventHandlerAdapter(numMetersToRemoveFromBeginning, numMetersToRemoveFromEnd);
         gpxReader.addGPXEventHandler(gpxEventHandler);

         // read, then get the new GPX
         gpxReader.read();

         return gpxEventHandler.getElement();
         }
      }

   /**
    * Privatizes the given GPX by removing the given lengths from the beginning and the end of the GPX.  The new GPX is
    * returned and the original is never modified.
    *
    * @throws IllegalArgumentException if the <code>location</code> is <code>null</code>, the
    * <code>location</code>'s latitude or longitude is null, or <code>privatizationRadiusInMeters</code> is
    * negative.
    */
   public Element privatize(final Element gpx, final GPSCoordinate location, final double privatizationRadiusInMeters) throws IOException, JDOMException
      {
      if (Double.compare(privatizationRadiusInMeters, 0) < 0)
         {
         throw new IllegalArgumentException("Privatization radius in meters must be non-negative");
         }
      else if (location == null || location.isNull())
         {
         throw new IllegalArgumentException("The GPSCoordinate and its latitude and longitude must not be null");
         }
      else
         {
         final GPXReader gpxReader = new GPXReader(gpx);
         final LocationPrivatizerGPXEventHandlerAdapter gpxEventHandler = new LocationPrivatizerGPXEventHandlerAdapter(location, privatizationRadiusInMeters);
         gpxReader.addGPXEventHandler(gpxEventHandler);

         // read, then get the new GPX
         gpxReader.read();

         return gpxEventHandler.getElement();
         }
      }

   private GPXPrivatizer()
      {
      // private to prevent instantiation
      }

   private static class BeginningAndEndPrivatizerGPXEventHandlerAdapter extends GPXEventHandlerAdapter
      {
      private final double numMetersToRemoveFromBeginning;
      private final double numMetersToRemoveFromEnd;
      private final List<TrackPoint> trackPoints = new ArrayList<TrackPoint>();
      private final GPXFile gpxFile = new GPXFile();
      private Element currentTrack;

      private BeginningAndEndPrivatizerGPXEventHandlerAdapter(final double numMetersToRemoveFromBeginning, final double numMetersToRemoveFromEnd)
         {
         this.numMetersToRemoveFromBeginning = numMetersToRemoveFromBeginning;
         this.numMetersToRemoveFromEnd = numMetersToRemoveFromEnd;
         }

      public void handleGPXBegin(final String gpxCreator)
         {
         gpxFile.setCreator(GPXPrivatizer.class.getName() + "(" + gpxCreator + ")");
         }

      public void handleTrackBegin(final String trackName)
         {
         currentTrack = gpxFile.createTrack(trackName);
         }

      public void handleTrackPoint(final TrackPoint trackPoint)
         {
         trackPoints.add(trackPoint);
         }

      public void handleTrackEnd(final String trackName)
         {
         final DistanceCalculator distanceCalculator = SphericalLawOfCosinesDistanceCalculator.getInstance();

         // run through the list of track points and trim off the requested lengths from the beginning
         if (Double.compare(numMetersToRemoveFromBeginning, 0) > 0)
            {
            double runningSumDistanceInMeters = 0.0;
            int i = 0;
            while (i < trackPoints.size() - 1 && Double.compare(runningSumDistanceInMeters, numMetersToRemoveFromBeginning) < 0)
               {
               runningSumDistanceInMeters += distanceCalculator.compute2DDistance(trackPoints.get(i), trackPoints.get(i + 1));
               i++;
               }

            // chop off the points
            trackPoints.subList(0, Math.min(i, trackPoints.size())).clear();
            }

         if (Double.compare(numMetersToRemoveFromEnd, 0) > 0)
            {
            double runningSumDistanceInMeters = 0.0;
            int i = trackPoints.size() - 1;
            while (i > 1 && Double.compare(runningSumDistanceInMeters, numMetersToRemoveFromEnd) < 0)
               {
               runningSumDistanceInMeters += distanceCalculator.compute2DDistance(trackPoints.get(i - 1), trackPoints.get(i));
               i--;
               }

            // chop off the points
            trackPoints.subList(Math.max(i, 0), trackPoints.size()).clear();
            }

         // add the track points to the track
         final Element trackSegment = gpxFile.createTrackSegment(currentTrack);
         for (final TrackPoint trackPoint : trackPoints)
            {
            gpxFile.createTrackPoint(trackSegment, trackPoint);
            }

         // clear the collection of track points
         trackPoints.clear();
         }

      public Element getElement()
         {
         return gpxFile.toElement();
         }
      }

   private static class LocationPrivatizerGPXEventHandlerAdapter extends GPXEventHandlerAdapter
      {
      private final GPSCoordinate location;
      private final double privatizationRadiusInMeters;
      private final GPXFile gpxFile = new GPXFile();
      private Element currentTrack;
      private Element currentTrackSegment;
      private final DistanceCalculator distanceCalculator = SphericalLawOfCosinesDistanceCalculator.getInstance();

      private LocationPrivatizerGPXEventHandlerAdapter(final GPSCoordinate location, final double privatizationRadiusInMeters)
         {
         this.location = location;
         this.privatizationRadiusInMeters = privatizationRadiusInMeters;
         }

      @Override
      public void handleGPXBegin(final String gpxCreator)
         {
         gpxFile.setCreator(GPXPrivatizer.class.getName() + "(" + gpxCreator + ")");
         }

      @Override
      public void handleTrackBegin(final String trackName)
         {
         currentTrack = gpxFile.createTrack(trackName);
         }

      @Override
      public void handleTrackSegmentBegin()
         {
         currentTrackSegment = gpxFile.createTrackSegment(currentTrack);
         }

      @Override
      public void handleTrackPoint(final TrackPoint trackPoint)
         {
         // compute the distance between the given track point and the one we're privatizing
         final double distanceInMeters = distanceCalculator.compute2DDistance(trackPoint, location);

         // only keep the given track point if it falls outside the privatization radius
         if (Double.compare(distanceInMeters, privatizationRadiusInMeters) > 0)
            {
            gpxFile.createTrackPoint(currentTrackSegment, trackPoint);
            }
         }

      public Element getElement()
         {
         return gpxFile.toElement();
         }
      }
   }
