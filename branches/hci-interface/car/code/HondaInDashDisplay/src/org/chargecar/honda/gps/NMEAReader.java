package org.chargecar.honda.gps;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import edu.cmu.ri.createlab.serial.config.BaudRate;
import edu.cmu.ri.createlab.serial.config.CharacterSize;
import edu.cmu.ri.createlab.serial.config.FlowControl;
import edu.cmu.ri.createlab.serial.config.Parity;
import edu.cmu.ri.createlab.serial.config.SerialIOConfiguration;
import edu.cmu.ri.createlab.serial.config.StopBits;
import org.apache.log4j.Logger;
import org.chargecar.serial.streaming.DefaultSerialIOManager;
import org.chargecar.serial.streaming.SerialIOManager;
import org.chargecar.serial.streaming.StreamingSerialPortReader;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
class NMEAReader extends StreamingSerialPortReader<GPSEvent>
   {
   private static final Logger LOG = Logger.getLogger(NMEAReader.class);

   private static final Character SENTENCE_DELIMETER = '\n';
   private static final String WORD_DELIMITER = ",";
   private static final String DEGREES_SYMBOL = "\u00b0";
   private static final String GPS_FIX_DATA_SENTENCE_PREFIX = "$GPGGA";
   private static final String GARMIN_ALTITUDE_SENTENCE_PREFIX = "$PGRMZ";
   private static final double FEET_PER_METER = 3.2808399;

   private static final Map<String, NMEASentenceProcessor> SENTENCE_PROCESSORS;

   static
      {
      final Map<String, NMEASentenceProcessor> sentenceProcessors = new HashMap<String, NMEASentenceProcessor>();
      sentenceProcessors.put(GPS_FIX_DATA_SENTENCE_PREFIX,
                             new NMEASentenceProcessor()
                             {
                             public void process(final Date timestamp, final String sentence, final NMEAReader nmeaReader)
                                {
                                try
                                   {
                                   final Scanner s = new Scanner(sentence).useDelimiter(WORD_DELIMITER);
                                   s.next();  // ignore the time
                                   final String lat1 = s.next();  // latitude
                                   final String lat2 = s.next();  // latitude direction
                                   final String latitude = (lat1.length() == 0 || lat2.length() == 0) ? null : lat2 + " " + lat1.substring(0, 2) + DEGREES_SYMBOL + lat1.substring(2) + "'";

                                   final String long1 = s.next();  // longitude
                                   final String long2 = s.next();  // longitude direction
                                   final String longitude = (long1.length() == 0 || long2.length() == 0) ? null :  long2 + long1.substring(0, 3) + DEGREES_SYMBOL + long1.substring(3) + "'";
                                   
                                   s.next();  // ignore the fix quality
                                   final int numSatellites = s.nextInt();  // number of satellites being tracked
                                   s.next();  // ignore horizontal dilution of position
                                   final String elevationInMetersStr = s.next();   // elevation in meters
                                   Long elevationInFeet;
                                   try
                                      {
                                      final float elevationInMeters = Float.parseFloat(elevationInMetersStr);
                                      elevationInFeet = Math.round(elevationInMeters * FEET_PER_METER);
                                      }
                                   catch (NumberFormatException ignored)
                                      {
                                      LOG.error("NumberFormatException while trying to convert the elevation in meters [" + elevationInMetersStr + "] to a float");
                                      elevationInFeet = null;
                                      }

                                   // publish the event
                                   nmeaReader.publishDataEvent(GPSEvent.createLocationEvent(timestamp, latitude, longitude, numSatellites, elevationInFeet));
                                   }
                                catch (StringIndexOutOfBoundsException ignored)
                                   {
                                   LOG.error("StringIndexOutOfBoundsException while trying to parse the NMEA sentence.  Ignoring it [" + sentence + "]");
                                   }
                                catch (Exception e)
                                   {
                                   LOG.error("Exception while trying to parse the NMEA sentence.  Ignoring it [" + sentence + "]", e);
                                   }
                                }
                             });

      sentenceProcessors.put(GARMIN_ALTITUDE_SENTENCE_PREFIX,
                             new NMEASentenceProcessor()
                             {
                             public void process(final Date timestamp, final String sentence, final NMEAReader nmeaReader)
                                {
                                final Scanner s = new Scanner(sentence).useDelimiter(WORD_DELIMITER);
                                final int elevationInFeet = s.nextInt();  // elevation in feet

                                // publish the event
                                nmeaReader.publishDataEvent(GPSEvent.createElevationEvent(timestamp, elevationInFeet));
                                }
                             });

      SENTENCE_PROCESSORS = Collections.unmodifiableMap(sentenceProcessors);
      }

   NMEAReader(final String serialPortName)
      {
      this(new DefaultSerialIOManager(NMEAReader.class.getClass().getSimpleName(),
                                      new SerialIOConfiguration(serialPortName,
                                                                BaudRate.BAUD_4800,
                                                                CharacterSize.EIGHT,
                                                                Parity.NONE,
                                                                StopBits.ONE,
                                                                FlowControl.NONE)));
      }

   NMEAReader(final SerialIOManager serialIOManager)
      {
      super(serialIOManager, SENTENCE_DELIMETER);
      }

   protected void processSentence(final Date timestamp, final byte[] sentenceBytes)
      {
      if (sentenceBytes != null)
         {
         final String sentence = new String(sentenceBytes);

         final int firstDelimiter = sentence.indexOf(WORD_DELIMITER);
         if (firstDelimiter >= 0)
            {
            final String command = sentence.substring(0, firstDelimiter);
            final NMEASentenceProcessor sentenceProcessor = SENTENCE_PROCESSORS.get(command);
            if (sentenceProcessor != null)
               {
               sentenceProcessor.process(timestamp, sentence.substring(firstDelimiter + 1), this);
               }
            }
         }
      }

   private interface NMEASentenceProcessor
      {
      void process(final Date timestamp, final String sentence, final NMEAReader nmeaReader);
      }
   }
