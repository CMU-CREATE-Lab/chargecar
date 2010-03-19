package org.chargecar;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import edu.cmu.ri.createlab.util.StringUtils;
import edu.cmu.ri.createlab.xml.XmlHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.gpx.GPXEventHandler;
import org.chargecar.gpx.TrackPoint;
import org.chargecar.gpx.UTCHelper;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.joda.time.DateTime;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class MotionXGPSRawFileReader
   {
   private static final Log LOG = LogFactory.getLog(MotionXGPSRawFileReader.class);

   private final File file;
   private final List<GPXEventHandler> eventHandlers = new ArrayList<GPXEventHandler>();

   MotionXGPSRawFileReader(final File file)
      {
      this.file = file;
      }

   public void addGPXEventHandler(final GPXEventHandler gpxEventHandler)
      {
      if (gpxEventHandler != null)
         {
         eventHandlers.add(gpxEventHandler);
         }
      }

   public void read() throws IOException, JDOMException
      {
      // determine whether the specified file is a KMZ or a raw.xml
      Element rootElement;
      try
         {
         // start by assuming it's a KMZ
         final ZipFile zipfile = new ZipFile(file);
         final ZipEntry rawFileZipEntry = zipfile.getEntry("raw.xml");
         if (rawFileZipEntry == null)
            {
            throw new IOException("raw.xml file not found within the KMZ");
            }
         rootElement = XmlHelper.createElementNoValidate(new BufferedInputStream(zipfile.getInputStream(rawFileZipEntry)));
         }
      catch (ZipException e)
         {
         // must not be a zip file, so assume it's an XML file
         rootElement = XmlHelper.createElementNoValidate(file);
         }

      if (rootElement != null && "track".equals(rootElement.getName()))
         {
         for (final GPXEventHandler handler : eventHandlers)
            {
            handler.handleGPXBegin("MotionX-GPX KMZ raw.xml");
            }

         processTrack(rootElement);

         for (final GPXEventHandler handler : eventHandlers)
            {
            handler.handleGPXEnd();
            }
         }
      else
         {
         throw new IOException("XML file does not appear to be a MotionX-GPS raw file.");
         }
      }

   private void processTrack(final Element trackElement) throws IOException
      {
      if (trackElement != null)
         {
         // get the track name
         final String trackName = trackElement.getChildText("name");
         LOG.debug("trackName = [" + trackName + "]");

         // get the starting timestamp (as text)
         final String startingTimestampStr = trackElement.getChildText("formattedStartDate");
         LOG.debug("startingTimestampStr = [" + startingTimestampStr + "]");

         // convert the textual timestamp into a DateTime
         final DateTime startingTimestamp = UTCHelper.getUTCTimestampAsDateTime(startingTimestampStr);
         if (startingTimestamp == null)
            {
            throw new IOException("Missing formattedStartDate element");
            }

         final Element locationsElement = trackElement.getChild("locations");
         if (locationsElement != null)
            {
            final List locationElements = locationsElement.getChildren("location");
            if ((locationElements != null) && (!locationElements.isEmpty()))
               {
               // start the track
               for (final GPXEventHandler handler : eventHandlers)
                  {
                  handler.handleTrackBegin(trackName);
                  }

               // start the track segment
               for (final GPXEventHandler handler : eventHandlers)
                  {
                  handler.handleTrackSegmentBegin();
                  }

               // process each location and fire a track point event
               for (final ListIterator listIterator = locationElements.listIterator(); listIterator.hasNext();)
                  {
                  final Element locationElement = (Element)listIterator.next();

                  // Read the time value (which is the running sum of seconds since the starting time) and add it to
                  // the starting timestamp to get the current timestamp.  We use this value instead of the "activeTime"
                  // attribute because this value has more accuracy.
                  DateTime timestamp = startingTimestamp;
                  final Double timeValue = StringUtils.convertStringToDouble(locationElement.getAttributeValue("time"));
                  if (timeValue != null)
                     {
                     timestamp = timestamp.plusMillis((int)(timeValue * 1000.0));
                     }

                  // create the track point
                  final TrackPoint trackPoint = new TrackPoint(locationElement.getAttributeValue("lon"),
                                                               locationElement.getAttributeValue("lat"),
                                                               timestamp.toString(UTCHelper.ISO_DATE_TIME_FORMATTER_FRACTIONAL_SECONDS),
                                                               locationElement.getAttributeValue("alt"));

                  for (final GPXEventHandler handler : eventHandlers)
                     {
                     handler.handleTrackPoint(trackPoint);
                     }
                  }

               // end the track segment
               for (final GPXEventHandler handler : eventHandlers)
                  {
                  handler.handleTrackSegmentEnd();
                  }

               // end the track
               for (final GPXEventHandler handler : eventHandlers)
                  {
                  handler.handleTrackEnd(trackName);
                  }
               }
            }
         }
      }
   }