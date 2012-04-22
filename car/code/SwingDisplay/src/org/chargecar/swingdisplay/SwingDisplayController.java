package org.chargecar.swingdisplay;

import edu.cmu.ri.createlab.util.runtime.LifecycleManager;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class SwingDisplayController
   {
   private final LifecycleManager lifecycleManager;

   SwingDisplayController(final LifecycleManager lifecycleManager)
      {
      this.lifecycleManager = lifecycleManager;
      }

   public void shutdown()
      {
      lifecycleManager.shutdown();
      }
   }
