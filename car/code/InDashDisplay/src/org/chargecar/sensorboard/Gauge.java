package org.chargecar.sensorboard;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.jdesktop.layout.GroupLayout;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class Gauge<T> extends JPanel
   {
   private static final String UNKNOWN_VALUE = "?";

   private final JLabel value;
   private final String stringFormat;

   Gauge(final String labelText, final String stringFormat)
      {
      this.stringFormat = stringFormat;
      final JLabel label = SwingUtils.createLabel(labelText, GUIConstants.FONT_NORMAL);
      this.value = SwingUtils.createLabel("", GUIConstants.FONT_MEDIUM_LARGE);
      final GroupLayout layout = new GroupLayout(this);
      this.setLayout(layout);
      this.setBackground(Color.WHITE);

      layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.CENTER)
                  .add(value)
                  .add(label));
      layout.setVerticalGroup(
            layout.createSequentialGroup()
                  .add(value)
                  .add(label));
      }

   /**
    * Sets the gauge's value using the string format given to the constructor.  Assumes it is being called in the GUI
    * thread.
    */
   void setValue(final T s)
   {
   if (s != null)
      {
      value.setForeground(Color.BLACK);
      value.setText(String.format(stringFormat, s));
      }
   else
      {
      value.setForeground(Color.RED);
      value.setText(UNKNOWN_VALUE);
      }
   }
   }
