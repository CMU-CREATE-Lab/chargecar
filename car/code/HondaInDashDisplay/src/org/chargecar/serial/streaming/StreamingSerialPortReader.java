package org.chargecar.serial.streaming;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import edu.cmu.ri.createlab.serial.SerialPortException;
import edu.cmu.ri.createlab.serial.SerialPortIOHelper;
import edu.cmu.ri.createlab.serial.config.SerialIOConfiguration;
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
   private ScheduledExecutorService executor;
   private ScheduledFuture<?> scheduledFuture;
   private final Set<StreamingSerialPortEventListener<E>> eventListeners = new HashSet<StreamingSerialPortEventListener<E>>();

   public StreamingSerialPortReader(final String applicationName, final SerialIOConfiguration config)
      {
      this(new DefaultSerialIOManager(applicationName, config));
      }

   public StreamingSerialPortReader(final SerialIOManager serialIIOManager)
      {
      this.serialIIOManager = serialIIOManager;
      }

   public final void addEventListener(final StreamingSerialPortEventListener<E> listener)
      {
      if (listener != null)
         {
         eventListeners.add(listener);
         }
      }

   public final void removeEventListener(final StreamingSerialPortEventListener<E> listener)
      {
      if (listener != null)
         {
         eventListeners.remove(listener);
         }
      }

   @SuppressWarnings({"ConstantConditions"})
   public final void publishEvent(final E event)
      {
      if (!eventListeners.isEmpty())
         {
         for (final StreamingSerialPortEventListener<E> listener : eventListeners)
            {
            try
               {
               listener.handleEvent(event);
               }
            catch (Exception e)
               {
               LOG.error("StreamingSerialPortReader.publishEvent(): Exception thrown by StreamingSerialPortEventListener", e);
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
         return true;
         }

      return false;
      }

   public final void startReading()
      {
      final StreamingSerialPortSentenceReadingStrategy sentenceReadingStrategy = createStreamingSerialPortSentenceReadingStrategy(serialIIOManager.getSerialPortIoHelper());

      LOG.debug("StreamingSerialPortReader.startReading(): Scheduling the SentenceReader for execution");
      scheduledFuture = executor.schedule(new SentenceReader(sentenceReadingStrategy), 0, TimeUnit.MILLISECONDS);
      try
         {
         scheduledFuture.get();
         LOG.debug("StreamingSerialPortReader.startReading(): SentenceReader has finished executing");
         }
      catch (CancellationException e)
         {
         // TODO: test cancelling!
         LOG.error("CancellationException while waiting for the SentenceReader to finish executing", e);
         }
      catch (InterruptedException e)
         {
         LOG.error("InterruptedException while waiting for the SentenceReader to finish executing", e);
         }
      catch (ExecutionException e)
         {
         LOG.error("ExecutionException while waiting for the SentenceReader to finish executing", e);
         }
      }

   protected abstract StreamingSerialPortSentenceReadingStrategy createStreamingSerialPortSentenceReadingStrategy(final SerialPortIOHelper serialPortIoHelper);

   protected abstract void processSentence(final Date timestamp, final byte[] sentence);

   public final void stopReading()
      {
      if (scheduledFuture != null && scheduledFuture.cancel(true))
         {
         scheduledFuture = null;
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
      private final StreamingSerialPortSentenceReadingStrategy sentenceReadingStrategy;

      private SentenceReader(final StreamingSerialPortSentenceReadingStrategy sentenceReadingStrategy)
         {
         this.sentenceReadingStrategy = sentenceReadingStrategy;
         }

      public void run()
         {
         // slurp up the first sentence, since it's likely a fragment
         sentenceReadingStrategy.getNextSentence();

         byte[] sentence;
         while ((sentence = sentenceReadingStrategy.getNextSentence()) != null)
            {
            processSentence(new Date(), sentence);
            }

         LOG.debug("StreamingSerialPortReader$SentenceReader.run(): getNextSentence() returned null, so I'm done reading");
         }
      }
   }
