package org.chargecar.sensorboard;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import org.jdesktop.layout.GroupLayout;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SpeedAndOdometryView implements SpeedEventListener
   {
   private static final String UNKNOWN_VALUE = "?";

   private final JPanel speedGaugePanel = new JPanel();
   private final JLabel speedValue = GUIConstants.createLabel("      ", GUIConstants.FONT_LARGE);

   private final JPanel odometerGaugePanel = new JPanel();
   private final JLabel odometerValue = GUIConstants.createLabel("      ", GUIConstants.FONT_LARGE);

   private final JPanel tripOdometerGaugePanel = new JPanel();
   private final JLabel tripOdometerValue = GUIConstants.createLabel("      ", GUIConstants.FONT_LARGE);

   public SpeedAndOdometryView()
      {
      // TODO: i18n
      final JLabel speedLabel = GUIConstants.createLabel("mph", GUIConstants.FONT_SMALL);
      final JLabel odometerLabel = GUIConstants.createLabel("odometer", GUIConstants.FONT_SMALL);
      final JLabel tripOdometerLabel = GUIConstants.createLabel("trip", GUIConstants.FONT_SMALL);

      setupGaugePanel(speedGaugePanel, speedLabel, speedValue);
      setupGaugePanel(odometerGaugePanel, odometerLabel, odometerValue);
      setupGaugePanel(tripOdometerGaugePanel, tripOdometerLabel, tripOdometerValue);
      }

   private void setupGaugePanel(final JPanel panel, final JLabel label, final JLabel value)
      {
      final GroupLayout layout = new GroupLayout(panel);
      panel.setLayout(layout);
      panel.setBackground(Color.WHITE);

      layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.CENTER)
                  .add(value)
                  .add(label));
      layout.setVerticalGroup(
            layout.createSequentialGroup()
                  .add(value)
                  .add(label));
      }

   public JPanel getSpeedGaugePanel()
      {
      return speedGaugePanel;
      }

   public JPanel getOdometerGaugePanel()
      {
      return odometerGaugePanel;
      }

   public JPanel getTripOdometerGaugePanel()
      {
      return tripOdometerGaugePanel;
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
                  tripOdometerValue.setText(String.valueOf(speedAndOdometry.getTripOdometer()));
                  }
               else
                  {
                  speedValue.setText(UNKNOWN_VALUE);
                  odometerValue.setText(UNKNOWN_VALUE);
                  tripOdometerValue.setText(UNKNOWN_VALUE);
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
