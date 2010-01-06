package org.chargecar;

import java.awt.Component;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import org.chargecar.sensorboard.SensorBoardConstants;
import org.chargecar.sensorboard.SpeedAndOdometryView;
import org.chargecar.sensorboard.TemperaturesView;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class HeadsUpDisplayView
   {
   private static final int SPACER_SIZE = 20;

   private final JPanel panel = new JPanel();

   HeadsUpDisplayView(final SpeedAndOdometryView speedAndOdometryView, final TemperaturesView temperaturesView)
      {

      final JPanel speedAndOdometryPanel = new JPanel();
      speedAndOdometryPanel.setLayout(new BoxLayout(speedAndOdometryPanel, BoxLayout.X_AXIS));
      speedAndOdometryPanel.add(speedAndOdometryView.getSpeedGaugePanel());
      speedAndOdometryPanel.add(GUIConstants.createRigidSpacer(SPACER_SIZE));
      speedAndOdometryPanel.add(speedAndOdometryView.getOdometerGaugePanel());
      speedAndOdometryPanel.add(GUIConstants.createRigidSpacer(SPACER_SIZE));
      speedAndOdometryPanel.add(speedAndOdometryView.getTripOdometerGaugePanel());
      speedAndOdometryPanel.add(Box.createGlue());

      final JPanel temperaturesPanel = new JPanel();
      temperaturesPanel.setLayout(new BoxLayout(temperaturesPanel, BoxLayout.X_AXIS));
      temperaturesPanel.add(temperaturesView.getBatteryGauge());
      temperaturesPanel.add(GUIConstants.createRigidSpacer(SPACER_SIZE));
      for (int i = 0; i < SensorBoardConstants.MOTOR_DEVICE_COUNT; i++)
         {
         temperaturesPanel.add(temperaturesView.getMotorGauge(i));
         temperaturesPanel.add(GUIConstants.createRigidSpacer(SPACER_SIZE));
         }
      for (int i = 0; i < SensorBoardConstants.MOTOR_CONTROLLER_DEVICE_COUNT; i++)
         {
         temperaturesPanel.add(temperaturesView.getMotorControllerGauge(i));
         temperaturesPanel.add(GUIConstants.createRigidSpacer(SPACER_SIZE));
         }
      temperaturesPanel.add(temperaturesView.getCapacitorGauge());
      temperaturesPanel.add(GUIConstants.createRigidSpacer(SPACER_SIZE));
      temperaturesPanel.add(temperaturesView.getOutsideGauge());
      temperaturesPanel.add(Box.createGlue());

      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.add(GUIConstants.createRigidSpacer(SPACER_SIZE));
      panel.add(speedAndOdometryPanel);
      panel.add(GUIConstants.createRigidSpacer(SPACER_SIZE));
      panel.add(temperaturesPanel);
      panel.add(Box.createGlue());
      }

   Component getComponent()
      {
      return panel;
      }
   }
