package org.chargecar.lcddisplay.lcd;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class LCDButtonPanel
   {

   private final Set<LCDButtonPanelEventListener> lcdButtonPanelEventListeners = new HashSet<LCDButtonPanelEventListener>();
   private final ExecutorService executor = Executors.newCachedThreadPool();

   LCDButtonPanel()
      {


      }

   public void addButtonPanelEventListener(final LCDButtonPanelEventListener listener)
      {
      if (listener != null)
         {
         lcdButtonPanelEventListeners.add(listener);
         }
      }

   public void removeButtonPanelEventListener(final LCDButtonPanelEventListener listener)
      {
      if (listener != null)
         {
         lcdButtonPanelEventListeners.remove(listener);
         }
      }

   private final class ButtonActionListener implements ActionListener
      {
      private final Runnable task;

      private ButtonActionListener(final Runnable task)
         {
         this.task = task;
         }

      public void actionPerformed(final ActionEvent e)
         {
         executor.execute(task);
         }
      }
   }
