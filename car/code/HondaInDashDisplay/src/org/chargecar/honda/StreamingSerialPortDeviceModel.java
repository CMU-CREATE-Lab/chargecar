package org.chargecar.honda;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import edu.cmu.ri.createlab.util.mvc.Model;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.chargecar.serial.streaming.StreamingSerialPortDeviceConnectionStateListener;
import org.chargecar.serial.streaming.StreamingSerialPortDeviceConnectionStatePublisher;
import org.chargecar.serial.streaming.StreamingSerialPortDeviceReadingStateListener;
import org.chargecar.serial.streaming.StreamingSerialPortDeviceReadingStatePublisher;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public abstract class StreamingSerialPortDeviceModel<T, U> extends Model<T, U> implements StreamingSerialPortDeviceConnectionStatePublisher,
                                                                                          StreamingSerialPortDeviceReadingStatePublisher
   {
   private List<StreamingSerialPortDeviceConnectionStateListener> connectionStateListeners = new ArrayList<StreamingSerialPortDeviceConnectionStateListener>();
   private List<StreamingSerialPortDeviceReadingStateListener> readingStateListeners = new ArrayList<StreamingSerialPortDeviceReadingStateListener>();
   private ExecutorService executorService = Executors.newSingleThreadExecutor(new DaemonThreadFactory(this.getClass().getSimpleName()));
   private boolean isConnected = false;
   private boolean isReading = false;

   public final synchronized void addStreamingSerialPortDeviceConnectionStateListener(final StreamingSerialPortDeviceConnectionStateListener listener)
      {
      if (listener != null)
         {
         connectionStateListeners.add(listener);
         }
      }

   public final synchronized void removeStreamingSerialPortDeviceConnectionStateListener(final StreamingSerialPortDeviceConnectionStateListener listener)
      {
      if (listener != null)
         {
         connectionStateListeners.remove(listener);
         }
      }

   public final synchronized void addStreamingSerialPortDeviceReadingStateListener(final StreamingSerialPortDeviceReadingStateListener listener)
      {
      if (listener != null)
         {
         readingStateListeners.add(listener);
         }
      }

   public final synchronized void removeStreamingSerialPortDeviceReadingStateListener(final StreamingSerialPortDeviceReadingStateListener listener)
      {
      if (listener != null)
         {
         readingStateListeners.remove(listener);
         }
      }

   /**
    * Publishes the connection state to all registered {@link StreamingSerialPortDeviceConnectionStateListener}s.  Publication is
    * performed in a separate thread so that control can quickly be returned to the caller.
    */
   public final synchronized void setConnectionState(final boolean isConnected)
      {
      this.isConnected = isConnected;
      if (!connectionStateListeners.isEmpty())
         {
         executorService.execute(
               new Runnable()
               {
               public void run()
                  {
                  for (final StreamingSerialPortDeviceConnectionStateListener listener : connectionStateListeners)
                     {
                     listener.handleConnectionStateChange(isConnected);
                     }
                  }
               });
         }
      }

   public final synchronized boolean isConnected()
      {
      return isConnected;
      }

   /**
    * Publishes the reading state to all registered {@link StreamingSerialPortDeviceReadingStateListener}s.  Publication is
    * performed in a separate thread so that control can quickly be returned to the caller.
    */
   public final synchronized void setReadingState(final boolean isReading)
      {
      this.isReading = isReading;

      if (!readingStateListeners.isEmpty())
         {
         executorService.execute(
               new Runnable()
               {
               public void run()
                  {
                  for (final StreamingSerialPortDeviceReadingStateListener listener : readingStateListeners)
                     {
                     listener.handleReadingStateChange(isReading);
                     }
                  }
               });
         }
      }

   public final synchronized boolean isReading()
      {
      return isReading;
      }
   }
