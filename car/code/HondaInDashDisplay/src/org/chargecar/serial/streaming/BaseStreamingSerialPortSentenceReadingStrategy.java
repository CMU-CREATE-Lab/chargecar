package org.chargecar.serial.streaming;

import java.io.IOException;
import edu.cmu.ri.createlab.serial.SerialPortIOHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class BaseStreamingSerialPortSentenceReadingStrategy implements StreamingSerialPortSentenceReadingStrategy
   {
   private static final Log LOG = LogFactory.getLog(BaseStreamingSerialPortSentenceReadingStrategy.class);

   private final SerialPortIOHelper serialPortIoHelper;

   protected BaseStreamingSerialPortSentenceReadingStrategy(final SerialPortIOHelper serialPortIoHelper)
      {
      this.serialPortIoHelper = serialPortIoHelper;
      }

   protected final SerialPortIOHelper getSerialPortIoHelper()
      {
      return serialPortIoHelper;
      }

   public final boolean isDataAvailable() throws IOException
      {
      return serialPortIoHelper.isDataAvailable();
      }

   /**
    * Reads the next byte if data is available.  If no data is available, this method returns <code>null</code>.  Throws
    * an {@link IOException} if the end of stream is reached while trying to read the data.
    */
   protected final Byte readByte() throws IOException
      {
      if (serialPortIoHelper.isDataAvailable())
         {
         final int b = serialPortIoHelper.read();

         if (b >= 0)
            {
            return (byte)b;
            }
         else
            {
            LOG.error("StreamingSerialPortReader$StreamingSerialPortSentenceParser.readByte(): End of stream reached while trying to read the data");
            throw new IOException("End of stream reached while trying to read the data");
            }
         }

      return null;
      }
   }
