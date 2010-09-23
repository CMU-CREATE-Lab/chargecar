package org.chargecar.ned.gridfloat;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.chargecar.ned.ElevationDataFilter;
import org.chargecar.ned.ElevationDatasetException;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GridFloatDataFileProcessor
   {
   private static final Logger LOG = Logger.getLogger(GridFloatDataFileProcessor.class);

   private final GridFloatDataFile gridFloatDataFile;

   public GridFloatDataFileProcessor(final GridFloatDataFile gridFloatDataFile)
      {
      this.gridFloatDataFile = gridFloatDataFile;
      }

   public void applyFilter(final ElevationDataFilter filter) throws IOException, ElevationDatasetException
      {
      final File file = gridFloatDataFile.getDataFile();
      if (file.exists())
         {
         final long expectedFileLength = gridFloatDataFile.getNumColumns() * gridFloatDataFile.getNumRows() * GridFloatDataFile.BYTES_PER_FLOAT;
         final long actualFileLength = file.length();
         if (actualFileLength != expectedFileLength)
            {
            final String message = "Aborting due to invalid file length: actual = [" + actualFileLength + "], expected [" + expectedFileLength + "]";
            LOG.error(message);
            throw new ElevationDatasetException(message);
            }

         filter.onBeforeProcessing();
         final long startingTimestamp = System.currentTimeMillis();
         final DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
         final byte[] buffer = new byte[GridFloatDataFile.BYTES_PER_FLOAT];
         for (int row = 0; row < gridFloatDataFile.getNumRows(); row++)
            {
            for (int col = 0; col < gridFloatDataFile.getNumColumns(); col++)
               {
               final int bytesRead = dis.read(buffer);
               if (bytesRead != GridFloatDataFile.BYTES_PER_FLOAT)
                  {
                  final String message = "Aborting due to invalid number of bytes read: actual = [" + bytesRead + "], expected [" + GridFloatDataFile.BYTES_PER_FLOAT + "]";
                  LOG.error(message);
                  throw new ElevationDatasetException(message);
                  }

               filter.processElevation(col, row, GridFloatDataFile.convertGridFloatToElevation(buffer, gridFloatDataFile.isBigEndian()));
               }
            }
         final long endingTimestamp = System.currentTimeMillis();
         filter.onAfterProcessing(endingTimestamp - startingTimestamp);
         }
      else
         {
         if (LOG.isEnabledFor(Level.ERROR))
            {
            LOG.error("File [" + file + "] does not exist.");
            }
         }
      }
   }
