package org.chargecar.serial.streaming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import edu.cmu.ri.createlab.serial.SerialPortIOHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class StreamingSerialPortPlainTextSentenceReadingStrategy implements StreamingSerialPortSentenceReadingStrategy
   {
   private static final Log LOG = LogFactory.getLog(StreamingSerialPortPlainTextSentenceReadingStrategy.class);

   private BufferedReader reader;

   public StreamingSerialPortPlainTextSentenceReadingStrategy(final SerialPortIOHelper serialPortIoHelper)
      {
      reader = new BufferedReader(new InputStreamReader(serialPortIoHelper.getInputStream()));
      }

   public final byte[] getNextSentence()
      {
      try
         {
         final String line = reader.readLine();
         if (line != null)
            {
            return line.getBytes();
            }
         }
      catch (IOException e)
         {
         LOG.error("StreamingSerialPortPlainTextSentenceReadingStrategy.slurpUpFirstSentence(): IOException while trying to read a sentence", e);
         }
      return null;
      }
   }
