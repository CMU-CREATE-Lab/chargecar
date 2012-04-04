package org.chargecar.serial.streaming;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import edu.cmu.ri.createlab.util.runtime.LifecycleManager;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.log4j.Logger;
import org.chargecar.honda.StreamingSerialPortDeviceController;
import org.chargecar.honda.StreamingSerialPortDeviceModel;
import org.chargecar.honda.StreamingSerialPortDeviceView;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class StreamingSerialPortDeviceManager<S, T>
   {
   private static final Logger LOG = Logger.getLogger(StreamingSerialPortDeviceManager.class);

   private T eventData = null;
   private final StreamingSerialPortDeviceModel<S, T> model;
   private final MyLifecycleManager<S,T> lifecycleManager;

   public StreamingSerialPortDeviceManager(final StreamingSerialPortDeviceModel<S, T> model,
                                           final StreamingSerialPortDeviceController<S, T> controller)
      {
      if (model == null)
         {
         throw new IllegalArgumentException("The model cannot be null.");
         }
      if (controller == null)
         {
         throw new IllegalArgumentException("The controller cannot be null.");
         }

      if (LOG.isDebugEnabled())
         {
         LOG.debug("StreamingSerialPortDeviceManager.StreamingSerialPortDeviceManager(): creating StreamingSerialPortDeviceManager for model [" + model + "] and controller [" + controller + "]");
         }

      this.model = model;
      model.addEventListener(
            new StreamingSerialPortDeviceView<T>()
            {
            protected void handleEventInGUIThread(final T eventData)
               {
               StreamingSerialPortDeviceManager.this.eventData = eventData;
               }
            });
      model.addStreamingSerialPortDeviceConnectionStateListener(
            new StreamingSerialPortDeviceConnectionStateListener()
            {
            public void handleConnectionStateChange(final boolean isConnected)
               {
               if (isConnected)
                  {
                  controller.startReading();
                  }
               }
            });

      lifecycleManager = new MyLifecycleManager<S,T>(controller);
      lifecycleManager.startup();
      }

   public final T getData()
      {
      return eventData;
      }

   public final boolean isConnected()
      {
      return model.isConnected();
      }

   public final boolean isReading()
      {
      return model.isReading();
      }

   public final void startup()
      {
      lifecycleManager.startup();
      }

   public final void shutdown()
      {
      lifecycleManager.shutdown();
      }
   
   private static class MyLifecycleManager<S,T> implements LifecycleManager
      {
      private final Runnable startupRunnable;
      private final Runnable shutdownRunnable;
      private final ExecutorService executor = Executors.newCachedThreadPool(new DaemonThreadFactory("StreamingSerialPortDeviceManager$MyLifecycleManager"));

      private MyLifecycleManager(final StreamingSerialPortDeviceController<S, T> controller)
         {
         startupRunnable =
               new Runnable()
               {
               public void run()
                  {
                  if (LOG.isInfoEnabled())
                     {
                     LOG.info("StreamingSerialPortDeviceManager$MyLifecycleManager.run(): Attempting to establish a connection to the [" + controller + "] controller");
                     }
                  controller.connect();
                  }
               };

         shutdownRunnable =
               new Runnable()
               {
               public void run()
                  {
                  if (LOG.isInfoEnabled())
                     {
                     LOG.info("StreamingSerialPortDeviceManager$MyLifecycleManager.run(): Disconnecting from the [" + controller + "] controller");
                     }
                  controller.disconnect();
                  }
               };
         }

      public void startup()
         {
         LOG.debug("StreamingSerialPortDeviceManager$MyLifecycleManager.startup()");

         executor.submit(startupRunnable);
         }

      public void shutdown()
         {
         LOG.debug("StreamingSerialPortDeviceManager$MyLifecycleManager.shutdown()");

         executor.submit(shutdownRunnable);
         }
      }
   }
