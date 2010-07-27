package org.chargecar.honda;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.PropertyResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import edu.cmu.ri.createlab.userinterface.util.AbstractTimeConsumingAction;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.honda.bms.BMSModel;
import org.chargecar.honda.bms.BMSView;
import org.chargecar.honda.gps.GPSModel;
import org.chargecar.honda.motorcontroller.MotorControllerModel;
import org.chargecar.honda.motorcontroller.MotorControllerView;
import org.chargecar.honda.sensorboard.SensorBoardModel;
import org.chargecar.honda.sensorboard.SensorBoardView;
import org.chargecar.serial.streaming.StreamingSerialPortDeviceConnectionStateListener;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"CloneableClassWithoutClone"})
public final class InDashDisplayView extends JPanel
   {
   private static final Log LOG = LogFactory.getLog(InDashDisplayView.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(InDashDisplayView.class.getName());

   public InDashDisplayView(final InDashDisplayController inDashDisplayController,
                            final BMSModel bmsModel,
                            final GPSModel gpsModel,
                            final MotorControllerModel motorControllerModel,
                            final SensorBoardModel sensorBoardModel,
                            final BMSView bmsView,
                            final MotorControllerView motorControllerView,
                            final SensorBoardView sensorBoardView)
      {

      final AtomicInteger markValue = new AtomicInteger(0);
      final JButton quitButton = SwingUtils.createButton(RESOURCES.getString("label.quit"), true);
      final JButton markButton = SwingUtils.createButton(RESOURCES.getString("label.mark") + " " + markValue.get(), true);

      this.setBackground(Color.WHITE);

      quitButton.addActionListener(
            new ButtonTimeConsumingAction(this, quitButton)
            {
            protected Object executeTimeConsumingAction()
               {
               inDashDisplayController.shutdown();
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

      // create the connection state status labels
      final JLabel bmsConnectionState = SwingUtils.createLabel(RESOURCES.getString("label.bms"));
      final JLabel gpsConnectionState = SwingUtils.createLabel(RESOURCES.getString("label.gps"));
      final JLabel motorControllerConnectionState = SwingUtils.createLabel(RESOURCES.getString("label.motor-controller"));
      final JLabel sensorBoardConnectionState = SwingUtils.createLabel(RESOURCES.getString("label.sensor-board"));

      // configure the connection state status labels
      bmsConnectionState.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK),
                                                                      BorderFactory.createEmptyBorder(3, 3, 3, 3)));
      gpsConnectionState.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK),
                                                                      BorderFactory.createEmptyBorder(3, 3, 3, 3)));
      motorControllerConnectionState.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK),
                                                                                  BorderFactory.createEmptyBorder(3, 3, 3, 3)));
      sensorBoardConnectionState.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK),
                                                                              BorderFactory.createEmptyBorder(3, 3, 3, 3)));

      final JPanel row1 = new JPanel();
      row1.setBackground(Color.WHITE);
      row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
      row1.add(Box.createGlue());
      row1.add(quitButton);
      row1.add(Box.createGlue());
      row1.add(bmsConnectionState);
      row1.add(SwingUtils.createRigidSpacer(10));
      row1.add(gpsConnectionState);
      row1.add(SwingUtils.createRigidSpacer(10));
      row1.add(motorControllerConnectionState);
      row1.add(SwingUtils.createRigidSpacer(10));
      row1.add(sensorBoardConnectionState);
      row1.add(Box.createGlue());
      row1.add(markButton);
      row1.add(Box.createGlue());

      final JPanel row2 = new JPanel();
      row2.setBackground(Color.WHITE);
      row2.setLayout(new BoxLayout(row2, BoxLayout.X_AXIS));
      row2.add(Box.createGlue());
      row2.add(sensorBoardView.getMotorTempGauge());
      row2.add(Box.createGlue());
      row2.add(sensorBoardView.getMotorControllerTempGauge());
      row2.add(Box.createGlue());
      row2.add(motorControllerView.getRpmGauge());
      row2.add(Box.createGlue());

      final JPanel row3 = new JPanel();
      row3.setBackground(Color.WHITE);
      row3.setLayout(new BoxLayout(row3, BoxLayout.X_AXIS));
      row3.add(Box.createGlue());
      row3.add(bmsView.getMinimumCellTempGauge());
      row3.add(Box.createGlue());
      row3.add(bmsView.getMaximumCellTempGauge());
      row3.add(Box.createGlue());
      row3.add(bmsView.getAverageCellTempGauge());
      row3.add(Box.createGlue());
      row3.add(bmsView.getCellNumWithLowestTempGauge());
      row3.add(Box.createGlue());
      row3.add(bmsView.getCellNumWithHighestTempGauge());
      row3.add(Box.createGlue());

      final JPanel row4 = new JPanel();
      row4.setBackground(Color.WHITE);
      row4.setLayout(new BoxLayout(row4, BoxLayout.X_AXIS));
      row4.add(Box.createGlue());
      row4.add(bmsView.getPackTotalVoltageGauge());
      row4.add(Box.createGlue());
      row4.add(bmsView.getMinimumCellVoltageGauge());
      row4.add(Box.createGlue());
      row4.add(bmsView.getMaximumCellVoltageGauge());
      row4.add(Box.createGlue());
      row4.add(bmsView.getAverageCellVoltageGauge());
      row4.add(Box.createGlue());
      row4.add(bmsView.getCellNumWithLowestVoltageGauge());
      row4.add(Box.createGlue());
      row4.add(bmsView.getCellNumWithHighestVoltageGauge());
      row4.add(Box.createGlue());

      final JPanel bottomRow = new JPanel();
      bottomRow.setBackground(Color.WHITE);
      bottomRow.setLayout(new BoxLayout(bottomRow, BoxLayout.X_AXIS));
      bottomRow.add(Box.createGlue());
      bottomRow.add(bmsView.getLLIMSetGauge());
      bottomRow.add(Box.createGlue());
      bottomRow.add(bmsView.getFaultStatusPanel());
      bottomRow.add(Box.createGlue());
      bottomRow.add(bmsView.getHLIMSetGauge());
      bottomRow.add(Box.createGlue());

      // register self as a connection state listener for the various models so we can display connection status
      bmsModel.addStreamingSerialPortDeviceConnectionStateListener(new DeviceConnectionStateListener(bmsConnectionState));
      gpsModel.addStreamingSerialPortDeviceConnectionStateListener(new DeviceConnectionStateListener(gpsConnectionState));
      motorControllerModel.addStreamingSerialPortDeviceConnectionStateListener(new DeviceConnectionStateListener(motorControllerConnectionState));
      sensorBoardModel.addStreamingSerialPortDeviceConnectionStateListener(new DeviceConnectionStateListener(sensorBoardConnectionState));

      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      this.add(Box.createGlue());
      this.add(row1);
      this.add(Box.createGlue());
      this.add(row2);
      this.add(Box.createGlue());
      this.add(row3);
      this.add(Box.createGlue());
      this.add(row4);
      this.add(Box.createGlue());
      this.add(bottomRow);
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

   private final class DeviceConnectionStateListener implements StreamingSerialPortDeviceConnectionStateListener
      {
      private final JLabel label;
      private final Runnable isConnectedRunnable =
            new Runnable()
            {
            public void run()
               {
               label.setForeground(HondaConstants.GREEN);
               }
            };
      private final Runnable isDisconnectedRunnable =
            new Runnable()
            {
            public void run()
               {
               label.setForeground(HondaConstants.RED);
               }
            };

      private DeviceConnectionStateListener(final JLabel label)
         {
         this.label = label;
         }

      public void handleConnectionStateChange(final boolean isConnected)
         {
         SwingUtils.runInGUIThread(isConnected ? isConnectedRunnable : isDisconnectedRunnable);
         }
      }
   }
