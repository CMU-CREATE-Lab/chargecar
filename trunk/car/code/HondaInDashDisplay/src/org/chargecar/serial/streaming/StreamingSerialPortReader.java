package org.chargecar.serial.streaming;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import edu.cmu.ri.createlab.serial.SerialPortException;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class StreamingSerialPortReader<E> implements StreamingSerialPortEventPublisher<E>
   {
   private static final Log LOG = LogFactory.getLog(StreamingSerialPortReader.class);

   private final SerialIOManager serialIIOManager;
   private final StreamingSerialPortSentenceReadingStrategy sentenceReadingStrategy;
   private ScheduledExecutorService executor;
   private ScheduledFuture<?> scheduledFuture;
   private final Set<StreamingSerialPortEventListener<E>> eventListeners = new HashSet<StreamingSerialPortEventListener<E>>();
   private final byte[] dataSynchronizationLock = new byte[0];
   private boolean isReading = false;

   public StreamingSerialPortReader(final SerialIOManager serialIIOManager, final Character sentenceDelimeter)
      {
      this(serialIIOManager, new DefaultStreamingSerialPortSentenceReadingStrategy(serialIIOManager, sentenceDelimeter));
      }

   public StreamingSerialPortReader(final SerialIOManager serialIIOManager, final StreamingSerialPortSentenceReadingStrategy sentenceReadingStrategy)
      {
      this.serialIIOManager = serialIIOManager;
      this.sentenceReadingStrategy = sentenceReadingStrategy;
      }

   public final void addStreamingSerialPortEventListener(final StreamingSerialPortEventListener<E> listener)
      {
      if (listener != null)
         {
         eventListeners.add(listener);
         }
      }

   public final void removeStreamingSerialPortEventListener(final StreamingSerialPortEventListener<E> listener)
      {
      if (listener != null)
         {
         eventListeners.remove(listener);
         }
      }

   @SuppressWarnings({"ConstantConditions"})
   protected final void publishDataEvent(final E event)
      {
      if (!eventListeners.isEmpty())
         {
         for (final StreamingSerialPortEventListener<E> listener : eventListeners)
            {
            try
               {
               listener.handleDataEvent(event);
               }
            catch (Exception e)
               {
               LOG.error("StreamingSerialPortReader.publishEvent(): Exception thrown by StreamingSerialPortEventListener", e);
               }
            }
         }
      }

   @SuppressWarnings({"ConstantConditions"})
   private void publishConnectionStateChangeEvent(final boolean isConnected)
      {
      if (!eventListeners.isEmpty())
         {
         for (final StreamingSerialPortEventListener<E> listener : eventListeners)
            {
            try
               {
               listener.handleConnectionStateChange(isConnected);
               }
            catch (Exception e)
               {
               LOG.error("StreamingSerialPortReader.publishConnectionStateChangeEvent(): Exception thrown by StreamingSerialPortEventListener", e);
               }
            }
         }
      }

   @SuppressWarnings({"ConstantConditions"})
   private void publishReadingStateChangeEvent(final boolean isReading)
      {
      if (!eventListeners.isEmpty())
         {
         for (final StreamingSerialPortEventListener<E> listener : eventListeners)
            {
            try
               {
               listener.handleReadingStateChange(isReading);
               }
            catch (Exception e)
               {
               LOG.error("StreamingSerialPortReader.publishConnectionStateChangeEvent(): Exception thrown by StreamingSerialPortEventListener", e);
               }
            }
         }
      }

   public final boolean connect() throws SerialPortException, IOException
      {
      LOG.debug("StreamingSerialPortReader.connect()");

      final boolean wasConnectSuccessful = serialIIOManager.connect();

      if (wasConnectSuccessful)
         {
         executor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("StreamingSerialPortReader.executor"));

         publishConnectionStateChangeEvent(true);

         return true;
         }

      return false;
      }

   public final void startReading()
      {
      synchronized (dataSynchronizationLock)
         {
         if (isReading)
            {
            LOG.error("StreamingSerialPortReader.startReading(): already reading, so I'm ignoring this request to start reading.");
            }
         else
            {
            LOG.debug("StreamingSerialPortReader.startReading(): Scheduling the SentenceReader for execution");
            isReading = true;
            scheduledFuture = executor.schedule(new SentenceReader(), 0, TimeUnit.MILLISECONDS);
            }
         }
      }

   protected abstract void processSentence(final Date timestamp, final byte[] sentence);

   public final void stopReading()
      {
      synchronized (dataSynchronizationLock)
         {
         if (isReading)
            {
            if (scheduledFuture != null && scheduledFuture.cancel(true))
               {
               scheduledFuture = null;
               }
            isReading = false;

            publishReadingStateChangeEvent(false);
            }
         else
            {
            LOG.error("StreamingSerialPortReader.stopReading(): not reading, so I'm ignoring this request to stop reading.");
            }
         }
      }

   public final void disconnect()
      {
      LOG.debug("StreamingSerialPortReader.disconnect()");

      stopReading();

      // shut down the command queue
      try
         {
         LOG.debug("StreamingSerialPortReader.disconnect(): Shutting down the serial port command execution queue");
         final List<Runnable> unexecutedTasks = executor.shutdownNow();
         if (LOG.isDebugEnabled())
            {
            LOG.debug("StreamingSerialPortReader.disconnect(): Unexecuted tasks: " + (unexecutedTasks == null ? 0 : unexecutedTasks.size()));
            }
         LOG.debug("StreamingSerialPortReader.disconnect(): Waiting for the serial port command execution queue to shutdown.");
         executor.awaitTermination(10, TimeUnit.SECONDS);
         executor = null;
         LOG.debug("StreamingSerialPortReader.disconnect(): Serial port command execution queue successfully shutdown");
         }
      catch (Exception e)
         {
         LOG.error("StreamingSerialPortReader.disconnect(): Exception while trying to shut down the serial port command execution queue", e);
         }

      // shut down the serial port
      try
         {
         LOG.debug("StreamingSerialPortReader.disconnect(): Telling the SerialIOManager to disconnect...");
         serialIIOManager.disconnect();
         LOG.debug("StreamingSerialPortReader.disconnect(): SerialIOManager disconnected successfully.");
         }
      catch (Exception e)
         {
         LOG.error("StreamingSerialPortReader.disconnect(): Exception while trying to close the serial port", e);
         }
      }

   private final class SentenceReader implements Runnable
      {
      public void run()
         {
         publishReadingStateChangeEvent(true);

         try
            {
            // slurp up the first sentence, since it's likely a fragment
            sentenceReadingStrategy.getNextSentence();

            while (true)
               {
               if (sentenceReadingStrategy.isDataAvailable())
                  {
                  final byte[] sentence = sentenceReadingStrategy.getNextSentence();
                  try
                     {
                     processSentence(new Date(), sentence);
                     }
                  catch (Exception e)
                     {
                     LOG.error("Exception while processing sentence--logging, but otherwise ignoring", e);
                     }
                  synchronized (dataSynchronizationLock)
                     {
                     if (!isReading)
                        {
                        LOG.debug("StreamingSerialPortReader$SentenceReader.run(): reading has been cancelled, so I'm exiting.");
                        return;
                        }
                     }
                  }
               else
                  {
                  try
                     {
                     Thread.sleep(5);
                     }
                  catch (InterruptedException e)
                     {
                     LOG.error("InterruptedException while sleeping", e);
                     }
                  }
               }
            }
         catch (IOException e)
            {
            LOG.error("IOException while reading, so we'll now stop reading", e);
            }
         catch (Exception e)
            {
            LOG.error("Exception while reading, so we'll now stop reading", e);
            }

         synchronized (dataSynchronizationLock)
            {
            isReading = false;
            }

         publishReadingStateChangeEvent(false);
         }
      }
   }
