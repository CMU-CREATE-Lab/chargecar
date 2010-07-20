package org.chargecar.honda.sensorboard;

import java.util.Date;
import edu.cmu.ri.createlab.serial.config.BaudRate;
import edu.cmu.ri.createlab.serial.config.CharacterSize;
import edu.cmu.ri.createlab.serial.config.FlowControl;
import edu.cmu.ri.createlab.serial.config.Parity;
import edu.cmu.ri.createlab.serial.config.SerialIOConfiguration;
import edu.cmu.ri.createlab.serial.config.StopBits;
import edu.cmu.ri.createlab.util.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.serial.streaming.DefaultSerialIOManager;
import org.chargecar.serial.streaming.SerialIOManager;
import org.chargecar.serial.streaming.StreamingSerialPortReader;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SensorBoardReader extends StreamingSerialPortReader<SensorBoardEvent>
   {
   private static final Log LOG = LogFactory.getLog(SensorBoardReader.class);
   private static final char SENTENCE_DELIMETER = '\r';
   private static final String WORD_DELIMETER = ",";
   private static final int NUM_WORDS_PER_SENTENCE = 5;
   private static final String KEY_VALUE_DELIMITER = "=";

   public SensorBoardReader(final String serialPortName)
      {
      this(new DefaultSerialIOManager(SensorBoardReader.class.getClass().getSimpleName(),
                                      new SerialIOConfiguration(serialPortName,
                                                                BaudRate.BAUD_9600,
                                                                CharacterSize.EIGHT,
                                                                Parity.NONE,
                                                                StopBits.ONE,
                                                                FlowControl.NONE)));
      }

   public SensorBoardReader(final SerialIOManager serialIOManager)
      {
      super(SENTENCE_DELIMETER, serialIOManager);
      }

   protected void processSentence(final Date timestamp, final String sentence)
      {
      if (sentence != null)
         {
         // process the sentence and then create the SensorBoardEvent
         final String[] words = sentence.split(WORD_DELIMETER);
         if (words.length >= NUM_WORDS_PER_SENTENCE)
            {
            final Double motorTemperature = getDouble(words[1]);
            final Double controllerTemperature = getDouble(words[2]);
            final Integer throttleValue = getInteger(words[3]);
            final Integer regenValue = getInteger(words[4]);

            if (motorTemperature != null &&
                controllerTemperature != null &&
                throttleValue != null &&
                regenValue != null)
               {
               publishEvent(new SensorBoardEvent(timestamp,
                                                 motorTemperature,
                                                 controllerTemperature,
                                                 throttleValue,
                                                 regenValue));
               }
            }
         else
            {
            if (LOG.isErrorEnabled())
               {
               LOG.error("SensorBoardReader.processSentence(): unexpected number of words in sentence--found [" + words.length + "] but expected at least [" + NUM_WORDS_PER_SENTENCE + "]");
               }
            }
         }
      }

   private Double getDouble(final String word)
      {
      final String[] keyValue = word.split(KEY_VALUE_DELIMITER);
      if (keyValue.length == 2)
         {
         return StringUtils.convertStringToDouble(keyValue[1]);
         }
      return null;
      }

   private Integer getInteger(final String word)
      {
      final String[] keyValue = word.split(KEY_VALUE_DELIMITER);
      if (keyValue.length == 2)
         {
         return StringUtils.convertStringToInteger(keyValue[1]);
         }
      return null;
      }
   }