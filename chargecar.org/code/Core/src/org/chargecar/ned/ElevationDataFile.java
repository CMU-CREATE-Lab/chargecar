package org.chargecar.ned;

import java.io.FileNotFoundException;

/**
 * <p>
 * <code>ElevationDataFile</code> aids in reading a USGS elevation data file.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ElevationDataFile extends ElevationService
   {
   void open() throws FileNotFoundException;

   boolean isNoDataElevation(final Double elevation);

   void close();
   }