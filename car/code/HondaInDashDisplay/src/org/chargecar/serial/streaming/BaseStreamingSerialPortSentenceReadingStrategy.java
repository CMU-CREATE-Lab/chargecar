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

   protected final Byte readByte()
      {
      try
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
               // todo: handle this better
               LOG.error("StreamingSerialPortReader$StreamingSerialPortSentenceParser.readByte(): End of stream reached while trying to read the data");
               }
            }
         }
      catch (IOException ignored)
         {
         LOG.error("StreamingSerialPortReader$StreamingSerialPortSentenceParser.readByte(): IOException while trying to read a byte");
         }

      return null;
      }
   }
