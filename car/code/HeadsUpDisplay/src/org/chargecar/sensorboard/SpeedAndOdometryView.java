package org.chargecar.sensorboard;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SpeedAndOdometryView implements SpeedEventListener
   {
   private static final String UNKNOWN_VALUE = "?";

   private final JPanel panel = new JPanel();
   private final JLabel speedValue = new JLabel("      ");
   private final JLabel odometerValue = new JLabel("      ");

   public SpeedAndOdometryView()
      {
      panel.add(speedValue);
      panel.add(odometerValue);
      }

   public Component getComponent()
      {
      return panel;
      }

   public void handleEvent(final SpeedAndOdometry speedAndOdometry)
      {
      runInGUIThread(
            new Runnable()
            {
            public void run()
               {
               if (speedAndOdometry != null)
                  {
                  final Integer speed = speedAndOdometry.getSpeed();
                  speedValue.setText(speed == null ? UNKNOWN_VALUE : speed.toString());
                  odometerValue.setText(String.valueOf(speedAndOdometry.getOdometer()));
                  }
               else
                  {
                  speedValue.setText(UNKNOWN_VALUE);
                  odometerValue.setText(UNKNOWN_VALUE);
                  }
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
