package org.chargecar.gpx;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class UTCHelper
   {
   private static final Log LOG = LogFactory.getLog(UTCHelper.class);

   public static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC);
   public static final DateTimeFormatter ISO_DATE_TIME_FORMATTER_FRACTIONAL_SECONDS = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);

   public static DateTime getUTCTimestampAsDateTime(final String timestamp)
      {
      if (timestamp != null)
         {
         // Parse the timestamp, first trying the fractional second parser, then fall back to the non-fractional
         // second parser if the first fails.
         DateTime dateTime = null;
         try
            {
            dateTime = ISO_DATE_TIME_FORMATTER_FRACTIONAL_SECONDS.parseDateTime(timestamp);
            }
         catch (Exception e)
            {
            try
               {
               dateTime = ISO_DATE_TIME_FORMATTER.parseDateTime(timestamp);
               }
            catch (Exception e1)
               {
               LOG.error("Exception while parsing the timestamp [" + timestamp + "]", e1);
               }
            }

         return dateTime;
         }

      return null;
      }

   public static Date getUTCTimestampAsDate(final String timestamp)
      {
      if (timestamp != null)
         {
         final DateTime dateTime = getUTCTimestampAsDateTime(timestamp);

         if (dateTime != null)
            {
            return dateTime.toDate();
            }
         }

      return null;
      }

   private UTCHelper()
      {
      // private to prevent instantiation
      }
   }
