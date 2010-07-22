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
import org.chargecar.serial.streaming.DefaultSerialIOManager;
import org.chargecar.serial.streaming.SerialIOManager;
import org.chargecar.serial.streaming.StreamingSerialPortEventPublisher;
import org.chargecar.serial.streaming.StreamingSerialPortReader;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class NMEAReader extends StreamingSerialPortReader<NMEAEvent>
   {
   private static final char SENTENCE_DELIMETER = '\n';
   private static final String WORD_DELIMITER = ",";
   private static final String DEGREES_SYMBOL = "\u00b0";
   private static final String GPS_FIX_DATA_SENTENCE_PREFIX = "$GPGGA";
   private static final String GARMIN_ALTITUDE_SENTENCE_PREFIX = "$PGRMZ";

   private static final Map<String, NMEASentenceProcessor> SENTENCE_PROCESSORS;

   static
      {
      final Map<String, NMEASentenceProcessor> sentenceProcessors = new HashMap<String, NMEASentenceProcessor>();
      sentenceProcessors.put(GPS_FIX_DATA_SENTENCE_PREFIX,
                             new NMEASentenceProcessor()
                             {
                             public void process(final Date timestamp, final String sentence, final StreamingSerialPortEventPublisher<NMEAEvent> eventPublisher)
                                {
                                final Scanner s = new Scanner(sentence).useDelimiter(WORD_DELIMITER);
                                s.next();  // ignore the time
                                final String lat1 = s.next();  // latitude
                                final String lat2 = s.next();  // latitude direction
                                final String latitude = lat2 + " " + lat1.substring(0, 2) + DEGREES_SYMBOL + lat1.substring(2) + "'";

                                final String long1 = s.next();  // longitude
                                final String long2 = s.next();  // longitude direction
                                final String longitude = long2 + long1.substring(0, 3) + DEGREES_SYMBOL + long1.substring(3) + "'";
                                s.next();  // ignore the fix quality
                                final int numSatellites = s.nextInt();  // number of satellites being tracked

                                // publish the event
                                eventPublisher.publishEvent(NMEAEvent.createLocationEvent(timestamp, latitude, longitude, numSatellites));
                                }
                             });

      sentenceProcessors.put(GARMIN_ALTITUDE_SENTENCE_PREFIX,
                             new NMEASentenceProcessor()
                             {
                             public void process(final Date timestamp, final String sentence, final StreamingSerialPortEventPublisher<NMEAEvent> eventPublisher)
                                {
                                final Scanner s = new Scanner(sentence).useDelimiter(WORD_DELIMITER);
                                final int elevationInFeet = s.nextInt();  // elevation in feet

                                // publish the event
                                eventPublisher.publishEvent(NMEAEvent.createElevationEvent(timestamp, elevationInFeet));
                                }
                             });

      SENTENCE_PROCESSORS = Collections.unmodifiableMap(sentenceProcessors);
      }

   public NMEAReader(final String serialPortName)
      {
      this(new DefaultSerialIOManager(NMEAReader.class.getClass().getSimpleName(),
                                      new SerialIOConfiguration(serialPortName,
                                                                BaudRate.BAUD_4800,
                                                                CharacterSize.EIGHT,
                                                                Parity.NONE,
                                                                StopBits.ONE,
                                                                FlowControl.NONE)));
      }

   public NMEAReader(final SerialIOManager serialIOManager)
      {
      super(SENTENCE_DELIMETER, serialIOManager);
      }

   protected void processSentence(final Date timestamp, final String sentence)
      {
      if (sentence != null)
         {
         final int firstDelimiter = sentence.indexOf(WORD_DELIMITER);
         final String command = sentence.substring(0, firstDelimiter);
         final NMEASentenceProcessor sentenceProcessor = SENTENCE_PROCESSORS.get(command);
         if (sentenceProcessor != null)
            {
            sentenceProcessor.process(timestamp, sentence.substring(firstDelimiter + 1), this);
            }
         }
      }

   private interface NMEASentenceProcessor
      {
      void process(final Date timestamp, final String sentence, final StreamingSerialPortEventPublisher<NMEAEvent> eventPublisher);
      }
   }