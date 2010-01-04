package org.chargecar;

import java.awt.Component;
import javax.swing.JPanel;
import org.chargecar.sensorboard.SpeedAndOdometryView;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class HeadsUpDisplayView
   {
   private final JPanel panel = new JPanel();

   HeadsUpDisplayView(final SpeedAndOdometryView speedAndOdometryView)
      {
      panel.add(speedAndOdometryView.getComponent());
      }

   Component getComponent()
      {
      return panel;
      }
   }
