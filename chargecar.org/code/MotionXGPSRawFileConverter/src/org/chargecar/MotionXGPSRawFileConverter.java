package org.chargecar;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
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
 * <code>MotionXGPSRawFileConverter</code> takes a MotionX-GPS .kmz file or raw.xml file and produces a GPX file with
 * fractional-second timestamps.  If no input file is given, it launches a GUI which the user can use to choose one.
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class MotionXGPSRawFileConverter
   {
   private static final Log LOG = LogFactory.getLog(MotionXGPSRawFileConverter.class);
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(MotionXGPSRawFileConverter.class.getName());
   private static final String APPLICATION_NAME = RESOURCES.getString("application.name");

   public static void main(final String[] args) throws IOException, JDOMException
      {
      final MotionXGPSRawFileConverter converter = new MotionXGPSRawFileConverter();

      if (args.length < 1)
         {
         //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
         SwingUtilities.invokeLater(
               new Runnable()
               {
               public void run()
                  {
                  final JFrame jFrame = new JFrame(APPLICATION_NAME);

                  // add the root panel to the JFrame
                  jFrame.add(new MotionXGPSRawFileConverterGUI(converter));

                  // set various properties for the JFrame
                  jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                  jFrame.setBackground(Color.WHITE);
                  jFrame.setResizable(true);
                  jFrame.pack();
                  jFrame.setLocationRelativeTo(null);// center the window on the screen
                  jFrame.setVisible(true);
                  }
               });
         }
      else
         {
         final File file = new File(args[args.length - 1]);

         System.out.println(converter.convert(file));
         }
      }

   String convert(final File file) throws IOException, JDOMException
      {
      final long startTime = System.currentTimeMillis();
      if (LOG.isDebugEnabled())
         {
         LOG.debug("Converting MotionX-GPS file [" + file.getAbsolutePath() + "]...");
         }

      // create the raw file reader
      final MotionXGPSRawFileReader rawFileReader = new MotionXGPSRawFileReader(file);

      // add the event handler which creates the GPX
      final GPXCreator gpxCreator = new GPXCreator();
      rawFileReader.addGPXEventHandler(gpxCreator);

      // read the GPX so we can get the lat/long ranges
      rawFileReader.read();

      final long endTime = System.currentTimeMillis();
      if (LOG.isDebugEnabled())
         {
         LOG.debug("Total time (ms) to convert raw file [" + file.getAbsolutePath() + "]: " + (endTime - startTime));
         }

      return gpxCreator.getGPX();
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

      public String getGPX()
         {
         return XmlHelper.writeDocumentToStringFormatted(new Document(gpxFile.toElement()));
         }
      }
   }
