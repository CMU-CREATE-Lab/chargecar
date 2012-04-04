package org.chargecar.ned;

/**
 * <p>
 * <code>ElevationDataset</code> aids in reading a USGS elevation data file.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ElevationDataset extends ElevationService
   {
   void open() throws ElevationDatasetException;

   void close();
   }