package org.chargecar.honda.sensorboard;

import java.io.IOException;
import edu.cmu.ri.createlab.serial.SerialPortIOHelper;
import org.chargecar.serial.streaming.SerialIOManager;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class FakeSensorBoard implements SerialIOManager
   {
   private final SerialPortIOHelper serialPortIOHelper = new MySerialPortIOHelper();

   public boolean connect()
      {
      return true;
      }

   public SerialPortIOHelper getSerialPortIoHelper()
      {
      return serialPortIOHelper;
      }

   public void disconnect()
      {
      // nothing to do
      }

   private final class MySerialPortIOHelper implements SerialPortIOHelper
      {
      private static final String DATA = "!,M=83.98,C=84.96,T=203,R=201\r" +
                                         "!,M=85.45,C=83.50,T=195,R=195\r" +
                                         "!,M=84.96,C=82.52,T=193,R=193\r" +
                                         "!,M=85.45,C=82.03,T=192,R=192\r" +
                                         "!,M=83.98,C=83.98,T=190,R=191\r" +
                                         "!,M=84.96,C=83.98,T=190,R=191\r" +
                                         "!,M=83.98,C=83.01,T=191,R=191\r" +
                                         "!,M=85.45,C=83.50,T=190,R=191\r" +
                                         "!,M=85.94,C=83.98,T=190,R=190\r" +
                                         "!,M=83.98,C=83.50,T=189,R=190\r" +
                                         "!,M=83.98,C=83.01,T=189,R=189\r" +
                                         "!,M=84.96,C=83.98,T=189,R=190\r" +
                                         "!,M=83.50,C=82.03,T=190,R=190\r" +
                                         "!,M=83.50,C=82.52,T=190,R=191\r" +
                                         "!,M=83.98,C=83.98,T=190,R=190\r" +
                                         "!,M=84.47,C=83.50,T=189,R=190\r" +
                                         "!,M=83.98,C=82.52,T=190,R=190\r" +
                                         "!,M=83.50,C=83.01,T=189,R=190\r" +
                                         "!,M=83.98,C=83.01,T=191,R=191\r" +
                                         "!,M=83.98,C=83.50,T=189,R=190\r" +
                                         "!,M=84.47,C=83.98,T=189,R=190\r" +
                                         "!,M=83.98,C=82.52,T=190,R=190\r" +
                                         "!,M=83.98,C=82.03,T=190,R=191\r" +
                                         "!,M=83.50,C=82.52,T=191,R=191\r" +
                                         "!,M=84.47,C=83.98,T=189,R=190\r" +
                                         "!,M=85.45,C=83.98,T=189,R=190\r" +
                                         "!,M=83.98,C=83.01,T=190,R=191\r" +
                                         "!,M=83.98,C=84.47,T=190,R=190\r" +
                                         "!,M=84.96,C=83.01,T=189,R=190\r" +
                                         "!,M=85.94,C=84.47,T=189,R=190\r" +
                                         "!,M=83.98,C=83.98,T=189,R=190\r" +
                                         "!,M=83.98,C=83.01,T=191,R=191\r" +
                                         "!,M=83.98,C=83.01,T=192,R=192\r" +
                                         "!,M=84.96,C=83.01,T=192,R=192\r" +
                                         "!,M=85.45,C=82.52,T=191,R=191\r" +
                                         "!,M=84.96,C=83.50,T=190,R=191\r" +
                                         "!,M=84.47,C=83.98,T=189,R=190\r" +
                                         "!,M=84.47,C=83.50,T=189,R=190\r";

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
         final char c = DATA.charAt(currentCharacter);

         // increment the pointer, being careful to wrap-around
         currentCharacter++;
         if (currentCharacter >= DATA.length())
            {
            currentCharacter = 0;
            }

         return c;
         }
      }
   }
