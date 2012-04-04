package org.chargecar.honda.halleffect;

import edu.cmu.ri.createlab.serial.config.*;
import org.apache.log4j.Logger;
import org.chargecar.serial.streaming.DefaultSerialIOManager;
import org.chargecar.serial.streaming.SerialIOManager;
import org.chargecar.serial.streaming.StreamingSerialPortReader;

import java.util.Date;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
class HallEffectReader extends StreamingSerialPortReader<HallEffectEvent> {
    private static final Logger LOG = Logger.getLogger(HallEffectReader.class);
    private static final Character SENTENCE_DELIMETER = '\n';
    private static final String WORD_DELIMETER = ",";
    private static final int NUM_WORDS_PER_SENTENCE = 3;

    HallEffectReader(final String serialPortName) {
        this(new DefaultSerialIOManager(HallEffectReader.class.getClass().getSimpleName(),
                new SerialIOConfiguration(serialPortName,
                        BaudRate.BAUD_9600,
                        CharacterSize.EIGHT,
                        Parity.NONE,
                        StopBits.ONE,
                        FlowControl.NONE)));
    }

    HallEffectReader(final SerialIOManager serialIOManager) {
        super(serialIOManager, SENTENCE_DELIMETER);
    }

    protected void processSentence(final Date timestamp, final byte[] sentenceBytes) {

        final String sentence = new String(sentenceBytes);

         // process the sentence and then create the SensorBoardEvent
         final String[] words = sentence.split(WORD_DELIMETER);
         if (words.length >= NUM_WORDS_PER_SENTENCE)
            {
            final Integer speed = Integer.parseInt(words[0]);
            final Double motorTemp = Double.parseDouble(words[1]);
            final Double controllerTemp = Double.parseDouble(words[2]);

            if (speed != null &&
                motorTemp != null &&
                controllerTemp != null)
               {
               publishDataEvent(new HallEffectEvent(timestamp,
                                                     speed,
                                                     motorTemp,
                                                     controllerTemp));
               }
            } else {
             LOG.error("HallEffectReader.processSentence(): failed to parse string [" + sentence + "] as int.  Ignoring.");
         }


    }
}
