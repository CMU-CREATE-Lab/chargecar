package org.chargecar.honda;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.util.PropertyResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.AbstractTimeConsumingAction;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;
import org.chargecar.honda.bms.BMSController;
import org.chargecar.honda.bms.BMSModel;
import org.chargecar.honda.bms.BMSView;
import org.chargecar.honda.gps.GPSModel;
import org.chargecar.honda.motorcontroller.MotorControllerModel;
import org.chargecar.honda.motorcontroller.MotorControllerView;
import org.chargecar.honda.sensorboard.SensorBoardModel;
import org.chargecar.honda.sensorboard.SensorBoardView;
import org.chargecar.serial.streaming.StreamingSerialPortDeviceConnectionStateListener;
import org.jdesktop.layout.GroupLayout;
import java.awt.GridLayout;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"CloneableClassWithoutClone"})
public final class InDashDisplayView extends JPanel
   {
	   private static String CARD_HOME = "Card for Home Screen";
	   private static String CARD_INFO = "Card for Info Screen";
   private static final Logger LOG = Logger.getLogger(InDashDisplayView.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(InDashDisplayView.class.getName());

   public InDashDisplayView(final InDashDisplayController inDashDisplayController,
                            final BMSController bmsController,
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
      final JButton resetBatteryEnergyButton = SwingUtils.createButton(RESOURCES.getString("label.reset"), true);

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

      final ActionListener markButtonActionListener =
            new ActionListener()
            {
            public void actionPerformed(final ActionEvent e)
               {
               LOG.info("============================================================= MARK " + markValue.getAndIncrement() + " (" + System.currentTimeMillis() + ") =============================================================");
               markButton.setText(RESOURCES.getString("label.mark") + " " + markValue.get());
               }
            };

      markButton.addActionListener(markButtonActionListener);

      resetBatteryEnergyButton.addActionListener(
            new ButtonTimeConsumingAction(this, resetBatteryEnergyButton)
            {
            protected Object executeTimeConsumingAction()
               {
               bmsController.resetBatteryEnergyEquation();
               markButtonActionListener.actionPerformed(null);  // log a mark here as well
               return null;
               }
            });

      // create the connection state status labels
      final JLabel bmsConnectionState = SwingUtils.createLabel(RESOURCES.getString("label.bms"));
      final JLabel gpsConnectionState = SwingUtils.createLabel(RESOURCES.getString("label.gps"));
      final JLabel motorControllerConnectionState = SwingUtils.createLabel(RESOURCES.getString("label.motor-controller"));
      final JLabel sensorBoardConnectionState = SwingUtils.createLabel(RESOURCES.getString("label.sensor-board"));

      // set the initial color to red
      bmsConnectionState.setForeground(HondaConstants.RED);
      gpsConnectionState.setForeground(HondaConstants.RED);
      motorControllerConnectionState.setForeground(HondaConstants.RED);
      sensorBoardConnectionState.setForeground(HondaConstants.RED);

      // configure the connection state status labels
      bmsConnectionState.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK),
                                                                      BorderFactory.createEmptyBorder(3, 3, 3, 3)));
      gpsConnectionState.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK),
                                                                      BorderFactory.createEmptyBorder(3, 3, 3, 3)));
      motorControllerConnectionState.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK),
                                                                                  BorderFactory.createEmptyBorder(3, 3, 3, 3)));
      sensorBoardConnectionState.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.BLACK),
                                                                              BorderFactory.createEmptyBorder(3, 3, 3, 3)));

      final JLabel batteryEquationEquals = SwingUtils.createLabel(RESOURCES.getString("label.equals"), GUIConstants.FONT_MEDIUM_LARGE);
      final JLabel batteryEquationPlus = SwingUtils.createLabel(RESOURCES.getString("label.plus"), GUIConstants.FONT_MEDIUM_LARGE);
/*
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
      row2.add(bmsView.getFaultStatusPanel());
      row2.add(Box.createGlue());
*/

      final JPanel mainGauges = new JPanel();
      final GridLayout layout = new GridLayout(1,2);
	  mainGauges.setOpaque(true);
	  mainGauges.setLayout(layout);
	  mainGauges.setPreferredSize(new Dimension(800,420));
	  mainGauges.add(motorControllerView.getRpmGauge());
	  mainGauges.add(bmsView.getStateOfChargeGauge());

	  final JPanel secGauges = new JPanel();
	  secGauges.setPreferredSize(new Dimension(800,420));
	  secGauges.setOpaque(false);
	  final BoxLayout secLayout = new BoxLayout(secGauges, BoxLayout.Y_AXIS);
	  secGauges.setLayout(secLayout);

	 //SideGauges: top bar to handle elements in top corners
	JPanel sideGauges = new JPanel();
	BoxLayout sidelayout = new BoxLayout(sideGauges, BoxLayout.X_AXIS);
	sideGauges.setLayout(sidelayout);
	sideGauges.setOpaque(false);
	sideGauges.setPreferredSize(new Dimension(800,120));
	sideGauges.setMaximumSize(new Dimension(800,120));
	sideGauges.setBackground(Color.blue);

	JPanel leftPanel = new JPanel();
	leftPanel.setPreferredSize(new Dimension(120,120));
	leftPanel.setMaximumSize(new Dimension(120,120));
	//leftPanel.setBackground(Color.gray);
	leftPanel.setOpaque(false);
	sideGauges.add(leftPanel);

	sideGauges.add(Box.createHorizontalGlue());


	JPanel rightPanel = new JPanel();
	rightPanel.setPreferredSize(new Dimension(120,120));
	rightPanel.setMaximumSize(new Dimension(120,120));
	//rightPanel.setBackground(Color.gray);
	rightPanel.setOpaque(false);
	sideGauges.add(rightPanel);

	secGauges.add(sideGauges);

	secGauges.add(Box.createGlue());

	//Subscreen: carded layout for the center area
	JPanel subScreen = new JPanel();
	CardLayout subLayout = new CardLayout();
	subScreen.setPreferredSize(new Dimension(450, 280));
	subScreen.setMaximumSize(new Dimension(450, 220));
	subScreen.setOpaque(false);
	subScreen.setLayout(subLayout);

	//subPage1: first card for subscreen
	JPanel subPage1 = new JPanel();
	subPage1.setMaximumSize(new Dimension(600, 200));
	BoxLayout page1layout = new BoxLayout(subPage1, BoxLayout.X_AXIS);
	subPage1.setLayout(page1layout);
	subPage1.setOpaque(false);
	//subPage1.setBackground(Color.green);
	subPage1.add(new ChargeGauge<Integer>(ChargeGauge.TYPE_ECO));
	subPage1.add(Box.createHorizontalGlue());
	//subScreen.add(bmsView.getMaximumCellVoltageGauge());
	subPage1.add(bmsView.getLoadCurrentAmpsGauge());

	subScreen.add(subPage1, CARD_HOME);
	secGauges.add(subScreen);
	
	  /*
      layout.setHorizontalGroup(
            layout.createSequentialGroup()
			.add(motorControllerView.getRpmGauge())
      		.add(bmsView.getStateOfChargeGauge())
	  );
      layout.setVerticalGroup(
            layout.createSequentialGroup()
			.add(motorControllerView.getRpmGauge())
      		.add(bmsView.getStateOfChargeGauge())
		);
		*/
      /*layout.setHorizontalGroup(
            layout.createSequentialGroup()
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                             .add(deadSpace)
                             .add(horizontalSpacer5)
                             .add(bmsView.getOverTemperatureGauge())
                  )
                  .add(verticalSpacer1)
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                             .add(sensorBoardView.getMotorTempGauge())
                             .add(horizontalSpacer1)
                             .add(bmsView.getMinimumCellTempGauge())
                             .add(horizontalSpacer2)
                             .add(bmsView.getMinimumCellVoltageGauge())
                             .add(horizontalSpacer3)
                             .add(bmsView.getPackTotalVoltageGauge())
                             .add(horizontalSpacer4)
                             .add(bmsView.getBatteryEnergyTotalGauge())
                             .add(bmsView.getUnderVoltageGauge())
                  )
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                             .add(verticalSpacer2)
                  )
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                             .add(sensorBoardView.getMotorControllerTempGauge())
                             .add(bmsView.getMaximumCellTempGauge())
                             .add(bmsView.getMaximumCellVoltageGauge())
                             .add(bmsView.getLoadCurrentAmpsGauge())
                             .add(batteryEquationEquals)
                             .add(bmsView.getOverVoltageGauge())
                  )
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                             .add(verticalSpacer3)
                  )
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                             .add(motorControllerView.getRpmGauge())
                             .add(bmsView.getAverageCellTempGauge())
                             .add(bmsView.getAverageCellVoltageGauge())
                             .add(bmsView.getDepthOfDischargeGauge())
                             .add(bmsView.getBatteryEnergyUsedGauge())
                             .add(bmsView.getChargeOvercurrentGauge())
                  )
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                             .add(verticalSpacer4)
                  )
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                             .add(bmsView.getLLIMSetGauge())
                             .add(bmsView.getCellNumWithLowestTempGauge())
                             .add(bmsView.getCellNumWithLowestVoltageGauge())
                             .add(batteryEquationPlus)
                             .add(bmsView.getDischargeOvercurrentGauge())
                  )
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                             .add(verticalSpacer5)
                  )
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                             .add(bmsView.getHLIMSetGauge())
                             .add(bmsView.getCellNumWithHighestTempGauge())
                             .add(bmsView.getCellNumWithHighestVoltageGauge())
                             .add(bmsView.getStateOfHealthGauge())
                             .add(bmsView.getBatteryEnergyRegenGauge())
                             .add(bmsView.getCommunicationFaultWithBankOrCellGauge())
                  )
                  .add(verticalSpacer6)
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                             .add(resetBatteryEnergyButton)
                             .add(bmsView.getInterlockTrippedGauge())
                  )
      );
      layout.setVerticalGroup(
            layout.createSequentialGroup()
                  .add(layout.createParallelGroup(GroupLayout.LEADING)
                             .add(verticalSpacer1)
                             .add(sensorBoardView.getMotorTempGauge())
                             .add(verticalSpacer2)
                             .add(sensorBoardView.getMotorControllerTempGauge())
                             .add(verticalSpacer3)
                             .add(motorControllerView.getRpmGauge())
                             .add(verticalSpacer4)
                             .add(bmsView.getLLIMSetGauge())
                             .add(verticalSpacer5)
                             .add(bmsView.getHLIMSetGauge())
                             .add(verticalSpacer6)
                  )
                  .add(layout.createParallelGroup(GroupLayout.LEADING)
                             .add(horizontalSpacer1)
                  )
                  .add(layout.createParallelGroup(GroupLayout.LEADING)
                             .add(bmsView.getMinimumCellTempGauge())
                             .add(bmsView.getMaximumCellTempGauge())
                             .add(bmsView.getAverageCellTempGauge())
                             .add(bmsView.getCellNumWithLowestTempGauge())
                             .add(bmsView.getCellNumWithHighestTempGauge())
                  )
                  .add(layout.createParallelGroup(GroupLayout.LEADING)
                             .add(horizontalSpacer2)
                  )
                  .add(layout.createParallelGroup(GroupLayout.LEADING)
                             .add(bmsView.getMinimumCellVoltageGauge())
                             .add(bmsView.getMaximumCellVoltageGauge())
                             .add(bmsView.getAverageCellVoltageGauge())
                             .add(bmsView.getCellNumWithLowestVoltageGauge())
                             .add(bmsView.getCellNumWithHighestVoltageGauge())
                  )
                  .add(layout.createParallelGroup(GroupLayout.LEADING)
                             .add(horizontalSpacer3)
                  )
                  .add(layout.createParallelGroup(GroupLayout.LEADING)
                             .add(bmsView.getStateOfChargeGauge())
                             .add(bmsView.getPackTotalVoltageGauge())
                             .add(bmsView.getLoadCurrentAmpsGauge())
                             .add(bmsView.getDepthOfDischargeGauge())
                             .add(bmsView.getStateOfHealthGauge())
                  )
                  .add(layout.createParallelGroup(GroupLayout.LEADING)
                             .add(horizontalSpacer4)
                  )
                  .add(layout.createParallelGroup(GroupLayout.LEADING)
                             .add(deadSpace)
                             .add(bmsView.getBatteryEnergyTotalGauge())
                             .add(batteryEquationEquals)
                             .add(bmsView.getBatteryEnergyUsedGauge())
                             .add(batteryEquationPlus)
                             .add(bmsView.getBatteryEnergyRegenGauge())
                             .add(resetBatteryEnergyButton)
                  )
                  .add(layout.createParallelGroup(GroupLayout.LEADING)
                             .add(horizontalSpacer5)
                  )
                  .add(layout.createParallelGroup(GroupLayout.LEADING)
                             .add(bmsView.getOverTemperatureGauge())
                             .add(bmsView.getUnderVoltageGauge())
                             .add(bmsView.getOverVoltageGauge())
                             .add(bmsView.getChargeOvercurrentGauge())
                             .add(bmsView.getDischargeOvercurrentGauge())
                             .add(bmsView.getCommunicationFaultWithBankOrCellGauge())
                             .add(bmsView.getInterlockTrippedGauge())
                  )
      );
		*/

      // register self as a connection state listener for the various models so we can display connection status
      bmsModel.addStreamingSerialPortDeviceConnectionStateListener(new DeviceConnectionStateListener(bmsConnectionState));
      gpsModel.addStreamingSerialPortDeviceConnectionStateListener(new DeviceConnectionStateListener(gpsConnectionState));
      motorControllerModel.addStreamingSerialPortDeviceConnectionStateListener(new DeviceConnectionStateListener(motorControllerConnectionState));
      sensorBoardModel.addStreamingSerialPortDeviceConnectionStateListener(new DeviceConnectionStateListener(sensorBoardConnectionState));

      //this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel displayArea = new UnoptimizedJPanel();
		displayArea.setPreferredSize(new Dimension(800, 420));
		displayArea.setMaximumSize(new Dimension(800, 420));

	  OverlayLayout displayLayout = new OverlayLayout(displayArea);
	  displayArea.setLayout(displayLayout);

      displayArea.add(secGauges);
      displayArea.add(mainGauges);


	  final BoxLayout mainLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
	  this.setLayout(mainLayout);

	  this.add(displayArea);





	  JPanel buttonPanel = new JPanel();
	  BoxLayout buttonLayout = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
	  buttonPanel.setLayout(buttonLayout);
	  
	  JButton b1 = new JButton("Car Info");
	  b1.setMaximumSize(new Dimension(600,60));
	  JButton b2 = new JButton("Home");
	  b2.setMaximumSize(new Dimension(600,60));
	  JButton b3 = new JButton("History");
	  b3.setMaximumSize(new Dimension(600,60));
	  buttonPanel.add(b1);
	  buttonPanel.add(b2);
	  buttonPanel.add(b3);
	  buttonPanel.setPreferredSize(new Dimension(800,60));

	  this.add(buttonPanel);


      //this.add(Box.createGlue());
//      this.add(row1);
      //this.add(SwingUtils.createRigidSpacer(20));
 //     this.add(row2);
      //this.add(SwingUtils.createRigidSpacer(20));
      //this.add(Box.createGlue());
      }

   private class UnoptimizedJPanel extends JPanel 
   {

	   public boolean isOptimizedDrawingEnabled(){

		   return false;
	   }
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
   public boolean isOptimizedDrawingEnabled(){

	   return false;
   }
   }
