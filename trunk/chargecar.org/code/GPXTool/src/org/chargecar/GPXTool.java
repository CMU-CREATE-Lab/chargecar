package org.chargecar;

import java.io.File;
import edu.cmu.ri.createlab.util.StringUtils;
import edu.cmu.ri.createlab.xml.XmlHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.gpx.GPSCoordinate;
import org.chargecar.gpx.GPXElevationLookupTool;
import org.chargecar.gpx.GPXPrivatizer;
import org.chargecar.gpx.GPXValidator;
import org.jdom.Document;
import org.jdom.Element;

/**
 * <p>
 * <code>GPXTool</code> provides the ability to validate and privatize GPX files as well as update elevation values
 * with values obtained from either a local or remote dataset.  Upon completion, the new GPX file is pretty-printed on
 * stdout.
 * </p>
 * <p>
 * Command line options:
 * <ul>
 * <li><code>--no-validate</code>: Turns off validation</li>
 * <li><code>--privatize</code>: Trims 1/10 of a mile from the beginning and end of the GPX file</li>
 * <li><code>--privatize-location</code>: Removes all GPS data points within a certain radius of the specified location.
 * Radius defaults to 1/10 of a mile if unspecified or invalid.  Location must be specified as LAT,LONG or LAT,LONG,RADIUS
 * for example <code>--privatize-location=40.443887,-79.946357</code> or <code>--privatize-location=40.443887,-79.946357,321.8688</code>.</li>
 * <li><code>--lookup-elevations-locally</code>: Looks up elevations for each data point in the local elevation dataset,
 * uses value in GPX if not found</li>
 * <li><code>--lookup-elevations-online</code>: Looks up elevations for each data point in an online elevation dataset,
 * uses value in GPX if not found.  Ignored if <code>--lookup-elevations-locally</code> is also specified.</li>
 * </ul>
 * </p>
 * <p>
 * If no options are specified, the GPX file is simply validated and pretty-printed.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class GPXTool
   {
   private static final Log LOG = LogFactory.getLog(GPXTool.class);

   private static final String COMMAND_LINE_SWITCH_NO_VALIDATE = "--no-validate";
   private static final String COMMAND_LINE_SWITCH_PRIVATIZE = "--privatize";
   private static final String COMMAND_LINE_SWITCH_PRIVATIZE_LOCATION = "--privatize-location";
   private static final String COMMAND_LINE_SWITCH_LOOKUP_ELEVATIONS_LOCALLY = "--lookup-elevations-locally";
   private static final String COMMAND_LINE_SWITCH_LOOKUP_ELEVATIONS_ONLINE = "--lookup-elevations-online";

   public static void main(final String[] args)
      {
      if (args.length < 1)
         {
         System.err.println("No GPX file specified.  Aborting.");
         System.exit(1);
         }

      boolean willValidate = true;
      boolean willPrivatize = false;
      boolean willPrivatizeLocation = false;
      GPSCoordinate privateLocationGpsCoordinate = null;
      double locationPrivatizationRadius = GPXPrivatizer.DEFAULT_PRIVATIZATION_RADIUS_IN_METERS;
      boolean willDoLocalElevationLookup = false;
      boolean willDoOnlineElevationLookup = false;
      for (int i = 0; i < args.length - 1; i++)
         {
         final String arg = args[i];

         if (arg != null)
            {
            if (COMMAND_LINE_SWITCH_NO_VALIDATE.equals(arg))
               {
               willValidate = false;
               }
            else if (COMMAND_LINE_SWITCH_PRIVATIZE.equals(arg))
               {
               willPrivatize = true;
               }
            else if (arg.startsWith(COMMAND_LINE_SWITCH_PRIVATIZE_LOCATION))
                  {
                  final String[] keyVal = arg.split("=");
                  if (keyVal.length > 1 && keyVal[1].length() > 0)
                     {
                     final String[] latLong = keyVal[1].split(",");
                     if (latLong.length >= 2)
                        {
                        // create the GPSCoordinate
                        privateLocationGpsCoordinate = new GPSCoordinate(latLong[1], latLong[0]);

                        // make sure neither lat or long is null
                        if (privateLocationGpsCoordinate.isNull())
                           {
                           privateLocationGpsCoordinate = null;
                           }
                        }

                     // see whether the user specified a privatization radius
                     if (latLong.length >= 3)
                        {
                        final Double radius = StringUtils.convertStringToDouble(latLong[2]);
                        if (radius != null && Double.compare(radius, 0) > 0)
                           {
                           locationPrivatizationRadius = radius;
                           }
                        }
                     }
                  willPrivatizeLocation = privateLocationGpsCoordinate != null;
                  }
               else if (COMMAND_LINE_SWITCH_LOOKUP_ELEVATIONS_LOCALLY.equals(arg))
                     {
                     willDoLocalElevationLookup = true;
                     }
                  else if (COMMAND_LINE_SWITCH_LOOKUP_ELEVATIONS_ONLINE.equals(arg))
                        {
                        willDoOnlineElevationLookup = true;
                        }
            }
         }

      final String gpxFilepath = args[args.length - 1];
      final File gpxFile;
      try
         {
         gpxFile = new File(gpxFilepath);
         if (gpxFile.exists() && gpxFile.isFile())
            {
            // validate
            final boolean isValid = willValidate && GPXValidator.getInstance().isValid(gpxFile);

            // print an error and abort if we validated and it's not valid.
            if (willValidate && !isValid)
               {
               final String message = "File is not a valid GPX";
               LOG.error(message);
               System.err.println(message);
               System.exit(1);
               }

            try
               {
               // read in the GPX
               Element gpxElement = XmlHelper.createElementNoValidate(gpxFile);

               // privatize, if requested
               if (willPrivatize)
                  {
                  gpxElement = GPXPrivatizer.getInstance().privatize(gpxElement);
                  }

               if (willPrivatizeLocation)
                  {
                  gpxElement = GPXPrivatizer.getInstance().privatize(gpxElement, privateLocationGpsCoordinate, locationPrivatizationRadius);
                  }

               // do elevation lookup, if requested, favoring local lookup
               if (willDoLocalElevationLookup)
                  {
                  gpxElement = GPXElevationLookupTool.getInstance().convertElevationsUsingLocalData(gpxElement);
                  }
               else if (willDoOnlineElevationLookup)
                  {
                  gpxElement = GPXElevationLookupTool.getInstance().convertElevationsUsingOnlineData(gpxElement);
                  }

               // pretty print the XML file
               System.out.println(XmlHelper.writeDocumentToStringFormatted(new Document(gpxElement)));
               }
            catch (Exception e)
               {
               final String message = "The given GPX is not a valid XML file [" + gpxFilepath + "]";
               LOG.error(message, e);
               System.out.println(gpxFilepath);
               System.exit(1);
               }
            }
         else
            {
            final String message = "The path [" + gpxFile.getAbsolutePath() + "] does not exist or does not denote a file.";
            LOG.error(message);
            System.out.println(gpxFilepath);
            System.exit(1);
            }
         }
      catch (Exception e)
         {
         final String message = "Invalid path to the GPX file [" + gpxFilepath + "]";
         LOG.error(message, e);
         System.out.println(gpxFilepath);
         System.exit(1);
         }
      }

   private GPXTool()
      {
      // private to prevent instantiation
      }
   }
