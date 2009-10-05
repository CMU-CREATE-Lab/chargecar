package org.chargecar.ned;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class ElevationRangeFilter implements ElevationDataFilter
   {
   private static final Log LOG = LogFactory.getLog(ElevationRangeFilter.class);

   private static final double FEET_PER_METER = 3.2808399;

   private double maxElevation = Double.MIN_VALUE;
   private double minElevation = Double.MAX_VALUE;

   public void onBeforeProcessing()
      {
      maxElevation = Float.MIN_VALUE;
      minElevation = Float.MAX_VALUE;
      }

   public void processElevation(final int col, final int row, final double elevationInMeters)
      {
      minElevation = Math.min(elevationInMeters, minElevation);
      maxElevation = Math.max(elevationInMeters, maxElevation);
      }

   public void onAfterProcessing(final long elapsedTimeInMilliseconds)
      {
      if (LOG.isDebugEnabled())
         {
         LOG.debug("Min Elevation (ft)   = " + minElevation * FEET_PER_METER);
         LOG.debug("Max Elevation (ft)   = " + maxElevation * FEET_PER_METER);
         LOG.debug("Elevation Range (ft) = " + getElevationRangeInMeters() * FEET_PER_METER);
         LOG.debug("Elapsed time to read elevations (ms) = " + elapsedTimeInMilliseconds);
         }
      }

   public double getMaxElevationInMeters()
      {
      return maxElevation;
      }

   public double getMinElevationInMeters()
      {
      return minElevation;
      }

   public double getElevationRangeInMeters()
      {
      return maxElevation - minElevation;
      }
   }
