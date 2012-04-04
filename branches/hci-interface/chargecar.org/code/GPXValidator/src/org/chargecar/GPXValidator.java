package org.chargecar;

import java.io.File;
import org.apache.log4j.Logger;

/**
 * <p>
 * <code>GPXValidator</code> validates GPX files.  If valid, it prints the word "Valid" to standard out and returns with
 * a status code of zero.  Otherwise, it prints "Invalid: " followed by the error message explaining why the validation
 * failed and then exists with a non-zero status code.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class GPXValidator
   {
   private static final Logger LOG = Logger.getLogger(org.chargecar.GPXValidator.class);

   public static void main(final String[] args)
      {
      if (args.length < 1)
         {
         System.err.println("No GPX file specified.  Aborting.");
         System.exit(1);
         }

      final String gpxFilepath = args[args.length - 1];
      final File gpxFile;
      try
         {
         gpxFile = new File(gpxFilepath);
         if (gpxFile.exists() && gpxFile.isFile())
            {
            // validate
            final org.chargecar.gpx.GPXValidator.ValidationResult validationResult = org.chargecar.gpx.GPXValidator.getInstance().validate(gpxFile);

            final String response = validationResult.isValid() ? "Valid" : "Invalid: " + validationResult.getErrorMessage();
            System.out.println(response);
            LOG.info("Validation result for [" + gpxFile.getAbsolutePath() + "]: " + response);
            System.exit(validationResult.isValid() ? 0 : 1);
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

   private GPXValidator()
      {
      // private to prevent instantiation
      }
   }