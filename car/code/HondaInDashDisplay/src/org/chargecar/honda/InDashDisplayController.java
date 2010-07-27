package org.chargecar.honda;

import edu.cmu.ri.createlab.util.runtime.LifecycleManager;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class InDashDisplayController
   {
   private final LifecycleManager lifecycleManager;

   InDashDisplayController(final LifecycleManager lifecycleManager)
      {
      this.lifecycleManager = lifecycleManager;
      }

   public void shutdown()
      {
      lifecycleManager.shutdown();
      }
   }
