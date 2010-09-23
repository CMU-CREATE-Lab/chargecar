package org.chargecar;

import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.chargecar.ned.ElevationDatasetException;
import org.chargecar.ned.ElevationMapRenderingFilter;
import org.chargecar.ned.ElevationRangeFilter;
import org.chargecar.ned.gridfloat.GridFloatDataFile;
import org.chargecar.ned.gridfloat.GridFloatDataFileProcessor;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class ElevationMapGenerator
   {
   private static final Logger LOG = Logger.getLogger(ElevationMapGenerator.class);

   public static void main(final String[] args) throws IOException, ElevationDatasetException
      {
      // TODO: change this to take min/max lat/long values and a cell size and then fetch the elevations from the NED
      if (args.length < 2)
         {
         LOG.error("GiidFloat data/header file and output file not specified.  Aborting.");
         System.exit(1);
         }

      final File outputFile = new File(args[1]);
      if (outputFile.exists())
         {
         LOG.error("Output file [" + outputFile.getAbsolutePath() + "] already exists.  Aborting.");
         System.exit(1);
         }

      final long startingTimestamp = System.currentTimeMillis();

      final GridFloatDataFile gridFloatDataFile = GridFloatDataFile.create(new File(args[0]));

      LOG.debug("Reading elevations...");
      final GridFloatDataFileProcessor gridFloatDataFileProcessor = new GridFloatDataFileProcessor(gridFloatDataFile);
      final ElevationRangeFilter elevationRangeFilter = new ElevationRangeFilter();
      gridFloatDataFileProcessor.applyFilter(elevationRangeFilter);

      LOG.debug("Generating map...");
      final ElevationMapRenderingFilter elevationMapRenderingFilter = new ElevationMapRenderingFilter(gridFloatDataFile.getNumColumns(),
                                                                                                      gridFloatDataFile.getNumRows(),
                                                                                                      elevationRangeFilter.getMinElevationInMeters(),
                                                                                                      elevationRangeFilter.getElevationRangeInMeters(),
                                                                                                      outputFile);

      gridFloatDataFileProcessor.applyFilter(elevationMapRenderingFilter);

      LOG.debug("Total elapsed time (ms) = " + (System.currentTimeMillis() - startingTimestamp));
      }

   private ElevationMapGenerator()
      {
      // private to prevent instantiation
      }
   }
