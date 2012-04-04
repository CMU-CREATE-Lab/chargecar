package org.chargecar;

import java.io.File;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.chargecar.gpx.DistanceCalculator;
import org.chargecar.gpx.GPXEventHandlerAdapter;
import org.chargecar.gpx.GPXReader;
import org.chargecar.gpx.SphericalLawOfCosinesDistanceCalculator;
import org.chargecar.gpx.TrackPoint;
import org.chargecar.gpx.UTCHelper;

/**
 * <p>
 * <code>SpeedCalculator</code> reads a given GPX file and prints out speed and distances over time.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class SpeedCalculator
   {
   private static final Logger LOG = Logger.getLogger(SpeedCalculator.class);

   public static void main(final String[] args)
      {
      final long startTime = System.currentTimeMillis();

      if (args.length < 1)
         {
         LOG.error("GPX file not specified.  Aborting.");
         System.exit(1);
         }

      final File gpxFile = new File(args[args.length - 1]);

      if (LOG.isDebugEnabled())
         {
         LOG.debug("Calculating speeds for GPX [" + gpxFile.getAbsolutePath() + "]...");
         }

      try
         {
         // create the GPX reader
         final GPXReader gpxReader = new GPXReader(gpxFile);

         // add the event handler which will print the speeds
         gpxReader.addGPXEventHandler(new SpeedPrinter());

         // read the GPX
         gpxReader.read();

         final long endTime = System.currentTimeMillis();
         if (LOG.isDebugEnabled())
            {
            LOG.debug("Total time (ms) to compute [" + gpxFile.getAbsolutePath() + "]: " + (endTime - startTime));
            }
         }
      catch (Exception e)
         {
         if (LOG.isEnabledFor(Level.ERROR))
            {
            LOG.error("Exception caught while processing GPX [" + gpxFile.getAbsolutePath() + "]", e);
            }
         System.exit(1);
         }
      }

   private static class SpeedPrinter extends GPXEventHandlerAdapter
      {
      private static final double MILES_PER_KM = 0.621371192;

      private TrackPoint previousTrackPoint = null;
      private final DistanceCalculator distanceCalculator = SphericalLawOfCosinesDistanceCalculator.getInstance();
      private double runningSumDistanceInMiles = 0;

      private SpeedPrinter()
         {
         // nothing to do
         }

      public void handleTrackPoint(final TrackPoint trackPoint)
         {
         final Double longitude = trackPoint.getLongitude();
         final Double latitude = trackPoint.getLatitude();

         final double distanceInMeters = distanceCalculator.compute2DDistance(trackPoint, previousTrackPoint);
         final double distanceInKilometers = distanceInMeters / 1000;
         final double elapsedSeconds = previousTrackPoint == null ? 0.0 : (trackPoint.getTimestampAsDate().getTime() - previousTrackPoint.getTimestampAsDate().getTime()) / 1000.0;
         final double distanceInMiles;
         final double milesPerHour;
         if (Double.compare(distanceInKilometers, 0) == 0)
            {
            distanceInMiles = 0.0;
            milesPerHour = 0.0;
            }
         else
            {
            distanceInMiles = distanceInKilometers * MILES_PER_KM;
            runningSumDistanceInMiles += distanceInMiles;
            milesPerHour = distanceInMiles / elapsedSeconds * 3600.0;
            }

         System.out.printf("%s\t%5f\t%10.15f\t%10.15f\t%10.15f\t%10.15f\t%10.15f\t%10.15f\n",
                           trackPoint.getTimestampAsDateTime().toString(UTCHelper.ISO_DATE_TIME_FORMATTER_FRACTIONAL_SECONDS),
                           elapsedSeconds,
                           longitude,
                           latitude,
                           trackPoint.getElevation(),
                           distanceInMiles,
                           runningSumDistanceInMiles,
                           milesPerHour);

         previousTrackPoint = trackPoint;
         }
      }

   private SpeedCalculator()
      {
      // private to prevent instantiation
      }
   }