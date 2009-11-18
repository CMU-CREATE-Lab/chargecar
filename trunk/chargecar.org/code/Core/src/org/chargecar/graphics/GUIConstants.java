package org.chargecar.graphics;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;

/**
 * <p>
 * <code>GUIConstants</code> defines constants common to GUI applications
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GUIConstants
   {
   public static final String FONT_NAME = "Verdana";
   public static final String MONOSPACED_FONT_NAME = "Courier";
   public static final Font FONT_LARGE = new Font(FONT_NAME, 0, 20);
   public static final Font MONOSPACED_FONT_SMALL = new Font(MONOSPACED_FONT_NAME, 0, 11);
   public static final Font MONOSPACED_FONT_LARGE = new Font(MONOSPACED_FONT_NAME, 0, 20);
   public static final Font FONT_MEDIUM = new Font(FONT_NAME, 0, 14);
   public static final Font FONT_MEDIUM_BOLD = new Font(FONT_NAME, Font.BOLD, 14);
   public static final Font FONT_NORMAL = new Font(FONT_NAME, 0, 11);
   public static final Font FONT_SMALL = new Font(FONT_NAME, 0, 11);
   public static final Font FONT_TINY = new Font(FONT_NAME, 0, 9);
   public static final Font BUTTON_FONT = FONT_SMALL;

   /** Creates a 5 x 5 rigid spacer. */
   public static Component createRigidSpacer()
      {
      return createRigidSpacer(5);
      }

   /** Creates a <code>size</code> x <code>size</code> rigid spacer. */
   public static Component createRigidSpacer(final int size)
      {
      return createRigidSpacer(size, size);
      }

   /** Creates a <code>width</code> x <code>height</code> rigid spacer. */
   public static Component createRigidSpacer(final int width, final int height)
      {
      return Box.createRigidArea(new Dimension(width, height));
      }

   public static JLabel createLabel(final String labelText)
      {
      return createLabel(labelText, FONT_SMALL);
      }

   public static JLabel createTinyFontLabel(final String labelText)
      {
      return createLabel(labelText, FONT_TINY);
      }

   public static JLabel createLabel(final String labelText, final Font font)
      {
      final JLabel label = new JLabel(labelText);
      label.setFont(font);
      return label;
      }

   /** Creates a (disabled) button with the given <code>label</code>. */
   public static JButton createButton(final String label)
      {
      return createButton(label, false);
      }

   /** Creates a button with the given <code>label</code> and with an enabled state specified by <code>isEnabled</code>. */
   public static JButton createButton(final String label, final boolean isEnabled)
      {
      final JButton button = new JButton(label);
      button.setFont(BUTTON_FONT);
      button.setEnabled(isEnabled);
      button.setOpaque(false);// required for Macintosh
      return button;
      }

   private GUIConstants()
      {
      // private to prevent instantiation
      }
   }