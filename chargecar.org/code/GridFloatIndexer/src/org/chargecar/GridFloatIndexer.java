package org.chargecar;

import java.io.File;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.file.FileFinder;
import org.chargecar.ned.gridfloat.GridFloatDataFile;
import org.chargecar.ned.gridfloat.GridFloatIndex;

/**
 * <p>
 * <code>GridFloatIndexer</code> produces an index of USGS NED files.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GridFloatIndexer
   {
   private static final Log LOG = LogFactory.getLog(GridFloatIndexer.class);

   public static void main(final String[] args)
      {
      if (args.length < 1)
         {
         LOG.error("Root directory of GridFloat dataset repository not specified.  Aborting.");
         System.exit(1);
         }
      final File rootDirectory = new File(args[0]);

      final FileFinder fileFinder = new FileFinder(rootDirectory, GridFloatDataFile.HEADER_FILE_FILTER);
      final GridFloatIndex gridFloatIndex = new GridFloatIndex();
      final MyFileFinderEventHandler fileFinderEventHandler = new MyFileFinderEventHandler(gridFloatIndex);
      fileFinder.addEventHandler(fileFinderEventHandler);

      if (gridFloatIndex.open())
         {
         fileFinder.find();
         gridFloatIndex.close();
         }
      else
         {
         LOG.error("Failed to open the GridFloatIndex");
         }
      }

   private static class MyFileFinderEventHandler implements FileFinder.EventHandler
      {
      private final GridFloatIndex gridFloatIndex;

      private MyFileFinderEventHandler(final GridFloatIndex gridFloatIndex)
         {
         this.gridFloatIndex = gridFloatIndex;
         }

      public void handFileFoundEvent(final File file)
         {
         try
            {
            final GridFloatDataFile gridFloatDataFile = GridFloatDataFile.create(file);
            if (gridFloatIndex.add(gridFloatDataFile))
               {
               if (LOG.isDebugEnabled())
                  {
                  LOG.debug("Inserted into index: " + gridFloatDataFile.getDataFile().getName());
                  }
               }
            else
               {
               if (LOG.isErrorEnabled())
                  {
                  LOG.error("Failed to insert into index: " + gridFloatDataFile.getDataFile().getName());
                  }
               }
            }
         catch (IOException e)
            {
            if (LOG.isErrorEnabled())
               {
               LOG.error("IOException while trying to process header file [" + file + "]", e);
               }
            }
         }
      }

   private GridFloatIndexer()
      {
      // private to prevent instantiation
      }
   }
