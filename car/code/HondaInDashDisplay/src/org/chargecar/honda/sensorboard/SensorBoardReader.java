package org.chargecar.honda.sensorboard;

import java.util.Date;
import edu.cmu.ri.createlab.serial.config.BaudRate;
import edu.cmu.ri.createlab.serial.config.CharacterSize;
import edu.cmu.ri.createlab.serial.config.FlowControl;
import edu.cmu.ri.createlab.serial.config.Parity;
import edu.cmu.ri.createlab.serial.config.SerialIOConfiguration;
import edu.cmu.ri.createlab.serial.config.StopBits;
import edu.cmu.ri.createlab.util.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.chargecar.serial.streaming.DefaultSerialIOManager;
import org.chargecar.serial.streaming.SerialIOManager;
import org.chargecar.serial.streaming.StreamingSerialPortReader;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class SensorBoardReader extends StreamingSerialPortReader<SensorBoardEvent>
   {
   private static final Logger LOG = Logger.getLogger(SensorBoardReader.class);

   private static final Character SENTENCE_DELIMETER = '\r';
   private static final String WORD_DELIMETER = ",";
   private static final int NUM_WORDS_PER_SENTENCE = 5;
   private static final String KEY_VALUE_DELIMITER = "=";
   private static final double CELSIUS_CONVERSION_MULTIPLIER = 5.0 / 9.0;

   private static double convertToCelsius(final Double motorTemperature)
      {
      return CELSIUS_CONVERSION_MULTIPLIER * (motorTemperature - 32.0);
      }

   SensorBoardReader(final String serialPortName)
      {
      this(new DefaultSerialIOManager(SensorBoardReader.class.getClass().getSimpleName(),
                                      new SerialIOConfiguration(serialPortName,
                                                                BaudRate.BAUD_9600,
                                                                CharacterSize.EIGHT,
                                                                Parity.NONE,
                                                                StopBits.ONE,
                                                                FlowControl.NONE)));
      }

   SensorBoardReader(final SerialIOManager serialIOManager)
      {
      super(serialIOManager, SENTENCE_DELIMETER);
      }

   public void processSentence(final Date timestamp, final byte[] sentenceBytes)
      {
      if (sentenceBytes != null)
         {
         final String sentence = new String(sentenceBytes);

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
               publishDataEvent(new SensorBoardEvent(timestamp,
                                                     convertToCelsius(motorTemperature),
                                                     convertToCelsius(controllerTemperature),
                                                     throttleValue,
                                                     regenValue));
               }
            }
         else
            {
            if (LOG.isEnabledFor(Level.ERROR))
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
