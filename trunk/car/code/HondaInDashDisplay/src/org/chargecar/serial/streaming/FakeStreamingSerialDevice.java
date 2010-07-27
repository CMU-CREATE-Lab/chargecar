package org.chargecar.serial.streaming;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import edu.cmu.ri.createlab.serial.SerialPortIOHelper;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class FakeStreamingSerialDevice implements SerialIOManager
   {
   private final SerialPortIOHelper serialPortIOHelper;

   protected FakeStreamingSerialDevice(final String fakeData)
      {
      this(new BufferedInputStream(new ByteArrayInputStream(fakeData.getBytes())));
      }

   protected FakeStreamingSerialDevice(final InputStream inputStream)
      {
      serialPortIOHelper = new MySerialPortIOHelper(new BufferedInputStream(inputStream));
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

      private MySerialPortIOHelper(final BufferedInputStream bufferedInputStream)
         {
         inputStream = bufferedInputStream;
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
