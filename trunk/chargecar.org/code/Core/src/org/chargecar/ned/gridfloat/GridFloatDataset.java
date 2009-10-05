package org.chargecar.ned.gridfloat;

import java.io.FileNotFoundException;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.ned.ElevationDataset;
import org.chargecar.ned.ElevationDatasetException;

/**
 * <p>
 * <code>GridFloatDataset</code> provides a way to lookup elevations from one or more {@link GridFloatDataFile}s.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GridFloatDataset implements ElevationDataset
   {
   private static final Log LOG = LogFactory.getLog(GridFloatDataset.class);

   private final List<GridFloatDataFile> dataFiles;

   public GridFloatDataset(final double minLongitude, final double maxLongitude, final double minLatitude, final double maxLatitude) throws ElevationDatasetException
      {
      final GridFloatIndex gridFloatIndex = new GridFloatIndex();

      if (gridFloatIndex.open())
         {
         this.dataFiles = gridFloatIndex.lookup(minLongitude, maxLongitude, minLatitude, maxLatitude);
         gridFloatIndex.close();
         }
      else
         {
         throw new ElevationDatasetException("Failed to open the GridFloatIndex");
         }
      }

   public void open() throws ElevationDatasetException
      {
      for (final GridFloatDataFile dataFile : dataFiles)
         {
         try
            {
            if (LOG.isDebugEnabled())
               {
               LOG.debug("Opening GridFloatDataFile [" + dataFile.getId() + "].");
               }
            dataFile.open();
            }
         catch (FileNotFoundException e)
            {
            final String message = "FileNotFoundException while trying to open GridFloatDataFile [" + dataFile.getId() + "]";
            LOG.error(message, e);
            throw new ElevationDatasetException(message, e);
            }
         }
      }

   public Double getElevation(final double longitude, final double latitude)
      {
      if ((dataFiles != null) && (!dataFiles.isEmpty()))
         {
         for (final GridFloatDataFile dataFile : dataFiles)
            {
            final Double elevation = dataFile.getElevation(longitude, latitude);
            if (!dataFile.isNoDataElevation(elevation))
               {
               return elevation;
               }
            }
         }

      return null;
      }

   public void close()
      {
      for (final GridFloatDataFile dataFile : dataFiles)
         {
         if (LOG.isDebugEnabled())
            {
            LOG.debug("Closing GridFloatDataFile [" + dataFile.getId() + "].");
            }
         dataFile.close();
         }
      }
   }
