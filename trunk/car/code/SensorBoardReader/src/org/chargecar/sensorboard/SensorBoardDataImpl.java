package org.chargecar.sensorboard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class SensorBoardDataImpl implements SensorBoardData
   {
   private static final Log LOG = LogFactory.getLog(SensorBoardDataImpl.class);

   private final long timestamp = System.currentTimeMillis();

   public final long getTimestampMilliseconds()
      {
      return timestamp;
      }

   protected final double convertToDouble(final String rawValue)
      {
      double value = 0.0;
      if (rawValue != null && rawValue.length() > 0)
         {
         try
            {
            final boolean isNegative = rawValue.startsWith("-");
            if (isNegative)
               {
               value = -1 * Double.parseDouble(rawValue.substring(1));
               }
            else
               {
               value = Double.parseDouble(rawValue);
               }
            }
         catch (NumberFormatException e)
            {
            LOG.error("SensorBoardDataImpl.convertToDouble(): NumberFormatException while converting [" + rawValue + "] to a double.");
            }
         }
      return value;
      }
   }
