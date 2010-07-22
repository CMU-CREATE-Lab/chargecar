package org.chargecar.honda;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import edu.cmu.ri.createlab.serial.SerialPortException;
import edu.cmu.ri.createlab.userinterface.util.DialogHelper;
import edu.cmu.ri.createlab.userinterface.util.SwingWorker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.honda.gps.NMEAEvent;
import org.chargecar.honda.gps.NMEAReader;
import org.chargecar.honda.motorcontroller.MotorControllerEvent;
import org.chargecar.honda.motorcontroller.MotorControllerReader;
import org.chargecar.honda.sensorboard.FakeSensorBoard;
import org.chargecar.honda.sensorboard.SensorBoardEvent;
import org.chargecar.honda.sensorboard.SensorBoardReader;
import org.chargecar.serial.streaming.StreamingSerialPortEventListener;
import org.chargecar.serial.streaming.StreamingSerialPortReader;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr"})
public final class InDashDisplay
   {
   private static final Log LOG = LogFactory.getLog(InDashDisplay.class);
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(InDashDisplay.class.getName());
   private static final String APPLICATION_NAME = RESOURCES.getString("application.name");

   public static void main(final String[] args)
      {
      final SensorBoardReader sensorBoardReader;
      final MotorControllerReader motorControllerReader;
      final NMEAReader nmeaReader;

      // see whether we're using real or fake devices
      if (Boolean.valueOf(System.getProperty("use-fake-devices", "false")))
         {
         LOG.debug("InDashDisplay.main(): Using fake serial devices");
         sensorBoardReader = (SensorBoardReader)connectToStreamingSerialPortReader("Fake Sensor Board", new SensorBoardReader(new FakeSensorBoard()));
         motorControllerReader = null;//(MotorControllerReader)connectToStreamingSerialPortReader("Fake Motor Controller", new MotorControllerReader(new FakeMotorController()));
         nmeaReader = null;//(NMEAReader)connectToStreamingSerialPortReader("Fake GPS", new NMEAReader(new FakeGPS()));
         }
      else
         {
         LOG.debug("InDashDisplay.main(): Using real serial devices");
         if (args.length < 3)
            {
            System.err.println("Usage:  InDashDisplay <SENSOR_BOARD_SERIAL_PORT_NAME> <MOTOR_CONTROLLER_SERIAL_PORT_NAME> <GPS_SERIAL_PORT_NAME>");
            System.exit(1);
            }
         sensorBoardReader = (SensorBoardReader)connectToStreamingSerialPortReader("Sensor Board", new SensorBoardReader(args[0]));
         motorControllerReader = (MotorControllerReader)connectToStreamingSerialPortReader("Motor Controller", new MotorControllerReader(args[1]));
         nmeaReader = (NMEAReader)connectToStreamingSerialPortReader("GPS", new NMEAReader(args[1]));
         }

      SwingUtilities.invokeLater(
            new Runnable()
            {
            public void run()
               {
               new InDashDisplay(sensorBoardReader, motorControllerReader, nmeaReader);
               }
            });
      }

   private static StreamingSerialPortReader connectToStreamingSerialPortReader(final String name,
                                                                               final StreamingSerialPortReader reader)
      {
      try
         {
         // TODO: do something better here
         if (reader.connect())
            {
            return reader;
            }
         else
            {
            LOG.error("InDashDisplay.connectToStreamingSerialPortReader(): failed to connect to the " + name);
            }
         }
      catch (SerialPortException e)
         {
         LOG.error("SerialPortException while connecting to the " + name, e);
         }
      catch (IOException e)
         {
         LOG.error("IOException while connecting to the " + name, e);
         }
      catch (Exception e)
         {
         LOG.error("Exception while connecting to the " + name, e);
         }

      return null;
      }

   private InDashDisplay(final SensorBoardReader sensorBoardReader,
                         final MotorControllerReader motorControllerReader,
                         final NMEAReader nmeaReader)
      {
      // create and configure the GUI
      final JPanel panel = new JPanel();
      panel.setBackground(Color.WHITE);
      final JFrame jFrame = new JFrame(APPLICATION_NAME);
      jFrame.setContentPane(panel);

      panel.add(new JLabel("Hello World!")); // TODO

      if (sensorBoardReader != null)
         {
         sensorBoardReader.addEventListener(
               new StreamingSerialPortEventListener<SensorBoardEvent>()
               {
               public void handleEvent(final SensorBoardEvent sensorBoardEvent)
                  {
                  if (LOG.isInfoEnabled())
                     {
                     LOG.info(sensorBoardEvent.toLoggingString());
                     }
                  }
               });
         }

      if (motorControllerReader != null)
         {
         motorControllerReader.addEventListener(
               new StreamingSerialPortEventListener<MotorControllerEvent>()
               {
               public void handleEvent(final MotorControllerEvent motorControllerEvent)
                  {
                  if (LOG.isInfoEnabled())
                     {
                     LOG.info(motorControllerEvent.toLoggingString());
                     }
                  }
               });
         }

      if (nmeaReader != null)
         {
         nmeaReader.addEventListener(
               new StreamingSerialPortEventListener<NMEAEvent>()
               {
               public void handleEvent(final NMEAEvent nmeaEvent)
                  {
                  if (LOG.isInfoEnabled())
                     {
                     LOG.info(nmeaEvent.toLoggingString());
                     }
                  }
               });
         }

      final LifecycleManager lifecycleManager = new MyLifecycleManager(jFrame, sensorBoardReader, motorControllerReader, nmeaReader);

      // set various properties for the JFrame
      jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      jFrame.setBackground(Color.WHITE);
      jFrame.setResizable(true);    // This *should* be false, but full-screen doesn't work on Linux unless it's true
      jFrame.addWindowListener(
            new WindowAdapter()
            {
            public void windowOpened(final WindowEvent e)
               {
               LOG.debug("InDashDisplay.windowOpened()");
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

      private MyLifecycleManager(final JFrame jFrame,
                                 final SensorBoardReader sensorBoardReader,
                                 final MotorControllerReader motorControllerReader,
                                 final NMEAReader nmeaReader)
         {
         this.jFrame = jFrame;
         startupRunnable =
               new Runnable()
               {
               public void run()
                  {
                  // start scanning
                  if (sensorBoardReader == null)
                     {
                     LOG.info("InDashDisplay$MyLifecycleManager.run(): SensorBoardReader given to the LifecycleManager constructor was null, so Sensor Board data won't be read.");
                     }
                  else
                     {
                     LOG.info("InDashDisplay$MyLifecycleManager.run(): Starting the SensorBoardReader...");
                     sensorBoardReader.startReading();
                     }

                  if (motorControllerReader == null)
                     {
                     LOG.info("InDashDisplay$MyLifecycleManager.run(): MotorControllerReader given to the LifecycleManager constructor was null, so Motor Controller data won't be read.");
                     }
                  else
                     {
                     LOG.info("InDashDisplay$MyLifecycleManager.run(): Starting the MotorControllerReader...");
                     motorControllerReader.startReading();
                     }

                  if (nmeaReader == null)
                     {
                     LOG.info("InDashDisplay$MyLifecycleManager.run(): NMEAReader given to the LifecycleManager constructor was null, so GPS data won't be read.");
                     }
                  else
                     {
                     LOG.info("InDashDisplay$MyLifecycleManager.run(): Starting the NMEAReader...");
                     nmeaReader.startReading();
                     }
                  }
               };

         shutdownRunnable =
               new Runnable()
               {
               public void run()
                  {
                  // disconnect so we can exit gracefully
                  if (sensorBoardReader == null)
                     {
                     LOG.info("InDashDisplay$MyLifecycleManager.run(): SensorBoardReader given to the LifecycleManager constructor was null, so we won't try to shut it down.");
                     }
                  else
                     {
                     LOG.info("InDashDisplay$MyLifecycleManager.run(): Stopping the SensorBoardReader...");
                     sensorBoardReader.stopReading();
                     LOG.info("InDashDisplay$MyLifecycleManager.run(): Disconnecting from the Sensor Board...");
                     sensorBoardReader.disconnect();
                     }

                  if (motorControllerReader == null)
                     {
                     LOG.info("InDashDisplay$MyLifecycleManager.run(): MotorControllerReader given to the LifecycleManager constructor was null, so we won't try to shut it down.");
                     }
                  else
                     {
                     LOG.info("InDashDisplay$MyLifecycleManager.run(): Stopping the MotorControllerReader...");
                     motorControllerReader.stopReading();
                     LOG.info("InDashDisplay$MyLifecycleManager.run(): Disconnecting from the Motor Controller...");
                     motorControllerReader.disconnect();
                     }

                  if (nmeaReader == null)
                     {
                     LOG.info("InDashDisplay$MyLifecycleManager.run(): NMEAReader given to the LifecycleManager constructor was null, so we won't try to shut it down.");
                     }
                  else
                     {
                     LOG.info("InDashDisplay$MyLifecycleManager.run(): Stopping the NMEAReader...");
                     nmeaReader.stopReading();
                     LOG.info("InDashDisplay$MyLifecycleManager.run(): Disconnecting from the GPS...");
                     nmeaReader.disconnect();
                     }

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
