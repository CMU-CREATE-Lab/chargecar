package org.chargecar.sensorboard;

import javax.swing.SwingUtilities;

/**
 * <p>
 * <code>View</code> provides common functionality for all sensor board view classes.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
abstract class View<T> implements EventListener<T>
   {
   protected final void runInGUIThread(final Runnable runnable)
      {
      if (SwingUtilities.isEventDispatchThread())
         {
         runnable.run();
         }
      else
         {
         SwingUtilities.invokeLater(runnable);
         }
      }
   }
