package org.chargecar;

import java.io.File;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.gpx.GPXEventHandlerAdapter;
import org.chargecar.gpx.GPXFile;
import org.chargecar.gpx.GPXReader;
import org.chargecar.gpx.MinMaxLatLongCalculator;
import org.chargecar.gpx.TrackPoint;
import org.chargecar.ned.ElevationDataset;
import org.chargecar.ned.ElevationDatasetException;
import org.chargecar.ned.USGSWebServiceElevationDataset;
import org.chargecar.ned.gridfloat.GridFloatDataset;
import org.chargecar.xml.XmlHelper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * <p>
 * <code>GPXElevationConverter</code> reads a given GPX file and prints a new one to stdout with USGS elevations.  The
 * USGS elevations are obtained from the local GridFloat dataset unless the "--usgs" switch is supplied on the command
 * line, which causes the program to obtain the elevations from the USGS web service.  Fetches from the USGS web service
 * are throttled to occur at 50 millisecond intervals.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class GPXElevationConverter
   {
   private static final Log LOG = LogFactory.getLog(GPXElevationConverter.class);
   private static final String USGS_COMMAND_LINE_SWITCH = "--usgs";

   public static void main(final String[] args) throws IOException, JDOMException, ElevationDatasetException
      {
      final long startTime = System.currentTimeMillis();

      if (args.length < 1)
         {
         LOG.error("GPX file not specified.  Aborting.");
         System.exit(1);
         }

      final File gpxFile = new File(args[args.length - 1]);
      final boolean willQueryUSGSWebService = args.length > 1 && USGS_COMMAND_LINE_SWITCH.equals(args[0]);

      if (LOG.isDebugEnabled())
         {
         LOG.debug("Converting elevations for GPX [" + gpxFile.getAbsolutePath() + "]...");
         }

      // create the GPX reader
      final GPXReader gpxReader = new GPXReader(gpxFile);

      // add the event handler which computes the lat/long ranges
      final MinMaxLatLongCalculator minMaxLatLongCalculator = new MinMaxLatLongCalculator();
      gpxReader.addGPXEventHandler(minMaxLatLongCalculator);

      // read the GPX so we can get the lat/long ranges
      gpxReader.read();

      // remove the event handler
      gpxReader.removeEventHandlers();

      // add the event handler which will produce the new GPX
      final ElevationDataset elevationDataset;
      if (willQueryUSGSWebService)
         {
         elevationDataset = new ElevationDatasetThrottle(USGSWebServiceElevationDataset.getInstance());
         }
      else
         {
         elevationDataset = new GridFloatDataset(minMaxLatLongCalculator.getMinLongitude(),
                                                 minMaxLatLongCalculator.getMaxLongitude(),
                                                 minMaxLatLongCalculator.getMinLatitude(),
                                                 minMaxLatLongCalculator.getMaxLatitude());
         }
      gpxReader.addGPXEventHandler(new GPXPrinter(elevationDataset));

      // read the GPX so we can get the lat/long ranges
      gpxReader.read();

      final long endTime = System.currentTimeMillis();
      if (LOG.isDebugEnabled())
         {
         LOG.debug("Total time (ms) to convert elevations for GPX [" + gpxFile.getAbsolutePath() + "]: " + (endTime - startTime));
         }
      }

   private static class GPXPrinter extends GPXEventHandlerAdapter
      {
      private final ElevationDataset elevationDataset;
      private final GPXFile gpxFile = new GPXFile();
      private Element currentTrack;
      private Element currentTrackSegment;

      private GPXPrinter(final ElevationDataset elevationDataset) throws ElevationDatasetException
         {
         this.elevationDataset = elevationDataset;
         this.elevationDataset.open();
         }

      public void handleTrackBegin(final String trackName)
         {
         currentTrack = gpxFile.createTrack(trackName);
         }

      public void handleTrackSegmentBegin()
         {
         currentTrackSegment = gpxFile.createTrackSegment(currentTrack);
         }

      public void handleTrackPoint(final TrackPoint trackPoint)
         {
         final Double longitude = trackPoint.getLongitude();
         final Double latitude = trackPoint.getLatitude();
         final Double elevation = (longitude != null && latitude != null) ? elevationDataset.getElevation(longitude, latitude) : null;

         //System.out.printf("%10.15f\t%10.15f\t%10.15f\t%10.15f\n", longitude, latitude, trackPoint.getElevation(), elevation);
         gpxFile.createTrackPoint(currentTrackSegment, new TrackPoint(trackPoint, elevation));
         }

      public void handleGPXEnd()
         {
         elevationDataset.close();
         System.out.println(XmlHelper.writeDocumentToStringFormatted(new Document(gpxFile.toElement())));
         }
      }

   private static class ElevationDatasetThrottle implements ElevationDataset
      {
      private final ElevationDataset elevationDataset;

      private ElevationDatasetThrottle(final ElevationDataset elevationDataset)
         {
         this.elevationDataset = elevationDataset;
         }

      public void open() throws ElevationDatasetException
         {
         elevationDataset.open();
         }

      public Double getElevation(final double longitude, final double latitude)
         {
         final Double elevation = elevationDataset.getElevation(longitude, latitude);
         try
            {
            Thread.sleep(50);
            }
         catch (InterruptedException e)
            {
            LOG.error("InterruptedException while sleeping", e);
            }
         return elevation;
         }

      public void close()
         {
         elevationDataset.close();
         }
      }

   private GPXElevationConverter()
      {
      // private to prevent instantiation
      }
   }