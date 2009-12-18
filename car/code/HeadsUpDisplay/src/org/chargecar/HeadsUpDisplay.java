package org.chargecar;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.PropertyResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import edu.cmu.ri.createlab.serial.device.connectivity.SerialDeviceConnectionEventListener;
import edu.cmu.ri.createlab.serial.device.connectivity.SerialDeviceConnectionState;
import edu.cmu.ri.createlab.serial.device.connectivity.SerialDeviceConnectivityManager;
import edu.cmu.ri.createlab.serial.device.connectivity.SerialDeviceConnectivityManagerImpl;
import edu.cmu.ri.createlab.userinterface.component.Spinner;
import edu.cmu.ri.createlab.userinterface.util.SwingWorker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.sensorboard.serial.proxy.SensorBoardSerialDeviceProxyCreator;
import org.jdesktop.layout.GroupLayout;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class HeadsUpDisplay
   {
   private static final Log LOG = LogFactory.getLog(HeadsUpDisplay.class);
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(HeadsUpDisplay.class.getName());
   private static final String APPLICATION_NAME = RESOURCES.getString("application.name");

   public static void main(final String[] args)
      {
      //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
      SwingUtilities.invokeLater(
            new Runnable()
            {
            public void run()
               {
               new HeadsUpDisplay();
               }
            });
      }

   private final SetStageRunnable setStageForIsScanningRunnable;
   private final SetStageRunnable setStageForIsNotScanningRunnable;

   private HeadsUpDisplay()
      {
      // create and configure the SerialDeviceConnectivityManager
      final SerialDeviceConnectivityManager serialDeviceConnectivityManager = new SerialDeviceConnectivityManagerImpl(new SensorBoardSerialDeviceProxyCreator());
      serialDeviceConnectivityManager.addConnectionEventListener(new SensorBoardConnectionEventListener());

      // create and configure the GUI
      final JFrame jFrame = new JFrame(APPLICATION_NAME);

      // create the main panel for the JFrame
      final JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      // add the panel to the JFrame
      jFrame.add(panel);

      // set up the runnables used to toggle the UI for scanning/not scanning
      final Spinner spinner = new Spinner(RESOURCES.getString("label.connecting-to-sensor-board"));
      setStageForIsScanningRunnable = new SetStageRunnable(panel, spinner);
      setStageForIsNotScanningRunnable = new SetStageRunnable(panel, new JLabel("Connected!"));
      setStageForIsScanningRunnable.run();

      // set various properties for the JFrame
      jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      jFrame.setBackground(Color.WHITE);
      jFrame.setResizable(true);
      jFrame.addWindowListener(
            new WindowAdapter()
            {
            public void windowOpened(final WindowEvent e)
               {
               LOG.debug("HeadsUpDisplay.windowOpened()");
               final SwingWorker worker =
                     new SwingWorker()
                     {
                     public Object construct()
                        {
                        // start scanning
                        serialDeviceConnectivityManager.scanAndConnect();
                        return null;
                        }
                     };
               worker.start();
               }

            public void windowClosing(final WindowEvent event)
               {
               // ask if the user really wants to exit
               final int selectedOption = JOptionPane.showConfirmDialog(jFrame,
                                                                        RESOURCES.getString("dialog.message.exit-confirmation"),
                                                                        RESOURCES.getString("dialog.title.exit-confirmation"),
                                                                        JOptionPane.YES_NO_OPTION,
                                                                        JOptionPane.QUESTION_MESSAGE);

               if (selectedOption == JOptionPane.YES_OPTION)
                  {
                  final SwingWorker worker =
                        new SwingWorker()
                        {
                        public Object construct()
                           {
                           // disconnect so we can exit gracefully
                           serialDeviceConnectivityManager.disconnect();
                           return null;
                           }

                        public void finished()
                           {
                           System.exit(0);
                           }
                        };
                  worker.start();
                  }
               }
            });
      jFrame.pack();
      jFrame.setLocationRelativeTo(null);// center the window on the screen
      jFrame.setVisible(true);
      }

   private class SetStageRunnable implements Runnable
      {
      private final JPanel parentPanel;
      private final Component component;

      private SetStageRunnable(final JPanel parentPanel, final Component component)
         {
         this.parentPanel = parentPanel;
         this.component = component;
         }

      public void run()
         {
         parentPanel.removeAll();

         final GroupLayout layout = new GroupLayout(parentPanel);
         final Component leftGlue = Box.createGlue();
         final Component rightGlue = Box.createGlue();
         parentPanel.setLayout(layout);
         layout.setHorizontalGroup(
               layout.createSequentialGroup()
                     .add(leftGlue)
                     .add(component)
                     .add(rightGlue)
         );
         layout.setVerticalGroup(
               layout.createParallelGroup(GroupLayout.CENTER)
                     .add(leftGlue)
                     .add(component)
                     .add(rightGlue)
         );
         }
      }

   private class SensorBoardConnectionEventListener implements SerialDeviceConnectionEventListener
      {
      public void handleConnectionStateChange(final SerialDeviceConnectionState oldState, final SerialDeviceConnectionState newState, final String serialPortName)
         {
         if (LOG.isDebugEnabled())
            {
            LOG.debug("HeadsUpDisplay$SensorBoardConnectionEventListener.handleConnectionStateChange(" + oldState.name() + "," + newState.name() + "," + serialPortName + ")");
            }

         if (SerialDeviceConnectionState.SCANNING.equals(newState))
            {
            SwingUtilities.invokeLater(setStageForIsScanningRunnable);
            }
         else if (SerialDeviceConnectionState.CONNECTED.equals(newState))
            {
            SwingUtilities.invokeLater(setStageForIsNotScanningRunnable);
            }
         }
      }
   }
