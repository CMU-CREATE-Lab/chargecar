package org.chargecar.sensorboard;

import java.awt.Color;
import java.util.PropertyResourceBundle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import org.chargecar.HeadsUpDisplay;
import org.jdesktop.layout.GroupLayout;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class Gauge<T> extends JPanel
   {
   private static final PropertyResourceBundle COMMON_RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(HeadsUpDisplay.class.getName());
   private static final String UNKNOWN_VALUE = COMMON_RESOURCES.getString("unknown-value");

   private final JLabel value;
   private final String stringFormat;

   Gauge(final String labelText, final String stringFormat)
      {
      this.stringFormat = stringFormat;
      final JLabel label = GUIConstants.createLabel(labelText, GUIConstants.FONT_SMALL);
      this.value = GUIConstants.createLabel("", GUIConstants.FONT_LARGE);
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

   void setValue(final String s)
      {
      value.setText(s);
      }

   void setValue(final T s)
      {
      if (s != null)
         {
         value.setText(String.format(stringFormat, s));
         }
      else
         {
         value.setText(UNKNOWN_VALUE);
         }
      }
   }
