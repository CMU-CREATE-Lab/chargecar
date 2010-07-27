package org.chargecar.honda;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import edu.cmu.ri.createlab.serial.SerialPortException;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.serial.streaming.StreamingSerialPortEventListener;
import org.chargecar.serial.streaming.StreamingSerialPortReader;

/**
 * <p>
 * <code>StreamingSerialPortDeviceController</code> is the base class for all streaming serial port device MVC controllers.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class StreamingSerialPortDeviceController<T, U>
   {
   private static final Log LOG = LogFactory.getLog(StreamingSerialPortDeviceController.class);

   protected static boolean shouldUseFakeDevice()
      {
      return Boolean.valueOf(System.getProperty(HondaConstants.USE_FAKE_DEVICES_SYSTEM_PROPERTY_KEY, "false"));
      }

   private final String deviceName;
   private final StreamingSerialPortReader reader;
   private final StreamingSerialPortDeviceModel<T, U> model;
   private ScheduledFuture<?> scheduledFuture;
   private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("StreamingSerialPortDeviceController.executor"));

   private StreamingSerialPortDeviceController.ConnectionCreator connectionCreator = new ConnectionCreator();

   protected StreamingSerialPortDeviceController(final String deviceName, final StreamingSerialPortReader<T> reader, final StreamingSerialPortDeviceModel<T, U> model)
      {
      this.deviceName = deviceName;
      this.reader = reader;
      this.model = model;

      // register a listener for handling reader events which simply delegates to the model
      if (reader != null)
         {
         reader.addStreamingSerialPortEventListener(
               new StreamingSerialPortEventListener<T>()
               {
               public void handleConnectionStateChange(final boolean isConnected)
                  {
                  if (LOG.isDebugEnabled())
                     {
                     LOG.debug("StreamingSerialPortDeviceController.handleConnectionStateChange(): connection state is now [" + isConnected + "] for device [" + deviceName + "]");
                     }
                  model.setConnectionState(isConnected);
                  }

               public void handleReadingStateChange(final boolean isReading)
                  {
                  if (LOG.isDebugEnabled())
                     {
                     LOG.debug("StreamingSerialPortDeviceController.handleReadingStateChange(): reading state is now [" + isReading + "] for device [" + deviceName + "]");
                     }
                  model.setReadingState(isReading);
                  }

               public void handleDataEvent(final T event)
                  {
                  model.update(event);
                  }
               });
         }
      }

   public final void connect()
      {
      LOG.debug("StreamingSerialPortDeviceController.connect()");

      scheduleConnectionAttempt(0);
      }

   public final void startReading()
      {
      LOG.debug("StreamingSerialPortDeviceController.startReading()");
      if (reader != null && model.isConnected())
         {
         reader.startReading();
         }
      }

   public final void stopReading()
      {
      LOG.debug("StreamingSerialPortDeviceController.stopReading()");
      if (reader != null && model.isConnected())
         {
         reader.stopReading();
         }
      }

   public final void disconnect()
      {
      LOG.debug("StreamingSerialPortDeviceController.disconnect()");

      if (scheduledFuture != null && scheduledFuture.cancel(true))
         {
         scheduledFuture = null;
         }

      stopReading();

      if (reader != null)
         {
         reader.disconnect();
         }
      }

   private void scheduleConnectionAttempt(final int delayInMillis)
      {
      try
         {
         if (LOG.isDebugEnabled())
            {
            LOG.debug("StreamingSerialPortDeviceController.scheduleConnectionAttempt(): scheduling connection to the " + deviceName + " in [" + delayInMillis + "] milliseconds");
            }

         scheduledFuture = executorService.schedule(connectionCreator, delayInMillis, TimeUnit.MILLISECONDS);
         }
      catch (Exception e)
         {
         LOG.error("Exception while trying to schedule an attempt to create a connection to the " + deviceName, e);
         }
      }

   private final class ConnectionCreator implements Runnable
      {
      public void run()
         {
         if (reader != null)
            {
            LOG.debug("StreamingSerialPortDeviceController$ConnectionCreator.run(): creating a connection with the " + deviceName);
            try
               {
               if (reader.connect())
                  {
                  LOG.debug("StreamingSerialPortDeviceController$ConnectionCreator.run(): connection established with the " + deviceName);

                  return;
                  }
               else
                  {
                  LOG.error("StreamingSerialPortDeviceController$ConnectionCreator.run(): failed to connect to the " + deviceName);
                  }
               }
            catch (SerialPortException e)
               {
               LOG.error("SerialPortException while connecting to the " + deviceName, e);
               }
            catch (IOException e)
               {
               LOG.error("IOException while connecting to the " + deviceName, e);
               }
            catch (Exception e)
               {
               LOG.error("Exception while connecting to the " + deviceName, e);
               }

            // schedule another connection attempt
            scheduleConnectionAttempt(1000);
            }
         else
            {
            LOG.info("StreamingSerialPortDeviceController$ConnectionCreator.run(): The StreamingSerialPortReader for the " + deviceName + " is null, so no connection will be attempted.");
            }
         }
      }
   }
