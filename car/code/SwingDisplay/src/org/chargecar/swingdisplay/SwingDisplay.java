package org.chargecar.swingdisplay;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;
import edu.cmu.ri.createlab.userinterface.util.SwingWorker;
import edu.cmu.ri.createlab.util.runtime.LifecycleManager;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.log4j.Logger;
import org.chargecar.honda.*;
import org.chargecar.honda.bms.BMSController;
import org.chargecar.honda.bms.BMSModel;
import org.chargecar.swingdisplay.views.BMSView;
import org.chargecar.honda.gps.GPSController;
import org.chargecar.honda.gps.GPSModel;
import org.chargecar.honda.gps.GPSView;
import org.chargecar.honda.motorcontroller.MotorControllerController;
import org.chargecar.honda.motorcontroller.MotorControllerModel;
//import org.chargecar.honda.motorcontroller.MotorControllerView;
import org.chargecar.swingdisplay.views.MotorControllerView;
import org.chargecar.honda.sensorboard.SensorBoardController;
import org.chargecar.honda.sensorboard.SensorBoardModel;
import org.chargecar.honda.sensorboard.SensorBoardView;
import org.chargecar.serial.streaming.StreamingSerialPortDeviceConnectionStateListener;
import org.jdesktop.layout.GroupLayout;

//import org.chargecar.lcddisplay.LCDConnectivityManager;
import org.chargecar.lcddisplay.*;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class SwingDisplay
   {
   private static final Logger LOG = Logger.getLogger(SwingDisplay.class);
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(SwingDisplay.class.getName());
   private static final String APPLICATION_NAME = RESOURCES.getString("application.name");

   public static void main(final String[] args)
      {
      final Map<String, String> deviceToSerialPortNameMap = new HashMap<String, String>(4);

      for (final String arg : args)
         {
         final String[] keyValue = arg.split("=");
         if (keyValue.length == 2)
            {
            LOG.debug("Associating [" + keyValue[0] + "] with serial port [" + keyValue[1] + "]");
            deviceToSerialPortNameMap.put(keyValue[0].toLowerCase(), keyValue[1]);
            }
         else
            {
            LOG.info("Ignoring unexpected switch [" + arg + "]");
            }
         }

      SwingUtilities.invokeLater(
            new Runnable()
            {
            public void run()
               {
               new SwingDisplay(deviceToSerialPortNameMap);
               }
            });
      }

   private SwingDisplay(final Map<String, String> deviceToSerialPortNameMap)
      {
      // create and configure the GUI
      final JPanel panel = new JPanel();
      panel.setBackground(Color.WHITE);
      final JFrame jFrame = new JFrame(APPLICATION_NAME);
      jFrame.setContentPane(panel);

      // create the models
      final BMSModel bmsModel = new BMSModel();
      final GPSModel gpsModel = new GPSModel();
      final MotorControllerModel motorControllerModel = new MotorControllerModel();
      final SensorBoardModel sensorBoardModel = new SensorBoardModel();

      // create the controllers
      final BMSController bmsController = BMSController.create(deviceToSerialPortNameMap.get("bms"), bmsModel);
      final GPSController gpsController = GPSController.create(deviceToSerialPortNameMap.get("gps"), gpsModel);
      final MotorControllerController motorControllerController = MotorControllerController.create(deviceToSerialPortNameMap.get("motor-controller"), motorControllerModel);
      final SensorBoardController sensorBoardController = SensorBoardController.create(deviceToSerialPortNameMap.get("sensor-board"), sensorBoardModel);

      final LifecycleManager lifecycleManager = new MyLifecycleManager(jFrame,
                                                                       bmsController,
                                                                       gpsController,
                                                                       motorControllerController,
                                                                       sensorBoardController);

	  System.out.println("done with livecycle stuff. gonna try LCD");
      final SwingDisplayController inDashDisplayController = new SwingDisplayController(lifecycleManager);


	  BMSManager.getInstance();

	  final LCD lcd = LCDProxy.getInstance();

	  lcd.setText(1,1,"hello");
      // create the views
      final BMSView bmsView = new BMSView();
      final GPSView gpsView = new GPSView();
      final MotorControllerView motorControllerView = new MotorControllerView();
      final SensorBoardView sensorBoardView = new SensorBoardView();
	  
      final SwingDisplayView swingDisplayView = new SwingDisplayView(inDashDisplayController,
                                                                        bmsController,
                                                                        bmsModel,
                                                                        gpsModel,
                                                                        motorControllerModel,
                                                                        sensorBoardModel,
                                                                        bmsView,
                                                                        motorControllerView,
                                                                        sensorBoardView);

      // add the various views as data event listeners to the models
      bmsModel.addEventListener(bmsView);
      gpsModel.addEventListener(gpsView);
      motorControllerModel.addEventListener(motorControllerView);
      sensorBoardModel.addEventListener(sensorBoardView);

      // add the various views as connection state listeners to the models
      bmsModel.addStreamingSerialPortDeviceConnectionStateListener(bmsView);
      gpsModel.addStreamingSerialPortDeviceConnectionStateListener(gpsView);
      motorControllerModel.addStreamingSerialPortDeviceConnectionStateListener(motorControllerView);
      sensorBoardModel.addStreamingSerialPortDeviceConnectionStateListener(sensorBoardView);

      // add the various views as reading state listeners to the models
      bmsModel.addStreamingSerialPortDeviceReadingStateListener(bmsView);
      gpsModel.addStreamingSerialPortDeviceReadingStateListener(gpsView);
      motorControllerModel.addStreamingSerialPortDeviceReadingStateListener(motorControllerView);
      sensorBoardModel.addStreamingSerialPortDeviceReadingStateListener(sensorBoardView);

      // add a listener to each model which starts the reading upon connection establishment
      bmsModel.addStreamingSerialPortDeviceConnectionStateListener(
            new StreamingSerialPortDeviceConnectionStateListener()
            {
            public void handleConnectionStateChange(final boolean isConnected)
               {
               if (isConnected)
                  {
                  bmsController.startReading();
                  }
               }
            });
      gpsModel.addStreamingSerialPortDeviceConnectionStateListener(
            new StreamingSerialPortDeviceConnectionStateListener()
            {
            public void handleConnectionStateChange(final boolean isConnected)
               {
               if (isConnected)
                  {
                  gpsController.startReading();
                  }
               }
            });
      motorControllerModel.addStreamingSerialPortDeviceConnectionStateListener(
            new StreamingSerialPortDeviceConnectionStateListener()
            {
            public void handleConnectionStateChange(final boolean isConnected)
               {
               if (isConnected)
                  {
                  motorControllerController.startReading();
                  }
               }
            });
      sensorBoardModel.addStreamingSerialPortDeviceConnectionStateListener(
            new StreamingSerialPortDeviceConnectionStateListener()
            {
            public void handleConnectionStateChange(final boolean isConnected)
               {
               if (isConnected)
                  {
                  sensorBoardController.startReading();
                  }
               }
            });

      // setup the layout
      final GroupLayout layout = new GroupLayout(panel);
      final Component leftGlue = Box.createGlue();
      final Component rightGlue = Box.createGlue();
      panel.setLayout(layout);
      layout.setHorizontalGroup(
            layout.createSequentialGroup()
                  .add(leftGlue)
                  .add(swingDisplayView)
                  .add(rightGlue)
      );
      layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.CENTER)
                  .add(leftGlue)
                  .add(swingDisplayView)
                  .add(rightGlue)
      );
      jFrame.pack();

      // set various properties for the JFrame
      jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      jFrame.setBackground(Color.WHITE);
      jFrame.setResizable(true);
      jFrame.addWindowListener(
            new WindowAdapter()
            {
            public void windowOpened(final WindowEvent e)
               {
               LOG.debug("SwingDisplay.windowOpened()");
               lifecycleManager.startup();
               }

            public void windowClosing(final WindowEvent event)
               {
               lifecycleManager.shutdown();
               }
            });

      jFrame.setVisible(true);
      }

   private static class MyLifecycleManager implements LifecycleManager
      {
      private final Runnable startupRunnable;
      private final Runnable shutdownRunnable;
      private final JFrame jFrame;
      private final ExecutorService executor = Executors.newCachedThreadPool(new DaemonThreadFactory("MyLifecycleManager.executor"));

      private MyLifecycleManager(final JFrame jFrame,
                                 final BMSController bmsController,
                                 final GPSController gpsController,
                                 final MotorControllerController motorControllerController,
                                 final SensorBoardController sensorBoardController)
         {
         this.jFrame = jFrame;
         startupRunnable =
               new Runnable()
               {
               private void connect(final String deviceName, final StreamingSerialPortDeviceController controller)
                  {
                  if (controller == null)
                     {
                     LOG.info("SwingDisplay$MyLifecycleManager.run(): Controller for the " + deviceName + " given to the LifecycleManager constructor was null, so data won't be read.");
                     }
                  else
                     {
                     executor.submit(
                           new Runnable()
                           {
                           public void run()
                              {
                              LOG.info("SwingDisplay$MyLifecycleManager.run(): Attempting to establish a connection to the " + deviceName + "...");
                              controller.connect();
                              }
                           });
                     }
                  }

               public void run()
                  {
                  connect("BMS", bmsController);
                  connect("GPS", gpsController);
                  connect("Motor Controller", motorControllerController);
                  connect("Sensor Board", sensorBoardController);
                  }
               };

         shutdownRunnable =
               new Runnable()
               {
               private void disconnect(final String deviceName, final StreamingSerialPortDeviceController controller)
                  {
                  if (controller == null)
                     {
                     LOG.info("SwingDisplay$MyLifecycleManager.run(): Controller for the " + deviceName + " given to the LifecycleManager constructor was null, so we won't try to shut it down.");
                     }
                  else
                     {
                     executor.submit(
                           new Runnable()
                           {
                           public void run()
                              {
                              LOG.info("SwingDisplay$MyLifecycleManager.run(): Disconnecting from the " + deviceName + "...");
                              controller.disconnect();
                              }
                           });
                     }
                  }

               public void run()
                  {
                  disconnect("BMS", bmsController);
                  disconnect("GPS", gpsController);
                  disconnect("Motor Controller", motorControllerController);
                  disconnect("Sensor Board", sensorBoardController);

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
