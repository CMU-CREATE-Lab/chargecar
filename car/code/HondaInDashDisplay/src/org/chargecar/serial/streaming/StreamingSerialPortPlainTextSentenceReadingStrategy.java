package org.chargecar.serial.streaming;

import java.io.IOException;
import edu.cmu.ri.createlab.serial.SerialPortIOHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class StreamingSerialPortPlainTextSentenceReadingStrategy extends BaseStreamingSerialPortSentenceReadingStrategy
   {
   private static final Log LOG = LogFactory.getLog(StreamingSerialPortPlainTextSentenceReadingStrategy.class);
   private final Character sentenceDelimiter;

   public StreamingSerialPortPlainTextSentenceReadingStrategy(final SerialPortIOHelper serialPortIoHelper, final Character sentenceDelimiter)
      {
      super(serialPortIoHelper);
      this.sentenceDelimiter = sentenceDelimiter;
      }

   public byte[] getNextSentence() throws IOException
      {
      final StringBuilder sb = new StringBuilder();

      while (true)
         {
         // Reads the next byte if data is available.  If no data is available, then b will be null
         final Byte b = readByte();

         // if the byte is null, then no data is available, so just wait a bit
         if (b == null)
            {
            try
               {
               Thread.sleep(5);
               }
            catch (InterruptedException e)
               {
               LOG.error("InterruptedException while sleeping", e);
               }
            continue;
            }
         else
            {
            final char c = (char)b.byteValue();
            if (sentenceDelimiter.equals(c))
               {
               return sb.toString().getBytes();
               }
            else
               {
               sb.append(c);
               }
            }
         }
      }

   //public final byte[] getNextSentenceOLD() throws IOException
   //   {
   //   while (!reader.ready())
   //      {
   //      try
   //         {
   //         Thread.sleep(5);
   //         }
   //      catch (InterruptedException e)
   //         {
   //         LOG.error("InterruptedException while sleeping", e);
   //         }
   //      }
   //
   //   final String line = reader.readLine();
   //   if (line != null)
   //      {
   //      return line.getBytes();
   //      }
   //   LOG.error("StreamingSerialPortPlainTextSentenceReadingStrategy.getNextSentence(): End of stream reached while trying to read the data");
   //   throw new IOException("End of stream reached while trying to read the data");
   //   }
   }
