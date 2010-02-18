package org.chargecar;

import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import edu.cmu.ri.createlab.serial.SerialPortException;
import edu.cmu.ri.createlab.serial.device.connectivity.SerialDeviceConnectionEventListener;
import edu.cmu.ri.createlab.serial.device.connectivity.SerialDeviceConnectionState;
import edu.cmu.ri.createlab.serial.device.connectivity.SerialDeviceConnectivityManager;
import edu.cmu.ri.createlab.serial.device.connectivity.SerialDeviceConnectivityManagerImpl;
import edu.cmu.ri.createlab.userinterface.component.Spinner;
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;
import edu.cmu.ri.createlab.userinterface.util.SwingWorker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.gps.GPSEventListener;
import org.chargecar.gps.nmea.NMEAReader;
import org.chargecar.sensorboard.PowerController;
import org.chargecar.sensorboard.PowerModel;
import org.chargecar.sensorboard.PowerView;
import org.chargecar.sensorboard.SpeedAndOdometryModel;
import org.chargecar.sensorboard.SpeedAndOdometryView;
import org.chargecar.sensorboard.TemperaturesModel;
import org.chargecar.sensorboard.TemperaturesView;
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
      final NMEAReader gpsReader = new NMEAReader(APPLICATION_NAME);
      if (args.length < 1)
         {
         LOG.warn("GPS receiver serial port not specified, so no GPS data will be available!");
         }
      else
         {
         final String gpsSerialPortName = args[0];
         try
            {
            gpsReader.connect(gpsSerialPortName);
            }
         catch (SerialPortException e)
            {
            LOG.error("SerialPortException while connecting to the GPS receiver", e);
            }
         catch (IOException e)
            {
            LOG.error("IOException while connecting to the GPS receiver", e);
            }
         }

      //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
      SwingUtilities.invokeLater(
            new Runnable()
            {
            public void run()
               {
               final GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
               final GraphicsDevice graphicsDevice = graphicsEnvironment.getDefaultScreenDevice();
               try
                  {
                  new HeadsUpDisplay(graphicsDevice, gpsReader);
                  }
               finally
                  {
                  // see if we can use full-screen mode
                  if (graphicsDevice.isFullScreenSupported())
                     {
                     // Exit full-screen mode
                     graphicsDevice.setFullScreenWindow(null);
                     }
                  }
               }
            });
      }

   private final SetStageRunnable setStageForIsScanningRunnable;
   private final SetStageRunnable setStageForIsConnectedRunnable;
   private final SetStageRunnable setStageForIsDisconnectedRunnable;

   private HeadsUpDisplay(final GraphicsDevice graphicsDevice, final NMEAReader gpsReader)
      {
      gpsReader.addEventListener(
            new GPSEventListener()
            {
            public void handleLocationEvent(final String latitude, final String longitude, final int numSatellitesBeingTracked)
               {
               LOG.info("GPS: " + latitude + "\t" + longitude + "\t" + numSatellitesBeingTracked);
               }

            public void handleElevationEvent(final int elevationInFeet)
               {
               LOG.info("GPS Elevation: " + elevationInFeet);
               }
            });

      // create and configure the GUI
      final JPanel panel = new JPanel();
      panel.setBackground(Color.WHITE);
      final JFrame jFrame = new JFrame(APPLICATION_NAME);
      jFrame.setUndecorated(true);
      jFrame.setContentPane(panel);

      // Enter full-screen mode
      graphicsDevice.setFullScreenWindow(jFrame);

      // create the models
      final SpeedAndOdometryModel speedAndOdometryModel = new SpeedAndOdometryModel();
      final TemperaturesModel temperaturesModel = new TemperaturesModel();
      final PowerModel powerModel = new PowerModel();
      final PowerController powerController = new PowerController(powerModel);

      // create and configure the SerialDeviceConnectivityManager, LifecycleManager, and HeadsUpDisplayController
      final SerialDeviceConnectivityManager serialDeviceConnectivityManager = new SerialDeviceConnectivityManagerImpl(new SensorBoardSerialDeviceProxyCreator());
      final LifecycleManager lifecycleManager = new MyLifecycleManager(jFrame, graphicsDevice, serialDeviceConnectivityManager, gpsReader);
      final HeadsUpDisplayController headsUpDisplayController = new HeadsUpDisplayController(lifecycleManager, serialDeviceConnectivityManager, speedAndOdometryModel, temperaturesModel, powerModel);
      serialDeviceConnectivityManager.addConnectionEventListener(
            new SerialDeviceConnectionEventListener()
            {
            public void handleConnectionStateChange(final SerialDeviceConnectionState oldState, final SerialDeviceConnectionState newState, final String serialPortName)
               {
               // don't bother doing anything if the state hasn't changed
               if (oldState.equals(newState))
                  {
                  return;
                  }

               if (SerialDeviceConnectionState.SCANNING.equals(newState))
                  {
                  SwingUtilities.invokeLater(setStageForIsScanningRunnable);
                  }
               else if (SerialDeviceConnectionState.CONNECTED.equals(newState))
                  {
                  SwingUtilities.invokeLater(setStageForIsConnectedRunnable);
                  }
               else
                  {
                  SwingUtilities.invokeLater(setStageForIsDisconnectedRunnable);
                  }
               }
            });

      // create the views
      final SpeedAndOdometryView speedAndOdometryView = new SpeedAndOdometryView();
      final TemperaturesView temperaturesView = new TemperaturesView();
      final PowerView powerView = new PowerView(powerController);
      final HeadsUpDisplayView headsUpDisplayView = new HeadsUpDisplayView(headsUpDisplayController,
                                                                           speedAndOdometryView,
                                                                           temperaturesView,
                                                                           powerView,
                                                                           powerController);

      // set up the runnables used to toggle the UI for scanning/connected/disconnected
      final Spinner spinner = new Spinner(RESOURCES.getString("label.connecting-to-sensor-board"));
      setStageForIsScanningRunnable = new SetStageRunnable(panel, spinner);
      setStageForIsConnectedRunnable = new SetStageRunnable(panel, headsUpDisplayView);
      setStageForIsDisconnectedRunnable = new SetStageRunnable(panel, new JLabel(RESOURCES.getString("label.disconnected")));
      setStageForIsScanningRunnable.run();

      // add the various views as listeners to the models
      speedAndOdometryModel.addEventListener(speedAndOdometryView);
      temperaturesModel.addEventListener(temperaturesView);
      powerModel.addEventListener(powerView);

      // set various properties for the JFrame
      jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      jFrame.setBackground(Color.WHITE);
      jFrame.setResizable(true);    // This *should* be false, but full-screen doesn't work on Linux unless it's true
      jFrame.addWindowListener(
            new WindowAdapter()
            {
            public void windowOpened(final WindowEvent e)
               {
               LOG.debug("HeadsUpDisplay.windowOpened()");
               lifecycleManager.startup();
               }

            public void windowClosing(final WindowEvent event)
               {
               lifecycleManager.shutdown();
               }
            });

      jFrame.setVisible(true);
      }

   private class SetStageRunnable implements Runnable
      {
      private final JPanel parentContainer;
      private final Component component;

      private SetStageRunnable(final JPanel parentPanel, final Component component)
         {
         this.parentContainer = parentPanel;
         this.component = component;
         }

      public void run()
         {
         parentContainer.removeAll();

         final GroupLayout layout = new GroupLayout(parentContainer);
         final Component leftGlue = Box.createGlue();
         final Component rightGlue = Box.createGlue();
         parentContainer.setLayout(layout);
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

   private static class MyLifecycleManager implements LifecycleManager
      {
      private final Runnable startupRunnable;
      private final Runnable shutdownRunnable;
      private final JFrame jFrame;

      private MyLifecycleManager(final JFrame jFrame, final GraphicsDevice graphicsDevice, final SerialDeviceConnectivityManager serialDeviceConnectivityManager, final NMEAReader gpsReader)
         {
         this.jFrame = jFrame;
         startupRunnable =
               new Runnable()
               {
               public void run()
                  {
                  // start scanning
                  serialDeviceConnectivityManager.scanAndConnect();
                  gpsReader.startReading();
                  }
               };

         shutdownRunnable =
               new Runnable()
               {
               public void run()
                  {
                  // disconnect so we can exit gracefully
                  serialDeviceConnectivityManager.disconnect();
                  gpsReader.stopReading();
                  gpsReader.disconnect();

                  // Exit full-screen mode
                  graphicsDevice.setFullScreenWindow(null);

                  System.exit(0);
                  }
               };
         }

      public void startup()
         {
         LOG.debug("LifecycleManager.startup()");

         run(startupRunnable);
         }

      public void shutdown()
         {
         LOG.debug("LifecycleManager.shutdown()");

         // ask if the user really wants to exit
         if (DialogHelper.showYesNoDialog(RESOURCES.getString("dialog.title.exit-confirmation"),
                                          RESOURCES.getString("dialog.message.exit-confirmation"),
                                          jFrame))
            {
            run(shutdownRunnable);
            }
         }

      private void run(final Runnable runnable)
         {
         if (SwingUtilities.isEventDispatchThread())
            {
            final SwingWorker worker =
                  new SwingWorker()
                  {
                  public Object construct()
                     {
                     runnable.run();
                     return null;
                     }
                  };
            worker.start();
            }
         else
            {
            runnable.run();
            }
         }
      }
   }
