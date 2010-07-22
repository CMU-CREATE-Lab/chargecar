package org.chargecar.honda;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

   private final SerialPortIOHelper serialPortIOHelper;

   protected FakeSerialDevice(final String fakeData)
      {
      LOG.debug("FakeSerialDevice.FakeSerialDevice(): fake data length = [" + (fakeData == null ? "null" : fakeData.length()) + "]");
      serialPortIOHelper = new MySerialPortIOHelper(fakeData);
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
      private BufferedInputStream inputStream;

      private MySerialPortIOHelper(final String fakeData)
         {
         inputStream = new BufferedInputStream(new ByteArrayInputStream(fakeData.getBytes()));
         }

      public int available() throws IOException
         {
         return inputStream.available();
         }

      public boolean isDataAvailable() throws IOException
         {
         return available() > 0;
         }

      public InputStream getInputStream()
         {
         return inputStream;
         }

      public OutputStream getOutputStream()
         {
         throw new UnsupportedOperationException("Write is not supported");
         }

      public int read() throws IOException
         {
         return inputStream.read();
         }

      public int read(final byte[] buffer) throws IOException
         {
         return inputStream.read(buffer);
         }

      public void write(final byte[] data) throws IOException
         {
         throw new IOException("Write is not supported");
         }
      }
   }
