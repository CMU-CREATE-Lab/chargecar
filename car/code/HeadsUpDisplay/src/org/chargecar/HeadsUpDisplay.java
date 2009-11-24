package org.chargecar;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import edu.cmu.ri.createlab.serial.SerialPortException;
import edu.cmu.ri.createlab.userinterface.SwingWorker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.gps.nmea.NMEAReader;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class HeadsUpDisplay
   {
   private static final Log LOG = LogFactory.getLog(HeadsUpDisplay.class);
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(HeadsUpDisplay.class.getName());
   private static final String APPLICATION_NAME = RESOURCES.getString("application.name");

   public static void main(final String[] args) throws SerialPortException, IOException
      {
      if (args.length < 1)
         {
         LOG.error("Serial port not specified, aborting.");
         System.exit(1);
         }

      final String serialPortName = args[0];
      final NMEAReader gpsReader = new NMEAReader(APPLICATION_NAME);
      gpsReader.connect(serialPortName);

      //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
      SwingUtilities.invokeLater(
            new Runnable()
            {
            public void run()
               {
               new HeadsUpDisplay(gpsReader);
               }
            });
      }

   private HeadsUpDisplay(final NMEAReader gpsReader)
      {
      LOG.debug("Hello World!");

      final GPSDisplay gpsDisplay = new GPSDisplay();
      gpsReader.addEventListener(gpsDisplay);

      final JFrame jFrame = new JFrame(APPLICATION_NAME);

      // create the main panel for the JFrame
      final JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      // add the views to the main panel
      panel.add(Box.createGlue());
      panel.add(gpsDisplay.getComponent());
      panel.add(Box.createGlue());

      // add the panel to the JFrame
      jFrame.add(panel);

      // set various properties for the JFrame
      jFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      jFrame.setBackground(Color.WHITE);
      jFrame.setResizable(true);
      jFrame.addWindowListener(
            new WindowAdapter()
            {
            @Override
            public void windowOpened(final WindowEvent e)
               {
               LOG.debug("HeadsUpDisplay.windowOpened()");
               gpsReader.startReading();
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
                           gpsReader.stopReading();
                           gpsReader.disconnect();
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

      LOG.debug("Goodbye World!");
      }
   }
