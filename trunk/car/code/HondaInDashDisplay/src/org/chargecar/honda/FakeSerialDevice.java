package org.chargecar.honda;

import java.io.IOException;
import edu.cmu.ri.createlab.serial.SerialPortIOHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.serial.streaming.SerialIOManager;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class FakeSerialDevice implements SerialIOManager
   {
   private static final Log LOG = LogFactory.getLog(FakeSerialDevice.class);

   private final SerialPortIOHelper serialPortIOHelper = new MySerialPortIOHelper();
   private final String fakeData;

   protected FakeSerialDevice(final String fakeData)
      {
      LOG.debug("FakeSerialDevice.FakeSerialDevice(): fake data length = [" + (fakeData == null ? "null" : fakeData.length()) + "]");
      this.fakeData = fakeData;
      }

   public final boolean connect()
      {
      return true;
      }

   public final SerialPortIOHelper getSerialPortIoHelper()
      {
      return serialPortIOHelper;
      }

   @SuppressWarnings({"NoopMethodInAbstractClass"})
   public final void disconnect()
      {
      // nothing to do
      }

   private final class MySerialPortIOHelper implements SerialPortIOHelper
      {
      private int currentCharacter = 0;

      public int available()
         {
         return 1;
         }

      public boolean isDataAvailable()
         {
         return true;
         }

      public int read()
         {
         return getCharacter();
         }

      public int read(final byte[] buffer)
         {
         buffer[0] = (byte)getCharacter();

         return 1;
         }

      public void write(final byte[] data) throws IOException
         {
         throw new IOException("Write is not supported");
         }

      public char getCharacter()
         {
         // get the character
         final char c = fakeData.charAt(currentCharacter);

         // increment the pointer, being careful to wrap-around
         currentCharacter++;
         if (currentCharacter >= fakeData.length())
            {
            currentCharacter = 0;
            }

         return c;
         }
      }
   }
