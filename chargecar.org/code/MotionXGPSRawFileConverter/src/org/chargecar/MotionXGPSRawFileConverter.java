package org.chargecar;

import java.io.File;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.gpx.GPXEventHandlerAdapter;
import org.chargecar.gpx.GPXFile;
import org.chargecar.gpx.TrackPoint;
import org.chargecar.xml.XmlHelper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class MotionXGPSRawFileConverter
   {
   private static final Log LOG = LogFactory.getLog(MotionXGPSRawFileConverter.class);

   public static void main(final String[] args) throws IOException, JDOMException
      {
      final long startTime = System.currentTimeMillis();

      if (args.length < 1)
         {
         LOG.error("MotionX-GPS raw file not specified.  Aborting.");
         System.exit(1);
         }

      final File rawFile = new File(args[args.length - 1]);

      if (LOG.isDebugEnabled())
         {
         LOG.debug("Converting MotionX-GPS raw file [" + rawFile.getAbsolutePath() + "]...");
         }

      // create the raw file reader
      final MotionXGPSRawFileReader rawFileReader = new MotionXGPSRawFileReader(rawFile);

      // add the event handler which creates the GPX
      rawFileReader.addGPXEventHandler(new GPXCreator());

      // read the GPX so we can get the lat/long ranges
      rawFileReader.read();

      final long endTime = System.currentTimeMillis();
      if (LOG.isDebugEnabled())
         {
         LOG.debug("Total time (ms) to convert raw file [" + rawFile.getAbsolutePath() + "]: " + (endTime - startTime));
         }
      }

   private MotionXGPSRawFileConverter()
      {
      // private to prevent instantiation
      }

   private static class GPXCreator extends GPXEventHandlerAdapter
      {
      private final GPXFile gpxFile = new GPXFile();
      private Element currentTrack;
      private Element currentTrackSegment;

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
         gpxFile.createTrackPoint(currentTrackSegment, trackPoint);
         }

      public void handleGPXEnd()
         {
         System.out.println(XmlHelper.writeDocumentToStringFormatted(new Document(gpxFile.toElement())));
         }
      }
   }
