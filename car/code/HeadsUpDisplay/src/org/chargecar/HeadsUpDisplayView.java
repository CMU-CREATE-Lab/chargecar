package org.chargecar;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.PropertyResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.AbstractTimeConsumingAction;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.sensorboard.PowerController;
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
   private static final Log LOG = LogFactory.getLog(HeadsUpDisplayView.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(HeadsUpDisplayView.class.getName());

   HeadsUpDisplayView(final HeadsUpDisplayController headsUpDisplayController,
                      final SpeedAndOdometryView speedAndOdometryView,
                      final TemperaturesView temperaturesView,
                      final PowerView powerView,
                      final PowerController powerController)
      {
      final AtomicInteger markValue = new AtomicInteger(0);
      final JButton quitButton = SwingUtils.createButton(RESOURCES.getString("label.quit"), true);
      final JButton markButton = SwingUtils.createButton(RESOURCES.getString("label.mark") + " " + markValue.get(), true);

      final JButton resetBatteryPowerButton = SwingUtils.createButton(RESOURCES.getString("label.reset"), true);
      final JButton resetCapacitorPowerButton = SwingUtils.createButton(RESOURCES.getString("label.reset"), true);
      final JButton resetAccessoryPowerButton = SwingUtils.createButton(RESOURCES.getString("label.reset"), true);

      final JLabel accessoryPowerLabel = SwingUtils.createLabel(RESOURCES.getString("label.accessory-power"), GUIConstants.FONT_MEDIUM_LARGE);
      final JLabel batteryPowerLabel = SwingUtils.createLabel(RESOURCES.getString("label.battery-power"), GUIConstants.FONT_MEDIUM_LARGE);
      final JLabel capacitorPowerLabel = SwingUtils.createLabel(RESOURCES.getString("label.capacitor-power"), GUIConstants.FONT_MEDIUM_LARGE);
      final JLabel batteryEquationEquals = SwingUtils.createLabel(RESOURCES.getString("label.equals"), GUIConstants.FONT_MEDIUM_LARGE);
      final JLabel batteryEquationPlus = SwingUtils.createLabel(RESOURCES.getString("label.plus"), GUIConstants.FONT_MEDIUM_LARGE);
      final JLabel capacitorEquationEquals = SwingUtils.createLabel(RESOURCES.getString("label.equals"), GUIConstants.FONT_MEDIUM_LARGE);
      final JLabel capacitorEquationPlus = SwingUtils.createLabel(RESOURCES.getString("label.plus"), GUIConstants.FONT_MEDIUM_LARGE);

      this.setBackground(Color.WHITE);

      quitButton.addActionListener(
            new ButtonTimeConsumingAction(this, quitButton)
            {
            protected Object executeTimeConsumingAction()
               {
               headsUpDisplayController.shutdown();
               return null;
               }
            });

      markButton.addActionListener(
            new ActionListener()
            {
            public void actionPerformed(final ActionEvent e)
               {
               LOG.info("============================================================= MARK " + markValue.getAndIncrement() + " =============================================================");
               markButton.setText(RESOURCES.getString("label.mark") + " " + markValue.get());
               }
            });

      resetBatteryPowerButton.addActionListener(
            new ButtonTimeConsumingAction(this, resetBatteryPowerButton)
            {
            protected Object executeTimeConsumingAction()
               {
               powerController.resetBatteryPowerEquation();
               return null;
               }
            });

      resetCapacitorPowerButton.addActionListener(
            new ButtonTimeConsumingAction(this, resetCapacitorPowerButton)
            {
            protected Object executeTimeConsumingAction()
               {
               powerController.resetCapacitorPowerEquation();
               return null;
               }
            });

      resetAccessoryPowerButton.addActionListener(
            new ButtonTimeConsumingAction(this, resetAccessoryPowerButton)
            {
            protected Object executeTimeConsumingAction()
               {
               powerController.resetAccessoryPowerEquation();
               return null;
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

      batteryAndCapDialsPanel.add(Box.createGlue());
      batteryAndCapDialsPanel.add(powerView.getBatteryVoltageMeter());
      batteryAndCapDialsPanel.add(SwingUtils.createRigidSpacer());
      batteryAndCapDialsPanel.add(powerView.getBatteryCurrentMeter());
      batteryAndCapDialsPanel.add(SwingUtils.createRigidSpacer());
      batteryAndCapDialsPanel.add(powerView.getCapacitorVoltageMeter());
      batteryAndCapDialsPanel.add(SwingUtils.createRigidSpacer());
      batteryAndCapDialsPanel.add(powerView.getCapacitorCurrentMeter());
      batteryAndCapDialsPanel.add(Box.createGlue());

      final Component powerPanelHorizontalSpacer1 = SwingUtils.createRigidSpacer(40);
      final Component powerPanelHorizontalSpacer2 = SwingUtils.createRigidSpacer(40);
      final Component powerPanelVerticalSpacer1 = SwingUtils.createRigidSpacer(20);
      final Component powerPanelVerticalSpacer2 = SwingUtils.createRigidSpacer(20);
      final Component powerPanelVerticalSpacer3 = SwingUtils.createRigidSpacer(20);
      final Component powerPanelVerticalSpacer4 = SwingUtils.createRigidSpacer(20);
      final Component powerPanelVerticalSpacer5 = SwingUtils.createRigidSpacer(20);
      final Component powerPanelVerticalSpacer6 = SwingUtils.createRigidSpacer(20);

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

      batteryVoltageDialsPanel.add(Box.createGlue());
      batteryVoltageDialsPanel.add(powerView.getBatteryVoltageMeter(0));
      batteryVoltageDialsPanel.add(SwingUtils.createRigidSpacer());
      batteryVoltageDialsPanel.add(powerView.getBatteryVoltageMeter(1));
      batteryVoltageDialsPanel.add(SwingUtils.createRigidSpacer());
      batteryVoltageDialsPanel.add(powerView.getBatteryVoltageMeter(2));
      batteryVoltageDialsPanel.add(SwingUtils.createRigidSpacer());
      batteryVoltageDialsPanel.add(powerView.getBatteryVoltageMeter(3));
      batteryVoltageDialsPanel.add(Box.createGlue());

      final JPanel motorCurrentDialsPanel = new JPanel();
      motorCurrentDialsPanel.setLayout(new BoxLayout(motorCurrentDialsPanel, BoxLayout.X_AXIS));

      motorCurrentDialsPanel.add(Box.createGlue());
      motorCurrentDialsPanel.add(powerView.getMotorCurrentMeter(0));
      motorCurrentDialsPanel.add(SwingUtils.createRigidSpacer());
      motorCurrentDialsPanel.add(powerView.getMotorCurrentMeter(1));
      motorCurrentDialsPanel.add(SwingUtils.createRigidSpacer());
      motorCurrentDialsPanel.add(powerView.getMotorCurrentMeter(2));
      motorCurrentDialsPanel.add(SwingUtils.createRigidSpacer());
      motorCurrentDialsPanel.add(powerView.getMotorCurrentMeter(3));
      motorCurrentDialsPanel.add(Box.createGlue());

      final JPanel batteryAndMotorDetailPanel = new JPanel();
      batteryAndMotorDetailPanel.setLayout(new BoxLayout(batteryAndMotorDetailPanel, BoxLayout.Y_AXIS));

      batteryAndMotorDetailPanel.add(batteryVoltageDialsPanel);
      batteryAndMotorDetailPanel.add(SwingUtils.createRigidSpacer());
      batteryAndMotorDetailPanel.add(motorCurrentDialsPanel);
      batteryAndMotorDetailPanel.add(Box.createGlue());

      final JPanel temperatureDialsRow1Panel = new JPanel();
      temperatureDialsRow1Panel.setLayout(new BoxLayout(temperatureDialsRow1Panel, BoxLayout.X_AXIS));

      temperatureDialsRow1Panel.add(Box.createGlue());
      temperatureDialsRow1Panel.add(temperaturesView.getBatteryMeter());
      temperatureDialsRow1Panel.add(SwingUtils.createRigidSpacer());
      temperatureDialsRow1Panel.add(temperaturesView.getCapacitorMeter());
      temperatureDialsRow1Panel.add(SwingUtils.createRigidSpacer());
      temperatureDialsRow1Panel.add(temperaturesView.getMotorControllerMeter(0));
      temperatureDialsRow1Panel.add(SwingUtils.createRigidSpacer());
      temperatureDialsRow1Panel.add(temperaturesView.getMotorControllerMeter(1));
      temperatureDialsRow1Panel.add(Box.createGlue());

      final JPanel temperatureDialsRow2Panel = new JPanel();
      temperatureDialsRow2Panel.setLayout(new BoxLayout(temperatureDialsRow2Panel, BoxLayout.X_AXIS));

      temperatureDialsRow2Panel.add(Box.createGlue());
      temperatureDialsRow2Panel.add(temperaturesView.getMotorMeter(0));
      temperatureDialsRow2Panel.add(SwingUtils.createRigidSpacer());
      temperatureDialsRow2Panel.add(temperaturesView.getMotorMeter(1));
      temperatureDialsRow2Panel.add(SwingUtils.createRigidSpacer());
      temperatureDialsRow2Panel.add(temperaturesView.getMotorMeter(2));
      temperatureDialsRow2Panel.add(SwingUtils.createRigidSpacer());
      temperatureDialsRow2Panel.add(temperaturesView.getMotorMeter(3));
      temperatureDialsRow2Panel.add(Box.createGlue());

      final JPanel temperatureDialsPanel = new JPanel();
      temperatureDialsPanel.setLayout(new BoxLayout(temperatureDialsPanel, BoxLayout.Y_AXIS));

      temperatureDialsPanel.add(temperatureDialsRow1Panel);
      temperatureDialsPanel.add(SwingUtils.createRigidSpacer());
      temperatureDialsPanel.add(temperatureDialsRow2Panel);
      temperatureDialsPanel.add(Box.createGlue());

      final JPanel accessoryDialsPanel = new JPanel();
      accessoryDialsPanel.setLayout(new BoxLayout(accessoryDialsPanel, BoxLayout.X_AXIS));

      accessoryDialsPanel.add(Box.createGlue());
      accessoryDialsPanel.add(powerView.getAccessoryVoltageMeter());
      accessoryDialsPanel.add(SwingUtils.createRigidSpacer());
      accessoryDialsPanel.add(powerView.getAccessoryCurrentMeter());
      accessoryDialsPanel.add(Box.createGlue());

      final JTabbedPane tabbedPane = new JTabbedPane();
      tabbedPane.addTab(RESOURCES.getString("label.power"), powerPanelContainer);
      tabbedPane.addTab(RESOURCES.getString("label.batteries-and-motors"), batteryAndMotorDetailPanel);
      tabbedPane.addTab(RESOURCES.getString("label.temperatures"), temperatureDialsPanel);
      tabbedPane.addTab(RESOURCES.getString("label.accessory"), accessoryDialsPanel);

      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      this.add(Box.createGlue());
      this.add(odometerPanel);
      this.add(SwingUtils.createRigidSpacer());
      this.add(batteryAndCapDialsPanel);
      this.add(SwingUtils.createRigidSpacer());
      this.add(tabbedPane);
      this.add(Box.createGlue());
      }

   private abstract static class ButtonTimeConsumingAction extends AbstractTimeConsumingAction
      {
      private final JButton button;

      private ButtonTimeConsumingAction(final Component parentComponent, final JButton button)
         {
         super(parentComponent);
         this.button = button;
         }

      @Override
      protected final void executeGUIActionBefore()
         {
         super.executeGUIActionBefore();
         button.setEnabled(false);
         }

      @Override
      protected void executeGUIActionAfter(final Object resultOfTimeConsumingAction)
         {
         super.executeGUIActionAfter(resultOfTimeConsumingAction);
         button.setEnabled(true);
         }
      }
   }
