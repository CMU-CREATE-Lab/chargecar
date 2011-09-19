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
      
        if (sentenceBytes != null) {
            final String sentence = new String(sentenceBytes).trim();

            try {
                final int value = Integer.parseInt(sentence);
                publishDataEvent(new HallEffectEvent(timestamp, value));
            } catch (NumberFormatException e) {
                LOG.error("HallEffectReader.processSentence(): failed to parse string [" + sentence + "] as int.  Ignoring.");
            }
        }
    }
}
