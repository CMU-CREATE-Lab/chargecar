package org.chargecar.serial.streaming;

import java.io.IOException;
import org.apache.log4j.Logger;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class DefaultStreamingSerialPortSentenceReadingStrategy implements StreamingSerialPortSentenceReadingStrategy
   {
   private static final Logger LOG = Logger.getLogger(DefaultStreamingSerialPortSentenceReadingStrategy.class);

   private final SerialIOManager serialIOManager;
   private final Character sentenceDelimiter;

   public DefaultStreamingSerialPortSentenceReadingStrategy(final SerialIOManager serialIOManager, final Character sentenceDelimiter)
      {
      this.serialIOManager = serialIOManager;
      this.sentenceDelimiter = sentenceDelimiter;
      }

   public boolean isDataAvailable() throws IOException
      {
      return serialIOManager.getSerialPortIoHelper().isDataAvailable();
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

   /**
    * Reads the next byte if data is available.  If no data is available, this method returns <code>null</code>.  Throws
    * an {@link IOException} if the end of stream is reached while trying to read the data.
    */
   private Byte readByte() throws IOException
   {
   if (serialIOManager.getSerialPortIoHelper().isDataAvailable())
      {
      final int b = serialIOManager.getSerialPortIoHelper().read();

      if (b >= 0)
         {
         return (byte)b;
         }
      else
         {
         LOG.error("DefaultStreamingSerialPortSentenceReadingStrategy.readByte(): End of stream reached while trying to read the data");
         throw new IOException("End of stream reached while trying to read the data");
         }
      }

   return null;
   }
   }
