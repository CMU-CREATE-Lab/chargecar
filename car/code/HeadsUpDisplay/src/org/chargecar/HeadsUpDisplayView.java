package org.chargecar;

import java.awt.Component;
import java.util.PropertyResourceBundle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import org.chargecar.sensorboard.PowerView;
import org.chargecar.sensorboard.SpeedAndOdometryView;
import org.chargecar.sensorboard.TemperaturesView;
import org.jdesktop.layout.GroupLayout;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class HeadsUpDisplayView
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(HeadsUpDisplayView.class.getName());

   private final JPanel panel = new JPanel();

   HeadsUpDisplayView(final SpeedAndOdometryView speedAndOdometryView, final TemperaturesView temperaturesView, final PowerView powerView)
      {
      final GroupLayout layout = new GroupLayout(panel);
      panel.setLayout(layout);
      layout.setAutocreateGaps(true);

      final JLabel temperatureLabel = GUIConstants.createLabel(RESOURCES.getString("label.temperature"), GUIConstants.FONT_NORMAL);
      final JLabel currentLabel = GUIConstants.createLabel(RESOURCES.getString("label.current"), GUIConstants.FONT_NORMAL);
      final JLabel voltageLabel = GUIConstants.createLabel(RESOURCES.getString("label.voltage"), GUIConstants.FONT_NORMAL);
      final JLabel accessoryPowerLabel = GUIConstants.createLabel(RESOURCES.getString("label.accessory-power"), GUIConstants.FONT_NORMAL);
      final JLabel batteryPowerLabel = GUIConstants.createLabel(RESOURCES.getString("label.battery-power"), GUIConstants.FONT_NORMAL);
      final JLabel capacitorPowerLabel = GUIConstants.createLabel(RESOURCES.getString("label.capacitor-power"), GUIConstants.FONT_NORMAL);
      final JLabel accessoryEquationEquals = GUIConstants.createLabel(RESOURCES.getString("label.equals"), GUIConstants.FONT_LARGE);
      final JLabel accessoryEquationPlus = GUIConstants.createLabel(RESOURCES.getString("label.plus"), GUIConstants.FONT_LARGE);
      final JLabel batteryEquationEquals = GUIConstants.createLabel(RESOURCES.getString("label.equals"), GUIConstants.FONT_LARGE);
      final JLabel batteryEquationPlus = GUIConstants.createLabel(RESOURCES.getString("label.plus"), GUIConstants.FONT_LARGE);
      final JLabel capacitorEquationEquals = GUIConstants.createLabel(RESOURCES.getString("label.equals"), GUIConstants.FONT_LARGE);
      final JLabel capacitorEquationPlus = GUIConstants.createLabel(RESOURCES.getString("label.plus"), GUIConstants.FONT_LARGE);

      layout.setHorizontalGroup(
            layout.createSequentialGroup()
                  .add(layout.createParallelGroup(GroupLayout.TRAILING)
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
                        .add(powerView.getBatteryCurrentGauge())
                        .add(powerView.getBatteryVoltageGauge())
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
                        .add(accessoryEquationEquals)
                  )
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                        .add(speedAndOdometryView.getTripOdometerGauge())
                        .add(temperaturesView.getMotorGauge(1))
                        .add(powerView.getMotorCurrentGauge(1))
                        .add(powerView.getBatteryVoltageGauge(1))
                        .add(powerView.getBatteryPowerUsedGauge())
                        .add(powerView.getCapacitorPowerUsedGauge())
                        .add(powerView.getAccessoryPowerUsedGauge())
                  )
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                        .add(temperaturesView.getMotorGauge(2))
                        .add(powerView.getMotorCurrentGauge(2))
                        .add(powerView.getBatteryVoltageGauge(2))
                        .add(batteryEquationPlus)
                        .add(capacitorEquationPlus)
                        .add(accessoryEquationPlus)
                  )
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                        .add(temperaturesView.getMotorGauge(3))
                        .add(powerView.getMotorCurrentGauge(3))
                        .add(powerView.getBatteryVoltageGauge(3))
                        .add(powerView.getBatteryPowerRegenGauge())
                        .add(powerView.getCapacitorPowerRegenGauge())
                        .add(powerView.getAccessoryPowerRegenGauge())
                  )
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                        .add(temperaturesView.getCapacitorGauge())
                        .add(powerView.getCapacitorCurrentGauge())
                        .add(powerView.getCapacitorVoltageGauge())
                  )
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                        .add(temperaturesView.getMotorControllerGauge(0))
                        .add(powerView.getAccessoryCurrentGauge())
                        .add(powerView.getAccessoryVoltageGauge())
                  )
                  .add(temperaturesView.getMotorControllerGauge(1))
                  .add(temperaturesView.getOutsideGauge())
      );

      layout.setVerticalGroup(
            layout.createSequentialGroup()
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                        .add(speedAndOdometryView.getSpeedGauge())
                        .add(speedAndOdometryView.getOdometerGauge())
                        .add(speedAndOdometryView.getTripOdometerGauge())
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
                        .add(temperaturesView.getOutsideGauge())
                  )
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                        .add(currentLabel)
                        .add(powerView.getBatteryCurrentGauge())
                        .add(powerView.getMotorCurrentGauge(0))
                        .add(powerView.getMotorCurrentGauge(1))
                        .add(powerView.getMotorCurrentGauge(2))
                        .add(powerView.getMotorCurrentGauge(3))
                        .add(powerView.getCapacitorCurrentGauge())
                        .add(powerView.getAccessoryCurrentGauge())
                  )
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                        .add(voltageLabel)
                        .add(powerView.getBatteryVoltageGauge())
                        .add(powerView.getBatteryVoltageGauge(0))
                        .add(powerView.getBatteryVoltageGauge(1))
                        .add(powerView.getBatteryVoltageGauge(2))
                        .add(powerView.getBatteryVoltageGauge(3))
                        .add(powerView.getCapacitorVoltageGauge())
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
                  .add(accessoryEquationEquals)
                  .add(powerView.getAccessoryPowerUsedGauge())
                  .add(accessoryEquationPlus)
                  .add(powerView.getAccessoryPowerRegenGauge())
            )
      );
      }

   Component getComponent()
      {
      return panel;
      }
   }
