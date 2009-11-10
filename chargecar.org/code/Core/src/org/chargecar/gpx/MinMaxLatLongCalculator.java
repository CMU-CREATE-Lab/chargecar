package org.chargecar.gpx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class MinMaxLatLongCalculator extends GPXEventHandlerAdapter
   {
   private static final Log LOG = LogFactory.getLog(MinMaxLatLongCalculator.class);

   private double minLongitude = Double.POSITIVE_INFINITY;
   private double maxLongitude = Double.NEGATIVE_INFINITY;
   private double minLatitude = Double.POSITIVE_INFINITY;
   private double maxLatitude = Double.NEGATIVE_INFINITY;

   public void handleGPXBegin()
      {
      LOG.trace("MinMaxLatLongCalculator.handleGPXBegin()");
      }

   public void handleTrackPoint(final TrackPoint trackPoint)
      {
      final Double longitude = trackPoint.getLongitude();
      final Double latitude = trackPoint.getLatitude();
      minLongitude = Math.min(longitude, minLongitude);
      maxLongitude = Math.max(longitude, maxLongitude);
      minLatitude = Math.min(latitude, minLatitude);
      maxLatitude = Math.max(latitude, maxLatitude);
      }

   public void handleGPXEnd()
      {
      if (LOG.isTraceEnabled())
         {
         LOG.trace("MinMaxLatLongCalculator.handleGPXEnd()");
         LOG.trace("minLongitude = [" + minLongitude + "]");
         LOG.trace("maxLongitude = [" + maxLongitude + "]");
         LOG.trace("minLatitude = [" + minLatitude + "]");
         LOG.trace("maxLatitude = [" + maxLatitude + "]");
         }
      }

   public double getMinLongitude()
      {
      return minLongitude;
      }

   public double getMaxLongitude()
      {
      return maxLongitude;
      }

   public double getMinLatitude()
      {
      return minLatitude;
      }

   public double getMaxLatitude()
      {
      return maxLatitude;
      }
   }
