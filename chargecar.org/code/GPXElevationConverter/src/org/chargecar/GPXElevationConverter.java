package org.chargecar;

import java.io.File;
import edu.cmu.ri.createlab.xml.XmlHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.gpx.GPXElevationLookupTool;
import org.jdom.Document;
import org.jdom.Element;

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

   public static void main(final String[] args)
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

      try
         {
         final Element gpxElement;
         if (willQueryUSGSWebService)
            {
            gpxElement = GPXElevationLookupTool.getInstance().convertElevationsUsingOnlineData(gpxFile);
            }
         else
            {
            gpxElement = GPXElevationLookupTool.getInstance().convertElevationsUsingLocalData(gpxFile);
            }

         System.out.println(XmlHelper.writeDocumentToStringFormatted(new Document(gpxElement)));

         final long endTime = System.currentTimeMillis();
         if (LOG.isDebugEnabled())
            {
            LOG.debug("Total time (ms) to convert elevations for GPX [" + gpxFile.getAbsolutePath() + "]: " + (endTime - startTime));
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

   private GPXElevationConverter()
      {
      // private to prevent instantiation
      }
   }