package org.chargecar;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class HeadsUpDisplayView
   {
   private final JPanel panel = new JPanel();
   private final JLabel speedValue = new JLabel("      ");

   HeadsUpDisplayView()
      {
      final JLabel speedLabel = new JLabel("Speed:");
      panel.add(speedValue);
      }

   Component getComponent()
      {
      return panel;
      }

   public void setSpeed(final Integer speed)
      {
      runInGUIThread(
            new Runnable()
            {
            public void run()
               {
               speedValue.setText(speed == null ? "?" : speed.toString());
               }
            });
      }

   private void runInGUIThread(final Runnable runnable)
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
