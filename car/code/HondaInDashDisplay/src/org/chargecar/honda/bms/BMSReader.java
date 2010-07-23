package org.chargecar.honda.bms;

import java.nio.ByteBuffer;
import java.util.Date;
import edu.cmu.ri.createlab.serial.SerialPortIOHelper;
import edu.cmu.ri.createlab.serial.config.BaudRate;
import edu.cmu.ri.createlab.serial.config.CharacterSize;
import edu.cmu.ri.createlab.serial.config.FlowControl;
import edu.cmu.ri.createlab.serial.config.Parity;
import edu.cmu.ri.createlab.serial.config.SerialIOConfiguration;
import edu.cmu.ri.createlab.serial.config.StopBits;
import edu.cmu.ri.createlab.util.ByteUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.serial.streaming.BaseStreamingSerialPortSentenceReadingStrategy;
import org.chargecar.serial.streaming.DefaultSerialIOManager;
import org.chargecar.serial.streaming.SerialIOManager;
import org.chargecar.serial.streaming.StreamingSerialPortReader;
import org.chargecar.serial.streaming.StreamingSerialPortSentenceReadingStrategy;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class BMSReader extends StreamingSerialPortReader<BMSEvent>
   {
   private static final Log LOG = LogFactory.getLog(BMSReader.class);
   private static final Character SENTENCE_DELIMETER = 0x1B;      // ESC
   private static final Character SENTENCE_START_CHAR_1 = 0x5B;   // [
   private static final Character SENTENCE_START_CHAR_2 = 0x48;   // H
   private static final Character GROUP_DELIMETER = ' ';          // SPACE
   private static final int SENTENCE_LENGTH_IN_BYTES = 1653;

   private static final int CONTENT_GROUP_OFFSET = 2;
   private static final int CONTENT_GROUP_LENGTH_IN_BYTES = 64;

   private static final int AUX_GROUP_OFFSET = 67;
   private static final int AUX_GROUP_LENGTH_IN_BYTES = 46;

   private static final int CELL_DATA_GROUP_1_OFFSET = 114;
   private static final int CELL_DATA_GROUP_2_OFFSET = 627;
   private static final int CELL_DATA_GROUP_3_OFFSET = 1140;
   private static final int CELL_DATA_GROUP_LENGTH_IN_BYTES = 512;

   public BMSReader(final String serialPortName)
      {
      this(new DefaultSerialIOManager(BMSReader.class.getClass().getSimpleName(),
                                      new SerialIOConfiguration(serialPortName,
                                                                BaudRate.BAUD_19200,
                                                                CharacterSize.EIGHT,
                                                                Parity.NONE,
                                                                StopBits.ONE,
                                                                FlowControl.NONE)));
      }

   public BMSReader(final SerialIOManager serialIOManager)
      {
      super(serialIOManager);
      }

   @Override
   protected StreamingSerialPortSentenceReadingStrategy createStreamingSerialPortSentenceReadingStrategy(final SerialPortIOHelper serialPortIoHelper)
      {
      return new SentenceReadingStrategy(serialPortIoHelper);
      }

   protected void processSentence(final Date timestamp, final byte[] sentenceBytes)
      {
      if (sentenceBytes != null)
         {
         // do some quick sanity checks to make sure the sentence is valid
         if (sentenceBytes.length == SENTENCE_LENGTH_IN_BYTES &&
             SENTENCE_START_CHAR_1.equals((char)sentenceBytes[0]) &&
             SENTENCE_START_CHAR_2.equals((char)sentenceBytes[1]) &&
             GROUP_DELIMETER.equals((char)sentenceBytes[66]) &&
             GROUP_DELIMETER.equals((char)sentenceBytes[113]) &&
             GROUP_DELIMETER.equals((char)sentenceBytes[626]) &&
             GROUP_DELIMETER.equals((char)sentenceBytes[1139]) &&
             GROUP_DELIMETER.equals((char)sentenceBytes[1652]))
            {
            // convert the content group bytes
            final byte[] contentGroup = ByteUtils.asciiHexBytesToByteArray(sentenceBytes, CONTENT_GROUP_OFFSET, CONTENT_GROUP_LENGTH_IN_BYTES);

            // convert the auxiliary group bytes
            final byte[] auxiliaryGroup = ByteUtils.asciiHexBytesToByteArray(sentenceBytes, AUX_GROUP_OFFSET, AUX_GROUP_LENGTH_IN_BYTES);

            // convert the cell data voltages group bytes
            final byte[] cellDataVoltages = ByteUtils.asciiHexBytesToByteArray(sentenceBytes, CELL_DATA_GROUP_1_OFFSET, CELL_DATA_GROUP_LENGTH_IN_BYTES);

            // convert the cell data temperatures group bytes
            final byte[] cellDataTemperatures = ByteUtils.asciiHexBytesToByteArray(sentenceBytes, CELL_DATA_GROUP_2_OFFSET, CELL_DATA_GROUP_LENGTH_IN_BYTES);

            // convert the cell data resistances group bytes
            final byte[] cellDataResistances = ByteUtils.asciiHexBytesToByteArray(sentenceBytes, CELL_DATA_GROUP_3_OFFSET, CELL_DATA_GROUP_LENGTH_IN_BYTES);

            publishEvent(createBMSEvent(timestamp,
                                        contentGroup,
                                        auxiliaryGroup,
                                        cellDataVoltages,
                                        cellDataTemperatures,
                                        cellDataResistances));
            }
         else
            {
            LOG.error("BMSReader.processSentence(): Unrecognized sentence format");
            }
         }
      else
         {
         LOG.error("BMSReader.processSentence(): Null sentence");
         }
      }

   private BMSEvent createBMSEvent(final Date timestamp,
                                   final byte[] contentGroup,
                                   final byte[] auxiliaryGroup,
                                   final byte[] cellDataVoltages,
                                   final byte[] cellDataTemperatures,
                                   final byte[] cellDataResistances)
      {

      final ByteBuffer contentGroupByteBuffer = ByteBuffer.wrap(contentGroup);
      final ByteBuffer auxiliaryGroupByteBuffer = ByteBuffer.wrap(auxiliaryGroup);
      final ByteBuffer cellDataVoltagesByteBuffer = ByteBuffer.wrap(cellDataVoltages);
      final ByteBuffer cellDataTemperaturesByteBuffer = ByteBuffer.wrap(cellDataTemperatures);
      final ByteBuffer cellDataResistancesByteBuffer = ByteBuffer.wrap(cellDataResistances);

      final BMSFault bmsFault = BMSFault.findByCode(contentGroupByteBuffer.get(0));
      final int numOnOffCycles = contentGroupByteBuffer.getShort(1);
      final int timeSincePowerUpInSecs = convertBytesToInt(contentGroupByteBuffer.get(3),
                                                           contentGroupByteBuffer.get(4),
                                                           contentGroupByteBuffer.get(5));

      return new BMSEvent(timestamp,
                          bmsFault,
                          numOnOffCycles,
                          timeSincePowerUpInSecs);
      }

   private static int convertBytesToInt(final byte b2, final byte b1, final byte b0)
      {
      return (((0 & 0xff) << 24) |
              ((b2 & 0xff) << 16) |
              ((b1 & 0xff) << 8) |
              ((b0 & 0xff) << 0));
      }

   private class SentenceReadingStrategy extends BaseStreamingSerialPortSentenceReadingStrategy
      {
      private SentenceReadingStrategy(final SerialPortIOHelper serialPortIoHelper)
         {
         super(serialPortIoHelper);
         }

      public byte[] getNextSentence()
         {
         final StringBuilder sb = new StringBuilder();

         Byte b;
         while ((b = readByte()) != null)
            {
            final char c = (char)b.byteValue();
            if (SENTENCE_DELIMETER.equals(c))
               {
               return sb.toString().getBytes();
               }
            else
               {
               sb.append(c);
               }
            }
         return null;
         }
      }
   }
