package org.chargecar.serial.streaming;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import edu.cmu.ri.createlab.serial.SerialPortException;
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

   private final char sentenceDelimiter;
   private final SerialIOManager serialIIOManager;
   private ScheduledExecutorService executor;
   private ScheduledFuture<?> scheduledFuture;
   private final Set<StreamingSerialPortEventListener<E>> eventListeners = new HashSet<StreamingSerialPortEventListener<E>>();

   public StreamingSerialPortReader(final char sentenceDelimiter,
                                    final String applicationName,
                                    final SerialIOConfiguration config)
      {
      this(sentenceDelimiter, new DefaultSerialIOManager(applicationName, config));
      }

   public StreamingSerialPortReader(final char sentenceDelimiter,
                                    final SerialIOManager serialIIOManager)
      {
      this.sentenceDelimiter = sentenceDelimiter;
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
      scheduledFuture = executor.scheduleAtFixedRate(new StreamingSerialPortSentenceParser(), 0, 1, TimeUnit.MILLISECONDS);
      }

   protected abstract void processSentence(final String sentence);

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

   /**
    * @author Chris Bartley (bartley@cmu.edu)
    */
   final class StreamingSerialPortSentenceParser implements Runnable
      {
      private boolean inSlurpMode = true;
      private StringBuilder sentence = new StringBuilder();

      public final void run()
         {
         final Byte b = readByte();

         if (b == null)
            {
            LOG.debug("StreamingSerialPortReader$StreamingSerialPortSentenceParser.run(): ignoring null byte");
            }
         else
            {
            // see whether we've found the sentence delimiter
            if (b.equals((byte)sentenceDelimiter))
               {
               // if we're in slurp mode, finding the sentence delimiter means we can get out of slurp mode, otherwise
               // it means we've found the end of sentence and can process it
               if (inSlurpMode)
                  {
                  inSlurpMode = false;
                  }
               else
                  {
                  // process sentence
                  processSentence(sentence.toString());

                  // start building a new sentence
                  sentence = new StringBuilder();
                  }
               }
            else
               {
               // if we're in slurp mode, then we just ignore this byte, otherwise add it to the sentence we're building
               if (!inSlurpMode)
                  {
                  sentence.append((char)b.byteValue());
                  }
               }
            }
         }

      private Byte readByte()
         {
         try
            {
            if (serialIIOManager.getSerialPortIoHelper().isDataAvailable())
               {
               final int b = serialIIOManager.getSerialPortIoHelper().read();

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
   }
