package org.chargecar.serial.streaming;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import edu.cmu.ri.createlab.serial.SerialDeviceIOHelper;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class FakeStreamingSerialDevice implements SerialIOManager
   {
   private final SerialDeviceIOHelper SerialDeviceIOHelper;

   protected FakeStreamingSerialDevice(final String fakeData)
      {
      this(new BufferedInputStream(new ByteArrayInputStream(fakeData.getBytes())));
      }

   protected FakeStreamingSerialDevice(final InputStream inputStream)
      {
      SerialDeviceIOHelper = new MySerialDeviceIOHelper(new BufferedInputStream(inputStream));
      }

   public final boolean connect()
      {
      return true;
      }

   public final SerialDeviceIOHelper getSerialDeviceIOHelper()
      {
      return SerialDeviceIOHelper;
      }

   @SuppressWarnings({"NoopMethodInAbstractClass"})
   public final void disconnect()
      {
      // nothing to do
      }

   private final class MySerialDeviceIOHelper implements SerialDeviceIOHelper
      {
      private BufferedInputStream inputStream;

      private MySerialDeviceIOHelper(final BufferedInputStream bufferedInputStream)
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
