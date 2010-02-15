package org.chargecar;

import java.awt.Color;
import java.awt.Component;
import java.util.PropertyResourceBundle;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.AbstractTimeConsumingAction;
import org.chargecar.sensorboard.PowerView;
import org.chargecar.sensorboard.SpeedAndOdometryView;
import org.chargecar.sensorboard.TemperaturesView;
import org.jdesktop.layout.GroupLayout;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"CloneableClassWithoutClone"})
final class HeadsUpDisplayView extends JPanel
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(HeadsUpDisplayView.class.getName());

   HeadsUpDisplayView(final HeadsUpDisplayController headsUpDisplayController, final SpeedAndOdometryView speedAndOdometryView, final TemperaturesView temperaturesView, final PowerView powerView)
      {
      final JButton quitButton = GUIConstants.createButton(RESOURCES.getString("label.quit"), true);
      final JButton markButton = GUIConstants.createButton(RESOURCES.getString("label.mark"), true);

      final JButton resetBatteryPowerButton = GUIConstants.createButton(RESOURCES.getString("label.reset"), true);
      final JButton resetCapacitorPowerButton = GUIConstants.createButton(RESOURCES.getString("label.reset"), true);
      final JButton resetAccessoryPowerButton = GUIConstants.createButton(RESOURCES.getString("label.reset"), true);

      final JLabel accessoryPowerLabel = GUIConstants.createLabel(RESOURCES.getString("label.accessory-power"), GUIConstants.FONT_MEDIUM_LARGE);
      final JLabel batteryPowerLabel = GUIConstants.createLabel(RESOURCES.getString("label.battery-power"), GUIConstants.FONT_MEDIUM_LARGE);
      final JLabel capacitorPowerLabel = GUIConstants.createLabel(RESOURCES.getString("label.capacitor-power"), GUIConstants.FONT_MEDIUM_LARGE);
      final JLabel batteryEquationEquals = GUIConstants.createLabel(RESOURCES.getString("label.equals"), GUIConstants.FONT_MEDIUM_LARGE);
      final JLabel batteryEquationPlus = GUIConstants.createLabel(RESOURCES.getString("label.plus"), GUIConstants.FONT_MEDIUM_LARGE);
      final JLabel capacitorEquationEquals = GUIConstants.createLabel(RESOURCES.getString("label.equals"), GUIConstants.FONT_MEDIUM_LARGE);
      final JLabel capacitorEquationPlus = GUIConstants.createLabel(RESOURCES.getString("label.plus"), GUIConstants.FONT_MEDIUM_LARGE);

      this.setBackground(Color.WHITE);

      quitButton.addActionListener(
            new AbstractTimeConsumingAction(this)
            {
            @Override
            protected void executeGUIActionBefore()
               {
               super.executeGUIActionBefore();
               quitButton.setEnabled(false);
               }

            @Override
            protected Object executeTimeConsumingAction()
               {
               headsUpDisplayController.shutdown();
               return null;
               }

            @Override
            protected void executeGUIActionAfter(final Object resultOfTimeConsumingAction)
               {
               super.executeGUIActionAfter(resultOfTimeConsumingAction);
               quitButton.setEnabled(true);
               }
            });

      final JPanel odometerPanel = new JPanel();
      odometerPanel.setLayout(new BoxLayout(odometerPanel, BoxLayout.X_AXIS));
      odometerPanel.add(Box.createGlue());
      odometerPanel.add(quitButton);
      odometerPanel.add(Box.createGlue());
      odometerPanel.add(speedAndOdometryView.getSpeedGauge());
      odometerPanel.add(Box.createGlue());
      odometerPanel.add(speedAndOdometryView.getOdometerGauge());
      odometerPanel.add(Box.createGlue());
      odometerPanel.add(speedAndOdometryView.getTripOdometer1Gauge());
      odometerPanel.add(Box.createGlue());
      odometerPanel.add(speedAndOdometryView.getTripOdometer2Gauge());
      odometerPanel.add(Box.createGlue());
      odometerPanel.add(markButton);
      odometerPanel.add(Box.createGlue());

      final JPanel batteryAndCapDialsPanel = new JPanel();
      batteryAndCapDialsPanel.setLayout(new BoxLayout(batteryAndCapDialsPanel, BoxLayout.X_AXIS));

      batteryAndCapDialsPanel.add(powerView.getBatteryVoltageMeter());
      batteryAndCapDialsPanel.add(GUIConstants.createRigidSpacer());
      batteryAndCapDialsPanel.add(powerView.getBatteryCurrentMeter());
      batteryAndCapDialsPanel.add(GUIConstants.createRigidSpacer());
      batteryAndCapDialsPanel.add(powerView.getCapacitorVoltageMeter());
      batteryAndCapDialsPanel.add(GUIConstants.createRigidSpacer());
      batteryAndCapDialsPanel.add(powerView.getCapacitorCurrentMeter());

      final Component powerPanelHorizontalSpacer1 = GUIConstants.createRigidSpacer(40);
      final Component powerPanelHorizontalSpacer2 = GUIConstants.createRigidSpacer(40);
      final Component powerPanelVerticalSpacer1 = GUIConstants.createRigidSpacer(20);
      final Component powerPanelVerticalSpacer2 = GUIConstants.createRigidSpacer(20);
      final Component powerPanelVerticalSpacer3 = GUIConstants.createRigidSpacer(20);
      final Component powerPanelVerticalSpacer4 = GUIConstants.createRigidSpacer(20);
      final Component powerPanelVerticalSpacer5 = GUIConstants.createRigidSpacer(20);
      final Component powerPanelVerticalSpacer6 = GUIConstants.createRigidSpacer(20);

      final JPanel powerPanel = new JPanel();
      final GroupLayout powerPanelLayout = new GroupLayout(powerPanel);
      powerPanel.setLayout(powerPanelLayout);
      powerPanelLayout.setAutocreateGaps(true);
      powerPanelLayout.setHorizontalGroup(
            powerPanelLayout.createSequentialGroup()
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.TRAILING)
                        .add(batteryPowerLabel)
                        .add(powerPanelHorizontalSpacer1)
                        .add(capacitorPowerLabel)
                        .add(powerPanelHorizontalSpacer2)
                        .add(accessoryPowerLabel)
                  )
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.CENTER)
                        .add(powerPanelVerticalSpacer1)
                  )
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.CENTER)
                        .add(powerView.getBatteryPowerTotalGauge())
                        .add(powerView.getCapacitorPowerTotalGauge())
                        .add(powerView.getAccessoryPowerTotalGauge())
                  )
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.CENTER)
                        .add(powerPanelVerticalSpacer2)
                  )
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.CENTER)
                        .add(batteryEquationEquals)
                        .add(capacitorEquationEquals)
                  )
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.CENTER)
                        .add(powerPanelVerticalSpacer3)
                  )
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.CENTER)
                        .add(powerView.getBatteryPowerUsedGauge())
                        .add(powerView.getCapacitorPowerUsedGauge())
                  )
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.CENTER)
                        .add(powerPanelVerticalSpacer4)
                  )
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.CENTER)
                        .add(batteryEquationPlus)
                        .add(capacitorEquationPlus)
                  )
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.CENTER)
                        .add(powerPanelVerticalSpacer5)
                  )
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.CENTER)
                        .add(powerView.getBatteryPowerRegenGauge())
                        .add(powerView.getCapacitorPowerRegenGauge())
                  )
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.CENTER)
                        .add(powerPanelVerticalSpacer6)
                  )
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.CENTER)
                  .add(resetBatteryPowerButton)
                  .add(resetCapacitorPowerButton)
                  .add(resetAccessoryPowerButton)
            )
      );

      powerPanelLayout.setVerticalGroup(
            powerPanelLayout.createSequentialGroup()
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.LEADING)
                        .add(batteryPowerLabel)
                        .add(powerPanelVerticalSpacer1)
                        .add(powerView.getBatteryPowerTotalGauge())
                        .add(powerPanelVerticalSpacer2)
                        .add(batteryEquationEquals)
                        .add(powerPanelVerticalSpacer3)
                        .add(powerView.getBatteryPowerUsedGauge())
                        .add(powerPanelVerticalSpacer4)
                        .add(batteryEquationPlus)
                        .add(powerPanelVerticalSpacer5)
                        .add(powerView.getBatteryPowerRegenGauge())
                        .add(powerPanelVerticalSpacer6)
                        .add(resetBatteryPowerButton)
                  )
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.LEADING)
                        .add(powerPanelHorizontalSpacer1)
                  )
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.LEADING)
                        .add(capacitorPowerLabel)
                        .add(powerView.getCapacitorPowerTotalGauge())
                        .add(capacitorEquationEquals)
                        .add(powerView.getCapacitorPowerUsedGauge())
                        .add(capacitorEquationPlus)
                        .add(powerView.getCapacitorPowerRegenGauge())
                        .add(resetCapacitorPowerButton)
                  )
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.LEADING)
                        .add(powerPanelHorizontalSpacer2)
                  )
                  .add(powerPanelLayout.createParallelGroup(GroupLayout.LEADING)
                  .add(accessoryPowerLabel)
                  .add(powerView.getAccessoryPowerTotalGauge())
                  .add(resetAccessoryPowerButton)
            )
      );

      final JPanel powerPanelContainer = new JPanel();
      powerPanelContainer.setLayout(new BoxLayout(powerPanelContainer, BoxLayout.X_AXIS));
      powerPanelContainer.add(Box.createGlue());
      powerPanelContainer.add(powerPanel);
      powerPanelContainer.add(Box.createGlue());

      final JPanel batteryVoltageDialsPanel = new JPanel();
      batteryVoltageDialsPanel.setLayout(new BoxLayout(batteryVoltageDialsPanel, BoxLayout.X_AXIS));

      batteryVoltageDialsPanel.add(powerView.getBatteryVoltageMeter(0));
      batteryVoltageDialsPanel.add(GUIConstants.createRigidSpacer());
      batteryVoltageDialsPanel.add(powerView.getBatteryVoltageMeter(1));
      batteryVoltageDialsPanel.add(GUIConstants.createRigidSpacer());
      batteryVoltageDialsPanel.add(powerView.getBatteryVoltageMeter(2));
      batteryVoltageDialsPanel.add(GUIConstants.createRigidSpacer());
      batteryVoltageDialsPanel.add(powerView.getBatteryVoltageMeter(3));

      final JPanel motorCurrentDialsPanel = new JPanel();
      motorCurrentDialsPanel.setLayout(new BoxLayout(motorCurrentDialsPanel, BoxLayout.X_AXIS));

      motorCurrentDialsPanel.add(powerView.getMotorCurrentMeter(0));
      motorCurrentDialsPanel.add(GUIConstants.createRigidSpacer());
      motorCurrentDialsPanel.add(powerView.getMotorCurrentMeter(1));
      motorCurrentDialsPanel.add(GUIConstants.createRigidSpacer());
      motorCurrentDialsPanel.add(powerView.getMotorCurrentMeter(2));
      motorCurrentDialsPanel.add(GUIConstants.createRigidSpacer());
      motorCurrentDialsPanel.add(powerView.getMotorCurrentMeter(3));

      final JPanel batteryAndMotorDetailPanel = new JPanel();
      batteryAndMotorDetailPanel.setLayout(new BoxLayout(batteryAndMotorDetailPanel, BoxLayout.Y_AXIS));

      batteryAndMotorDetailPanel.add(batteryVoltageDialsPanel);
      batteryAndMotorDetailPanel.add(GUIConstants.createRigidSpacer());
      batteryAndMotorDetailPanel.add(motorCurrentDialsPanel);
      batteryAndMotorDetailPanel.add(Box.createGlue());

      final JPanel temperatureDialsRow1Panel = new JPanel();
      temperatureDialsRow1Panel.setLayout(new BoxLayout(temperatureDialsRow1Panel, BoxLayout.X_AXIS));

      temperatureDialsRow1Panel.add(temperaturesView.getBatteryMeter());
      temperatureDialsRow1Panel.add(GUIConstants.createRigidSpacer());
      temperatureDialsRow1Panel.add(temperaturesView.getCapacitorMeter());
      temperatureDialsRow1Panel.add(GUIConstants.createRigidSpacer());
      temperatureDialsRow1Panel.add(temperaturesView.getMotorControllerMeter(0));
      temperatureDialsRow1Panel.add(GUIConstants.createRigidSpacer());
      temperatureDialsRow1Panel.add(temperaturesView.getMotorControllerMeter(1));

      final JPanel temperatureDialsRow2Panel = new JPanel();
      temperatureDialsRow2Panel.setLayout(new BoxLayout(temperatureDialsRow2Panel, BoxLayout.X_AXIS));

      temperatureDialsRow2Panel.add(temperaturesView.getMotorMeter(0));
      temperatureDialsRow2Panel.add(GUIConstants.createRigidSpacer());
      temperatureDialsRow2Panel.add(temperaturesView.getMotorMeter(1));
      temperatureDialsRow2Panel.add(GUIConstants.createRigidSpacer());
      temperatureDialsRow2Panel.add(temperaturesView.getMotorMeter(2));
      temperatureDialsRow2Panel.add(GUIConstants.createRigidSpacer());
      temperatureDialsRow2Panel.add(temperaturesView.getMotorMeter(3));

      final JPanel temperatureDialsPanel = new JPanel();
      temperatureDialsPanel.setLayout(new BoxLayout(temperatureDialsPanel, BoxLayout.Y_AXIS));

      temperatureDialsPanel.add(temperatureDialsRow1Panel);
      temperatureDialsPanel.add(GUIConstants.createRigidSpacer());
      temperatureDialsPanel.add(temperatureDialsRow2Panel);
      temperatureDialsPanel.add(Box.createGlue());

      final JPanel accessoryDialsPanel = new JPanel();
      accessoryDialsPanel.setLayout(new BoxLayout(accessoryDialsPanel, BoxLayout.X_AXIS));

      accessoryDialsPanel.add(powerView.getAccessoryVoltageMeter());
      accessoryDialsPanel.add(GUIConstants.createRigidSpacer());
      accessoryDialsPanel.add(powerView.getAccessoryCurrentMeter());

      final JTabbedPane tabbedPane = new JTabbedPane();
      tabbedPane.addTab(RESOURCES.getString("label.power"), powerPanelContainer);
      tabbedPane.addTab(RESOURCES.getString("label.batteries-and-motors"), batteryAndMotorDetailPanel);
      tabbedPane.addTab(RESOURCES.getString("label.temperatures"), temperatureDialsPanel);
      tabbedPane.addTab(RESOURCES.getString("label.accessory"), accessoryDialsPanel);

      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      this.add(Box.createGlue());
      this.add(odometerPanel);
      this.add(GUIConstants.createRigidSpacer());
      this.add(batteryAndCapDialsPanel);
      this.add(GUIConstants.createRigidSpacer());
      this.add(tabbedPane);
      this.add(Box.createGlue());
      }

   /*
   layout.setHorizontalGroup(
         layout.createSequentialGroup()
               .add(layout.createParallelGroup(GroupLayout.TRAILING)
                     .add(quitButton)
                     .add(temperatureLabel)
                     .add(currentLabel)
                     .add(voltageLabel)
                     .add(batteryPowerLabel)
                     .add(capacitorPowerLabel)
                     .add(accessoryPowerLabel)
               )
               .add(layout.createParallelGroup(GroupLayout.CENTER)
                     .add(speedAndOdometryView.getSpeedGauge())
                     .add(temperaturesView.getBatteryGauge())
                     .add(powerView.getBatteryCurrentMeter())
                     .add(powerView.getBatteryVoltageMeter())
                     .add(powerView.getBatteryPowerTotalGauge())
                     .add(powerView.getCapacitorPowerTotalGauge())
                     .add(powerView.getAccessoryPowerTotalGauge())
               )
               .add(layout.createParallelGroup(GroupLayout.CENTER)
                     .add(speedAndOdometryView.getOdometerGauge())
                     .add(temperaturesView.getMotorGauge(0))
                     .add(powerView.getMotorCurrentGauge(0))
                     .add(powerView.getBatteryVoltageGauge(0))
                     .add(batteryEquationEquals)
                     .add(capacitorEquationEquals)
               )
               .add(layout.createParallelGroup(GroupLayout.CENTER)
                     .add(speedAndOdometryView.getTripOdometer1Gauge())
                     .add(temperaturesView.getMotorGauge(1))
                     .add(powerView.getMotorCurrentGauge(1))
                     .add(powerView.getBatteryVoltageGauge(1))
                     .add(powerView.getBatteryPowerUsedGauge())
                     .add(powerView.getCapacitorPowerUsedGauge())
               )
               .add(layout.createParallelGroup(GroupLayout.CENTER)
                     .add(speedAndOdometryView.getTripOdometer2Gauge())
                     .add(temperaturesView.getMotorGauge(2))
                     .add(powerView.getMotorCurrentGauge(2))
                     .add(powerView.getBatteryVoltageGauge(2))
                     .add(batteryEquationPlus)
                     .add(capacitorEquationPlus)
               )
               .add(layout.createParallelGroup(GroupLayout.CENTER)
                     .add(temperaturesView.getMotorGauge(3))
                     .add(powerView.getMotorCurrentGauge(3))
                     .add(powerView.getBatteryVoltageGauge(3))
                     .add(powerView.getBatteryPowerRegenGauge())
                     .add(powerView.getCapacitorPowerRegenGauge())
               )
               .add(layout.createParallelGroup(GroupLayout.CENTER)
                     .add(temperaturesView.getCapacitorGauge())
                     .add(powerView.getCapacitorCurrentMeter())
                     .add(powerView.getCapacitorVoltageMeter())
               )
               .add(layout.createParallelGroup(GroupLayout.CENTER)
                     .add(temperaturesView.getMotorControllerGauge(0))
                     .add(powerView.getAccessoryCurrentGauge())
                     .add(powerView.getAccessoryVoltageGauge())
               )
               .add(temperaturesView.getMotorControllerGauge(1))
               .add(temperaturesView.getOutsideMeter())
   );

   layout.setVerticalGroup(
         layout.createSequentialGroup()
               .add(layout.createParallelGroup(GroupLayout.CENTER)
                     .add(quitButton)
                     .add(speedAndOdometryView.getSpeedGauge())
                     .add(speedAndOdometryView.getOdometerGauge())
                     .add(speedAndOdometryView.getTripOdometer1Gauge())
                     .add(speedAndOdometryView.getTripOdometer2Gauge())
               )
               .add(layout.createParallelGroup(GroupLayout.CENTER)
                     .add(temperatureLabel)
                     .add(temperaturesView.getBatteryGauge())
                     .add(temperaturesView.getMotorGauge(0))
                     .add(temperaturesView.getMotorGauge(1))
                     .add(temperaturesView.getMotorGauge(2))
                     .add(temperaturesView.getMotorGauge(3))
                     .add(temperaturesView.getMotorControllerGauge(0))
                     .add(temperaturesView.getMotorControllerGauge(1))
                     .add(temperaturesView.getCapacitorGauge())
                     .add(temperaturesView.getOutsideMeter())
               )
               .add(layout.createParallelGroup(GroupLayout.CENTER)
                     .add(currentLabel)
                     .add(powerView.getBatteryCurrentMeter())
                     .add(powerView.getMotorCurrentGauge(0))
                     .add(powerView.getMotorCurrentGauge(1))
                     .add(powerView.getMotorCurrentGauge(2))
                     .add(powerView.getMotorCurrentGauge(3))
                     .add(powerView.getCapacitorCurrentMeter())
                     .add(powerView.getAccessoryCurrentGauge())
               )
               .add(layout.createParallelGroup(GroupLayout.CENTER)
                     .add(voltageLabel)
                     .add(powerView.getBatteryVoltageMeter())
                     .add(powerView.getBatteryVoltageGauge(0))
                     .add(powerView.getBatteryVoltageGauge(1))
                     .add(powerView.getBatteryVoltageGauge(2))
                     .add(powerView.getBatteryVoltageGauge(3))
                     .add(powerView.getCapacitorVoltageMeter())
                     .add(powerView.getAccessoryVoltageGauge())
               )
               .add(layout.createParallelGroup(GroupLayout.CENTER)
                     .add(batteryPowerLabel)
                     .add(powerView.getBatteryPowerTotalGauge())
                     .add(batteryEquationEquals)
                     .add(powerView.getBatteryPowerUsedGauge())
                     .add(batteryEquationPlus)
                     .add(powerView.getBatteryPowerRegenGauge())
               )
               .add(layout.createParallelGroup(GroupLayout.CENTER)
                     .add(capacitorPowerLabel)
                     .add(powerView.getCapacitorPowerTotalGauge())
                     .add(capacitorEquationEquals)
                     .add(powerView.getCapacitorPowerUsedGauge())
                     .add(capacitorEquationPlus)
                     .add(powerView.getCapacitorPowerRegenGauge())
               )
               .add(layout.createParallelGroup(GroupLayout.CENTER)
               .add(accessoryPowerLabel)
               .add(powerView.getAccessoryPowerTotalGauge())
         )
   );
   */
   }