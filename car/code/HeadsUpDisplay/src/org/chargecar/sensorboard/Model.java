package org.chargecar.sensorboard;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;

/**
 * <p>
 * <code>Model</code> provides common functionality for all sensor board model classes.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
abstract class Model<T, U>
   {
   private List<EventListener<U>> eventListeners = new ArrayList<EventListener<U>>();
   private ExecutorService executorService = Executors.newSingleThreadExecutor(new DaemonThreadFactory(this.getClass().getSimpleName()));

   public final synchronized void addEventListener(final EventListener<U> listener)
      {
      if (listener != null)
         {
         eventListeners.add(listener);
         }
      }

   /**
    * Publishes the given object to all registered {@link EventListener}s.  Publication is performed in a separate
    * thread so that control can quickly be returned to the caller.  Published objects are guaranteed to be received by
    * the listeners in the same order in which they were published.
    */
   protected final synchronized void publishEventToListeners(final U obj)
      {
      if (!eventListeners.isEmpty())
         {
         executorService.execute(
               new Runnable()
               {
               public void run()
                  {
                  for (final EventListener<U> listener : eventListeners)
                     {
                     listener.handleEvent(obj);
                     }
                  }
               });
         }
      }

   public abstract void update(final T data);
   }