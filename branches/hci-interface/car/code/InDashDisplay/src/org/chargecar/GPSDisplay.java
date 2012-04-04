package org.chargecar;

import java.awt.Color;
import java.awt.Component;
import java.util.PropertyResourceBundle;
import java.util.concurrent.TimeUnit;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.component.DatasetPlotter;
import edu.cmu.ri.createlab.userinterface.util.SpringLayoutUtilities;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.apache.log4j.Logger;
import org.chargecar.gps.GPSEventListener;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class GPSDisplay implements GPSEventListener
   {
   private static final Logger LOG = Logger.getLogger(GPSDisplay.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(GPSDisplay.class.getName());

   private final JPanel panel = new JPanel(new SpringLayout());
   private final JLabel latitudeLabel = SwingUtils.createLabel("", GUIConstants.MONOSPACED_FONT_LARGE);
   private final JLabel longitudeLabel = SwingUtils.createLabel("", GUIConstants.MONOSPACED_FONT_LARGE);
   private final JLabel satellitesLabel = SwingUtils.createLabel("", GUIConstants.MONOSPACED_FONT_LARGE);
   private final JLabel elevationLabel = SwingUtils.createLabel("", GUIConstants.MONOSPACED_FONT_LARGE);
   private final DatasetPlotter<Integer> elevationPlot = new DatasetPlotter<Integer>(900, 1300, 400, 400, 1, TimeUnit.SECONDS);

   public GPSDisplay()
      {
      elevationPlot.addDataset(Color.RED);

      final JPanel dataPanel = new JPanel(new SpringLayout());
      dataPanel.add(SwingUtils.createLabel(RESOURCES.getString("label.latitude"), GUIConstants.FONT_LARGE));
      dataPanel.add(latitudeLabel);
      dataPanel.add(SwingUtils.createLabel(RESOURCES.getString("label.longitude"), GUIConstants.FONT_LARGE));
      dataPanel.add(longitudeLabel);
      dataPanel.add(SwingUtils.createLabel(RESOURCES.getString("label.satellites"), GUIConstants.FONT_LARGE));
      dataPanel.add(satellitesLabel);
      dataPanel.add(SwingUtils.createLabel(RESOURCES.getString("label.elevation"), GUIConstants.FONT_LARGE));
      dataPanel.add(elevationLabel);
      SpringLayoutUtilities.makeCompactGrid(dataPanel,
                                            4, 2, // rows, cols
                                            0, 0, // initX, initY
                                            5, 5);// xPad, yPad

      panel.add(dataPanel);
      panel.add(elevationPlot.getComponent());
      SpringLayoutUtilities.makeCompactGrid(panel,
                                            2, 1, // rows, cols
                                            0, 0, // initX, initY
                                            5, 5);// xPad, yPad
      }

   public Component getComponent()
      {
      return panel;
      }

   public void handleLocationEvent(final String latitude, final String longitude, final int numSatellitesBeingTracked)
      {
      LOG.debug("GPSDisplay.handleLocationEvent(" + latitude + "," + longitude + "," + numSatellitesBeingTracked + ")");
      SwingUtils.runInGUIThread(
            new Runnable()
            {
            public void run()
               {
               latitudeLabel.setText(latitude);
               longitudeLabel.setText(longitude);
               satellitesLabel.setText(String.valueOf(numSatellitesBeingTracked));
               }
            });
      }

   public void handleElevationEvent(final int elevationInFeet)
      {
      LOG.debug("GPSDisplay.handleElevationEvent(" + elevationInFeet + ")");
      SwingUtils.runInGUIThread(
            new Runnable()
            {
            public void run()
               {
               elevationLabel.setText(String.valueOf(elevationInFeet));
               elevationPlot.setCurrentValues(elevationInFeet);
               }
            });
      }
   }
