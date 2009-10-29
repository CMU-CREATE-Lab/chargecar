package org.chargecar;

import java.io.File;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.gpx.DistanceCalculator;
import org.chargecar.gpx.GPXEventHandlerAdapter;
import org.chargecar.gpx.GPXReader;
import org.chargecar.gpx.MinMaxLatLongCalculator;
import org.chargecar.gpx.SphericalLawOfCosinesDistanceCalculator;
import org.chargecar.gpx.TrackPoint;
import org.chargecar.gpx.UTCHelper;
import org.chargecar.ned.ElevationDataset;
import org.chargecar.ned.ElevationDatasetException;
import org.chargecar.ned.gridfloat.GridFloatDataset;

/**
 * <p>
 * <code>SpeedCalculator</code> reads a given GPX file prints out speed and distances over time.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class SpeedCalculator
   {
   private static final Log LOG = LogFactory.getLog(SpeedCalculator.class);

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
         LOG.debug("Converting elevations for GPX [" + gpxFile.getAbsolutePath() + "]...");
         }

      try
         {
         // create the GPX reader
         final GPXReader gpxReader = new GPXReader(gpxFile);

         // add the event handler which computes the lat/long ranges
         final MinMaxLatLongCalculator minMaxLatLongCalculator = new MinMaxLatLongCalculator();
         gpxReader.addGPXEventHandler(minMaxLatLongCalculator);

         // read the GPX so we can get the lat/long ranges
         gpxReader.read();

         // remove the event handler
         gpxReader.removeEventHandlers();

         // create the elevation dataset
         ElevationDataset elevationDataset = null;
         try
            {
            elevationDataset = new GridFloatDataset(minMaxLatLongCalculator.getMinLongitude(),
                                                    minMaxLatLongCalculator.getMaxLongitude(),
                                                    minMaxLatLongCalculator.getMinLatitude(),
                                                    minMaxLatLongCalculator.getMaxLatitude());
            }
         catch (ElevationDatasetException e)
            {
            LOG.error("ElevationDatasetException while trying to create the ElevationDataset", e);
            }

         // add the event handler which will produce the new GPX
         gpxReader.addGPXEventHandler(new SpeedPrinter(elevationDataset));

         // read the GPX so we can get the lat/long ranges
         gpxReader.read();

         final long endTime = System.currentTimeMillis();
         if (LOG.isDebugEnabled())
            {
            LOG.debug("Total time (ms) to compute [" + gpxFile.getAbsolutePath() + "]: " + (endTime - startTime));
            }
         }
      catch (Exception e)
         {
         if (LOG.isErrorEnabled())
            {
            LOG.error("Exception caught while processing GPX [" + gpxFile.getAbsolutePath() + "]", e);
            }
         System.exit(1);
         }
      }

   private static class SpeedPrinter extends GPXEventHandlerAdapter
      {
      private static final double MILES_PER_KM = 0.621371192;

      private final ElevationDataset elevationDataset;
      private TrackPoint previousTrackPoint = null;
      private final DistanceCalculator distanceCalculator1 = SphericalLawOfCosinesDistanceCalculator.getInstance();
      private double runningSumDistanceInMiles = 0;

      private SpeedPrinter(final ElevationDataset elevationDataset) throws ElevationDatasetException
         {

         this.elevationDataset = elevationDataset;
         if (elevationDataset != null)
            {
            this.elevationDataset.open();
            }
         }

      public void handleTrackPoint(final TrackPoint trackPoint)
         {
         final Double longitude = trackPoint.getLongitude();
         final Double latitude = trackPoint.getLatitude();
         final Double elevation = (longitude != null && latitude != null && elevationDataset != null) ? elevationDataset.getElevation(longitude, latitude) : null;

         final Double distanceInKilometers = distanceCalculator1.compute2DDistance(trackPoint, previousTrackPoint);
         final double elapsedSeconds;
         final Double distanceInMiles;
         final Double milesPerHour;
         if (distanceInKilometers != null)
            {
            elapsedSeconds = (trackPoint.getTimestampAsDate().getTime() -
                              previousTrackPoint.getTimestampAsDate().getTime()) / 1000.0;

            distanceInMiles = distanceInKilometers * MILES_PER_KM;
            runningSumDistanceInMiles += distanceInMiles;
            milesPerHour = distanceInMiles / elapsedSeconds * 3600.0;
            }
         else
            {
            elapsedSeconds = 0.0;
            distanceInMiles = 0.0;
            milesPerHour = 0.0;
            }

         System.out.printf("%s\t%5f\t%10.15f\t%10.15f\t%10.15f\t%10.15f\t%10.15f\t%10.15f\t%10.15f\n",
                           trackPoint.getTimestampAsDateTime().toString(UTCHelper.ISO_DATE_TIME_FORMATTER_FRACTIONAL_SECONDS),
                           elapsedSeconds,
                           longitude,
                           latitude,
                           trackPoint.getElevation(),
                           elevation,
                           distanceInMiles,
                           runningSumDistanceInMiles,
                           milesPerHour);

         previousTrackPoint = trackPoint;
         }

      public void handleGPXEnd()
         {
         if (elevationDataset != null)
            {
            elevationDataset.close();
            }
         }
      }

   private SpeedCalculator()
      {
      // private to prevent instantiation
      }
   }