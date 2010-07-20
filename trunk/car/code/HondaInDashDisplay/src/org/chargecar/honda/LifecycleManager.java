package org.chargecar.honda;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
interface LifecycleManager // TODO: move this somewhere in Commons?
   {
   void startup();

   void shutdown();
   }