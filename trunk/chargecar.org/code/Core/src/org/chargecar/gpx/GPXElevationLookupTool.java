package org.chargecar.gpx;

import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.chargecar.ned.ElevationDataset;
import org.chargecar.ned.ElevationDatasetException;
import org.chargecar.ned.USGSWebServiceElevationDataset;
import org.chargecar.ned.gridfloat.GridFloatDataset;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * <p>
 * <code>GPXElevationLookupTool</code> is a singleton class which takes a GPX file and attempts to replace the elevations
 * with values obtained from online data.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GPXElevationLookupTool
   {
   private static final Logger LOG = Logger.getLogger(GPXElevationLookupTool.class);

   private static final GPXElevationLookupTool INSTANCE = new GPXElevationLookupTool();

   public static GPXElevationLookupTool getInstance()
      {
      return INSTANCE;
      }

   /**
    * Returns a new GPX contain elevations obtained from local data.  If an elevation cannot be obtained, the original
    * elevation in the GPX is used.
    */
   public Element convertElevationsUsingLocalData(final File gpx) throws IOException, JDOMException, ElevationDatasetException
   {
   return convertElevationsUsingLocalData(new GPXReader(gpx));
   }

   /**
    * Returns a new GPX contain elevations obtained from local data.  If an elevation cannot be obtained, the original
    * elevation in the GPX is used.
    */
   public Element convertElevationsUsingLocalData(final Element gpx) throws IOException, JDOMException, ElevationDatasetException
   {
   return convertElevationsUsingLocalData(new GPXReader(gpx));
   }

   /**
    * Returns a new GPX contain elevations obtained from online data.  Request are throttled to occur at 50 ms
    * intervals.  If an elevation cannot be obtained, the original elevation in the GPX is used.
    */
   public Element convertElevationsUsingOnlineData(final File gpx) throws IOException, JDOMException, ElevationDatasetException
   {
   return convertElevationsUsingOnlineData(new GPXReader(gpx));
   }

   /**
    * Returns a new GPX contain elevations obtained from online data.  Request are throttled to occur at 50 ms
    * intervals.  If an elevation cannot be obtained, the original elevation in the GPX is used.
    */
   public Element convertElevationsUsingOnlineData(final Element gpx) throws IOException, JDOMException, ElevationDatasetException
   {
   return convertElevationsUsingOnlineData(new GPXReader(gpx));
   }

   /**
    * Returns a new GPX contain elevations obtained from local data.  If an elevation cannot be obtained, the original
    * elevation in the GPX is used.
    */
   private Element convertElevationsUsingLocalData(final GPXReader gpxReader) throws IOException, JDOMException, ElevationDatasetException
   {
   // add the event handler which computes the lat/long ranges
   final MinMaxLatLongCalculator minMaxLatLongCalculator = new MinMaxLatLongCalculator();
   gpxReader.addGPXEventHandler(minMaxLatLongCalculator);

   // read the GPX so we can get the lat/long ranges
   gpxReader.read();

   final ElevationDataset elevationDataset = new GridFloatDataset(minMaxLatLongCalculator.getMinLongitude(),
                                                                  minMaxLatLongCalculator.getMaxLongitude(),
                                                                  minMaxLatLongCalculator.getMinLatitude(),
                                                                  minMaxLatLongCalculator.getMaxLatitude());
   return convertElevations(gpxReader, elevationDataset);
   }

   private Element convertElevationsUsingOnlineData(final GPXReader gpxReader) throws IOException, JDOMException, ElevationDatasetException
      {
      return convertElevations(gpxReader, new ElevationDatasetThrottle(USGSWebServiceElevationDataset.getInstance()));
      }

   private Element convertElevations(final GPXReader gpxReader, final ElevationDataset elevationDataset) throws IOException, JDOMException, ElevationDatasetException
      {
      // add the event handler which will do the elevation lookups and produce the new GPX
      final GPXElevationCalculatorEventHandlerAdapter gpxEventHandler = new GPXElevationCalculatorEventHandlerAdapter(elevationDataset);
      gpxReader.addGPXEventHandler(gpxEventHandler);

      gpxReader.read();

      return gpxEventHandler.getElement();
      }

   private GPXElevationLookupTool()
      {
      // private to prevent instantiation
      }

   private static class GPXElevationCalculatorEventHandlerAdapter extends GPXEventHandlerAdapter
      {
      private final ElevationDataset elevationDataset;
      private final GPXFile gpxFile = new GPXFile();
      private Element currentTrack;
      private Element currentTrackSegment;

      private GPXElevationCalculatorEventHandlerAdapter(final ElevationDataset elevationDataset) throws ElevationDatasetException
         {
         this.elevationDataset = elevationDataset;
         this.elevationDataset.open();
         }

      public void handleGPXBegin(final String gpxCreator)
         {
         gpxFile.setCreator(GPXElevationLookupTool.class.getName() + "(" + gpxCreator + ")");
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
         gpxFile.createTrackPoint(currentTrackSegment, elevation == null ? trackPoint : new TrackPoint(trackPoint, elevation));
         }

      public void handleGPXEnd()
         {
         elevationDataset.close();
         }

      private Element getElement()
         {
         return gpxFile.toElement();
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
   }
