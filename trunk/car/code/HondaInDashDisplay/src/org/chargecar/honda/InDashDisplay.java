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
import org.chargecar.honda.sensorboard.FakeSensorBoard;
import org.chargecar.honda.sensorboard.SensorBoardEvent;
import org.chargecar.honda.sensorboard.SensorBoardReader;
import org.chargecar.serial.streaming.StreamingSerialPortEventListener;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class InDashDisplay
   {
   private static final Log LOG = LogFactory.getLog(InDashDisplay.class);
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(InDashDisplay.class.getName());
   private static final String APPLICATION_NAME = RESOURCES.getString("application.name");

   public static void main(final String[] args)
      {
      SensorBoardReader tempSensorBoardReader;
      if (args.length < 1)
         {
         LOG.warn("Sensor Board serial port not specified, so no Sensor Board data will be available!");
         tempSensorBoardReader = null;
         }
      else
         {
         //tempSensorBoardReader = new SensorBoardReader(args[0]);
         tempSensorBoardReader = new SensorBoardReader(new FakeSensorBoard());
         try
            {
            // TODO: do something better here
            if (!tempSensorBoardReader.connect())
               {
               LOG.error("InDashDisplay.main(): failed to connect to the Sensor Board");
               tempSensorBoardReader = null;
               }
            }
         catch (SerialPortException e)
            {
            LOG.error("SerialPortException while connecting to the Sensor Board.  Setting the SensorBoardReader to null, so no reading will be attempted.", e);
            tempSensorBoardReader = null;
            }
         catch (IOException e)
            {
            LOG.error("IOException while connecting to the Sensor Board.  Setting the SensorBoardReader to null, so no reading will be attempted.", e);
            tempSensorBoardReader = null;
            }
         catch (Exception e)
            {
            LOG.error("Exception while connecting to the Sensor Board.  Setting the SensorBoardReader to null, so no reading will be attempted.", e);
            tempSensorBoardReader = null;
            }
         }

      //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
      final SensorBoardReader sensorBoardReader = tempSensorBoardReader;
      SwingUtilities.invokeLater(
            new Runnable()
            {
            public void run()
               {
               new InDashDisplay(sensorBoardReader);
               }
            });
      }

   private InDashDisplay(final SensorBoardReader sensorBoardReader)
      {
      // create and configure the GUI
      final JPanel panel = new JPanel();
      panel.setBackground(Color.WHITE);
      final JFrame jFrame = new JFrame(APPLICATION_NAME);
      jFrame.setContentPane(panel);

      panel.add(new JLabel("Hello World!")); // TODO

      if (sensorBoardReader != null)
         {
         sensorBoardReader.addEventListener(new StreamingSerialPortEventListener<SensorBoardEvent>()
         {
         public void handleEvent(final SensorBoardEvent sensorBoardEvent)
            {
            LOG.debug("InDashDisplay.handleEvent(): " + sensorBoardEvent);
            }
         });
         }

      final LifecycleManager lifecycleManager = new MyLifecycleManager(jFrame, sensorBoardReader);

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

      private MyLifecycleManager(final JFrame jFrame, final SensorBoardReader sensorBoardReader)
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
