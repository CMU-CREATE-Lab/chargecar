package org.chargecar.ned;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ElevationDataFilter
   {
   void onBeforeProcessing();

   void processElevation(final int col, final int row, final double elevationInMeters);

   void onAfterProcessing(final long elapsedTimeInMilliseconds);
   }