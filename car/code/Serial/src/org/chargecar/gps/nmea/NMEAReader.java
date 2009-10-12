package org.chargecar.gps.nmea;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.gps.GPSEventListener;
import org.chargecar.serial.DefaultSerialPortIOHelper;
import org.chargecar.serial.SerialPortEnumerator;
import org.chargecar.serial.SerialPortException;
import org.chargecar.serial.SerialPortIOHelper;
import org.chargecar.serial.config.BaudRate;
import org.chargecar.serial.config.CharacterSize;
import org.chargecar.serial.config.FlowControl;
import org.chargecar.serial.config.Parity;
import org.chargecar.serial.config.SerialIOConfiguration;
import org.chargecar.serial.config.StopBits;
import org.chargecar.util.DaemonThreadFactory;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class NMEAReader
   {
   private static final Log LOG = LogFactory.getLog(NMEAReader.class);

   private static final int OPEN_PORT_TIMEOUT_MILLIS = 1000;
   private static final int READ_TIMEOUT_MILLIS = 1000;
   private static final int RECEIVE_TIMEOUT_MILLIS = 1000;

   private static int convertParity(final Parity parity)
      {
      switch (parity)
         {
         case NONE:
            return SerialPort.PARITY_NONE;
         case EVEN:
            return SerialPort.PARITY_EVEN;
         case ODD:
            return SerialPort.PARITY_ODD;

         default:
            throw new IllegalArgumentException("Unexpected Parity [" + parity + "]");
         }
      }

   private final String applicationName;
   private final Set<GPSEventListener> eventListeners = new HashSet<GPSEventListener>();
   private SerialPort port;
   private SerialPortIOHelper ioHelper;
   private ScheduledExecutorService executor;
   private ScheduledFuture<?> scheduledFuture;

   public NMEAReader(final String applicationName)
      {
      this.applicationName = applicationName;
      }

   public void addEventListener(final GPSEventListener listener)
      {
      if (listener != null)
         {
         eventListeners.add(listener);
         }
      }

   public void connect(final String serialPortName) throws SerialPortException, IOException
      {
      LOG.debug("NMEAReader.connect(" + serialPortName + ")");
      final SerialIOConfiguration config = new SerialIOConfiguration(serialPortName,
                                                                     BaudRate.BAUD_4800,
                                                                     CharacterSize.EIGHT,
                                                                     Parity.NONE,
                                                                     StopBits.ONE,
                                                                     FlowControl.NONE);

      LOG.debug("NMEAReader.connect(" + serialPortName + "): Calling SerialPortEnumerator.getSerialPortIdentifer(" + config.getPortDeviceName() + ")");
      final CommPortIdentifier portIdentifier = SerialPortEnumerator.getSerialPortIdentifer(config.getPortDeviceName());
      LOG.debug("NMEAReader.connect(" + serialPortName + "): Done calling SerialPortEnumerator.getSerialPortIdentifer(" + config.getPortDeviceName() + "), portIdentifier = " + portIdentifier);

      if (portIdentifier != null)
         {
         port = null;

         try
            {
            // try to open the port
            LOG.debug("NMEAReader.connect(): opening serial port");
            port = (SerialPort)portIdentifier.open(applicationName, OPEN_PORT_TIMEOUT_MILLIS);
            LOG.debug("NMEAReader.connect(): done opening serial port = " + port);

            // now configure the port
            if (port != null)
               {
               port.setSerialPortParams(config.getBaudRate().getValue(),
                                        config.getCharacterSize().getValue(),
                                        config.getStopBits().getValue(),
                                        convertParity(config.getParity()));

               // set the flow control
               if (FlowControl.HARDWARE.equals(config.getFlowControl()))
                  {
                  port.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
                  port.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT);
                  }
               else if (FlowControl.SOFTWARE.equals(config.getFlowControl()))
                  {
                  port.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN);
                  port.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_OUT);
                  }
               else
                  {
                  port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                  }

               // try to set the receive timeout
               if (LOG.isDebugEnabled())
                  {
                  LOG.debug("NMEAReader.connect(): Setting serial port receive timeout to " + RECEIVE_TIMEOUT_MILLIS + "...");
                  }
               port.enableReceiveTimeout(RECEIVE_TIMEOUT_MILLIS);
               if (LOG.isDebugEnabled())
                  {
                  LOG.debug("NMEAReader.connect(): Check whether setting serial port receive timeout worked: (is enabled=" + port.isReceiveTimeoutEnabled() + ",timeout=" + port.getReceiveTimeout() + ")");
                  }

               ioHelper = new DefaultSerialPortIOHelper(new BufferedInputStream(port.getInputStream()),
                                                        new BufferedOutputStream(port.getOutputStream()));
               executor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("NMEAReader.executor"));
               }
            }
         catch (PortInUseException e)
            {
            throw new SerialPortException("Failed to open serial port [" + config.getPortDeviceName() + "] because it is already in use", e);
            }
         catch (UnsupportedCommOperationException e)
            {
            port.close();
            throw new SerialPortException("Failed to configure serial port [" + config.getPortDeviceName() + "]", e);
            }
         }
      else
         {
         throw new SerialPortException("Failed to obtain the serial port [" + config.getPortDeviceName() + "].  Make sure that it exists and is not in use by another process.");
         }
      }

   public void startReading()
      {
      scheduledFuture = executor.scheduleAtFixedRate(new NMEASentenceParser(ioHelper, eventListeners), 0, 500, TimeUnit.MILLISECONDS);
      }

   public void stopReading()
      {
      if (scheduledFuture != null && scheduledFuture.cancel(true))
         {
         scheduledFuture = null;
         }
      }

   public void disconnect()
      {
      LOG.debug("NMEAReader.disconnect()");

      stopReading();

      // shut down the command queue
      try
         {
         LOG.debug("NMEAReader.disconnect(): Shutting down the serial port command execution queue");
         final List<Runnable> unexecutedTasks = executor.shutdownNow();
         LOG.debug("NMEAReader.disconnect(): Unexecuted tasks: " + (unexecutedTasks == null ? 0 : unexecutedTasks.size()));
         LOG.debug("NMEAReader.disconnect(): Waiting for the serial port command execution queue to shutdown.");
         executor.awaitTermination(10, TimeUnit.SECONDS);
         executor = null;
         LOG.debug("NMEAReader.disconnect(): Serial port command execution queue successfully shutdown");
         }
      catch (Exception e)
         {
         LOG.error("NMEAReader.disconnect(): Exception while trying to shut down the serial port command execution queue", e);
         }

      // shut down the serial port
      try
         {
         LOG.debug("NMEAReader.disconnect(): Now attempting to close the serial port...");
         port.close();
         port = null;
         ioHelper = null;
         LOG.debug("NMEAReader.disconnect(): Serial port closed successfully.");
         }
      catch (Exception e)
         {
         LOG.error("NMEAReader.disconnect(): Exception while trying to close the serial port", e);
         }
      }

   private interface NMEASentenceProcessor
      {
      void process(final String sentence, final Set<GPSEventListener> eventListeners);
      }

   private static class NMEASentenceParser implements Runnable
      {
      private static final String NMEA_BOL = "$";
      private static final String NMEA_EOL = "\r\n";
      private static final String NMEA_WORD_DELIMITER = ",";
      private static final String DEGREES_SYMBOL = "\u00b0";
      private static final String GPS_FIX_DATA_SENTENCE_PREFIX = "$GPGGA";
      private static final String GARMIN_ALTITUDE_SENTENCE_PREFIX = "$PGRMZ";
      private final SerialPortIOHelper ioHelper;
      private final Set<GPSEventListener> eventListeners;
      private String currentData = "";
      private static final Map<String, NMEASentenceProcessor> SENTENCE_PROCESSORS = new HashMap<String, NMEASentenceProcessor>();

      static
         {
         SENTENCE_PROCESSORS.put(GPS_FIX_DATA_SENTENCE_PREFIX,
                                 new NMEASentenceProcessor()
                                 {
                                 public void process(final String sentence, final Set<GPSEventListener> eventListeners)
                                    {
                                    final Scanner s = new Scanner(sentence).useDelimiter(NMEA_WORD_DELIMITER);
                                    s.next();  // ignore the time
                                    final String lat1 = s.next();  // latitude
                                    final String lat2 = s.next();  // latitude direction
                                    final String latitude = lat2 + " " + lat1.substring(0, 2) + DEGREES_SYMBOL + lat1.substring(2) + "'";

                                    final String long1 = s.next();  // longitude
                                    final String long2 = s.next();  // longitude direction
                                    final String longitude = long2 + long1.substring(0, 3) + DEGREES_SYMBOL + long1.substring(3) + "'";
                                    s.next();  // ignore the fix quality
                                    final int numSatellites = s.nextInt();  // number of satellites being tracked
                                    for (final GPSEventListener listener : eventListeners)
                                       {
                                       listener.handleLocationEvent(latitude, longitude, numSatellites);
                                       }
                                    }
                                 });
         SENTENCE_PROCESSORS.put(GARMIN_ALTITUDE_SENTENCE_PREFIX,
                                 new NMEASentenceProcessor()
                                 {
                                 public void process(final String sentence, final Set<GPSEventListener> eventListeners)
                                    {
                                    final Scanner s = new Scanner(sentence).useDelimiter(NMEA_WORD_DELIMITER);
                                    final int elevationInFeet = s.nextInt();  // number of satellites being tracked
                                    for (final GPSEventListener listener : eventListeners)
                                       {
                                       listener.handleElevationEvent(elevationInFeet);
                                       }
                                    }
                                 });
         }

      private NMEASentenceParser(final SerialPortIOHelper ioHelper, final Set<GPSEventListener> eventListeners)
         {
         this.ioHelper = ioHelper;
         this.eventListeners = eventListeners;
         }

      public void run()
         {
         final String dataRead = read();

         String data = currentData + (dataRead == null ? "" : dataRead);

         // slurp up any characters before the start of the data
         int startPos = data.indexOf(NMEA_BOL);
         if (currentData.length() == 0)
            {
            if (startPos >= 0)
               {
               data = data.substring(startPos);
               }
            else
               {
               return;
               }
            }

         startPos = data.indexOf(NMEA_BOL);
         final int endPos = data.lastIndexOf(NMEA_EOL);
         if (endPos >= 0)
            {
            processSentences(data.substring(startPos, endPos));
            if (data.endsWith(NMEA_EOL))
               {
               currentData = "";
               }
            else
               {
               currentData = data.substring(endPos + NMEA_EOL.length());
               }
            }
         else
            {
            currentData = data.substring(startPos);
            }
         }

      private void processSentences(final String sentences)
         {
         final Scanner s = new Scanner(sentences).useDelimiter(NMEA_EOL);
         while (s.hasNext())
            {
            processSentence(s.next());
            }
         }

      private void processSentence(final String sentence)
         {
         if (sentence != null)
            {
            if (sentence.lastIndexOf('$') > 0)
               {
               LOG.error("ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR-ERROR");
               LOG.debug("??????????????: " + sentence);
               }
            else
               {
               final int firstDelimiter = sentence.indexOf(NMEA_WORD_DELIMITER);
               final String command = sentence.substring(0, firstDelimiter);
               final NMEASentenceProcessor sentenceProcessor = SENTENCE_PROCESSORS.get(command);
               if (sentenceProcessor != null)
                  {
                  sentenceProcessor.process(sentence.substring(firstDelimiter + 1), eventListeners);
                  }
               }
            }
         }

      private String read()
         {
         try
            {
            final int numBytesToRead = ioHelper.available();
            if (numBytesToRead <= 0)
               {
               return null;
               }

            // create a buffer to read the data into
            final byte[] data = new byte[numBytesToRead];

            int numBytesRead = 0;

            // define the ending time
            final long endTime = READ_TIMEOUT_MILLIS + System.currentTimeMillis();
            while ((numBytesRead < numBytesToRead) && (System.currentTimeMillis() <= endTime))
               {
               if (ioHelper.isDataAvailable())
                  {
                  try
                     {
                     final int c = ioHelper.read();

                     if (c >= 0)
                        {
                        data[numBytesRead++] = (byte)c;
                        }
                     else
                        {
                        LOG.error("NMEAReader$NMEASentenceParser.read(): End of stream reached while trying to read the data");
                        return null;
                        }
                     }
                  catch (IOException e)
                     {
                     LOG.error("NMEAReader$NMEASentenceParser.read(): IOException while trying to read the data", e);
                     return null;
                     }
                  }
               }

            // Now compare the amount of data read with what the caller expected.  If it's less, then make a copy of the
            // array containing only the bytes actually read and return that.
            if (numBytesRead < numBytesToRead)
               {
               LOG.debug("NMEAReader$NMEASentenceParser.read(): only read [" + numBytesRead + "] bytes, was expecting to read [" + numBytesToRead + "]");
               final byte[] dataSubset = new byte[numBytesRead];
               System.arraycopy(data, 0, dataSubset, 0, numBytesRead);

               return new String(dataSubset);
               }

            return new String(data);
            }
         catch (IOException e)
            {
            LOG.error("NMEAReader$NMEASentenceParser.read(): IOException while reading the data", e);
            }

         return null;
         }
      }
   }
