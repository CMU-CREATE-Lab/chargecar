package org.chargecar;

import java.awt.Color;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PropertyResourceBundle;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import edu.cmu.ri.createlab.userinterface.AbstractTimeConsumingAction;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.dialog.AbstractAlert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.userinterface.filechooser.FileChooser;
import org.chargecar.userinterface.filechooser.FileChooserEventListener;
import org.chargecar.userinterface.filechooser.StandardFileChooser;
import org.jdesktop.layout.GroupLayout;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
@SuppressWarnings({"CloneableClassWithoutClone"})
final class MotionXGPSRawFileConverterGUI extends JPanel
   {
   private static final Log LOG = LogFactory.getLog(MotionXGPSRawFileConverterGUI.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(MotionXGPSRawFileConverterGUI.class.getName());
   private static final String APPLICATION_NAME = RESOURCES.getString("application.name");
   private static final String LINE_SEPARATOR = System.getProperty("line.separator");

   private final FileChooser sourceFileChooser = new StandardFileChooser(40,
                                                                         RESOURCES.getString("button.choose"),
                                                                         RESOURCES.getString("filechooser.filter.accepted-file-type.extension"),
                                                                         RESOURCES.getString("filechooser.filter.accepted-file-type.name"));

   private final JButton convertButton = GUIConstants.createButton(RESOURCES.getString("button.convert"), false);
   private final Alert alert = new Alert(this);

   MotionXGPSRawFileConverterGUI(final MotionXGPSRawFileConverter converter)
      {
      sourceFileChooser.addFileChooserEventListener(
            new FileChooserEventListener()
            {
            public void handleSelectedFileChange()
               {
               convertButton.setEnabled(sourceFileChooser.getSelectedFile() != null);
               }
            });

      convertButton.addActionListener(
            new AbstractTimeConsumingAction(MotionXGPSRawFileConverterGUI.this)
            {
            protected void executeGUIActionBefore()
               {
               sourceFileChooser.setEnabled(false);
               convertButton.setEnabled(false);
               }

            protected Object executeTimeConsumingAction()
               {
               try
                  {
                  final String output = converter.convert(sourceFileChooser.getSelectedFile());

                  if (output == null)
                     {
                     LOG.error("Conversion to GPX failed");
                     alert.showConversionFailureAlert("Generated output was null");
                     }
                  else
                     {
                     // save file, notify user
                     final File sourceFile = sourceFileChooser.getSelectedFile();
                     final File outputFile = new File(sourceFile.getParent(), sourceFile.getName() + ".gpx");
                     if (outputFile.exists())
                        {
                        // notify user file already exists
                        LOG.error("File [" + outputFile.getAbsolutePath() + "] not written because another file of the same name already exists in that location");
                        alert.showFileExistsAlert();
                        }
                     else
                        {
                        // save the file
                        BufferedWriter writer = null;
                        try
                           {
                           writer = new BufferedWriter(new FileWriter(outputFile));
                           writer.write(output);
                           writer.close();
                           alert.showConversionSuccessAlert(outputFile);
                           }
                        catch (IOException e)
                           {
                           // display error message to the user
                           LOG.error("IOException while trying to write the output file", e);
                           alert.showFileCouldNotBeWrittenAlert();
                           }
                        finally
                           {
                           if (writer != null)
                              {
                              try
                                 {
                                 writer.close();
                                 }
                              catch (IOException e)
                                 {
                                 LOG.error("IOException while trying to close the writer", e);
                                 }
                              }
                           }
                        }
                     }
                  }
               catch (Exception e)
                  {
                  LOG.error("Exception while converting the file", e);
                  alert.showConversionFailureAlert(e.getMessage());
                  }

               return null;
               }

            protected void executeGUIActionAfter(final Object resultOfTimeConsumingAction)
               {
               sourceFileChooser.setEnabled(true);
               convertButton.setEnabled(true);
               }
            });

      final Component titlePaddingLeft = Box.createGlue();
      final Component titlePaddingRight = Box.createGlue();
      final Component convertButtonPaddingLeft = Box.createGlue();
      final JLabel titleLabel = GUIConstants.createLabel(APPLICATION_NAME, GUIConstants.FONT_MEDIUM);
      final Component titlePadding = GUIConstants.createRigidSpacer(20);
      final JLabel instructionsLabel = GUIConstants.createLabel(RESOURCES.getString("label.instructions"));
      final Component instructionsPadding = GUIConstants.createRigidSpacer(20);
      final Component convertButtonPadding = GUIConstants.createRigidSpacer(20);
      final JLabel sourceLabel = GUIConstants.createLabel(RESOURCES.getString("label.source-kmz") + ":");

      final JPanel panel = new JPanel();
      final GroupLayout layout = new GroupLayout(panel);
      panel.setLayout(layout);
      panel.setBackground(Color.WHITE);

      layout.setHorizontalGroup(
            layout.createParallelGroup()
                  .add(layout.createParallelGroup(GroupLayout.CENTER)
                        .add(titlePaddingLeft)
                        .add(titleLabel)
                        .add(titlePaddingRight)
                  )
                  .add(layout.createParallelGroup(GroupLayout.LEADING)
                        .add(titlePadding)
                        .add(instructionsLabel)
                        .add(instructionsPadding)
                        .add(sourceLabel)
                        .add(sourceFileChooser.getComponent())
                        .add(convertButtonPadding)
                  )
                  .add(layout.createParallelGroup(GroupLayout.TRAILING)
                  .add(convertButtonPaddingLeft)
                  .add(convertButton)
            )
      );
      layout.setVerticalGroup(
            layout.createSequentialGroup()
                  .add(layout.createParallelGroup()
                        .add(titlePaddingLeft)
                        .add(titleLabel)
                        .add(titlePaddingRight)
                  )
                  .add(titlePadding)
                  .add(instructionsLabel)
                  .add(instructionsPadding)
                  .add(sourceLabel)
                  .add(sourceFileChooser.getComponent()
                  )
                  .add(convertButtonPadding)
                  .add(layout.createParallelGroup()
                  .add(convertButtonPaddingLeft)
                  .add(convertButton)
            )
      );

      this.add(panel);
      }

   private static final class Alert extends AbstractAlert
      {
      private Alert(final Component parentComponent)
         {
         super(parentComponent);
         }

      public void showConversionFailureAlert(final String message)
         {
         showErrorMessage(RESOURCES.getString("dialog.title.conversion-failed"),
                          RESOURCES.getString("dialog.message.conversion-failed") + LINE_SEPARATOR + LINE_SEPARATOR + message);
         }

      public void showFileExistsAlert()
         {
         showErrorMessage(RESOURCES.getString("dialog.title.conversion-failed-file-exists"),
                          RESOURCES.getString("dialog.message.conversion-failed-file-exists"));
         }

      public void showFileCouldNotBeWrittenAlert()
         {
         showErrorMessage(RESOURCES.getString("dialog.title.conversion-failed-file-could-not-be-written"),
                          RESOURCES.getString("dialog.message.conversion-failed-file-could-not-be-written"));
         }

      public void showConversionSuccessAlert(final File file)
         {
         showInfoMessage(RESOURCES.getString("dialog.title.conversion-success"),
                         RESOURCES.getString("dialog.message.conversion-success") + " " + file.getAbsolutePath());
         }
      }

   public static void main(final String[] args)
      {
      SwingUtilities.invokeLater(
            new Runnable()
            {
            public void run()
               {
               final JFrame jFrame = new JFrame(APPLICATION_NAME);

               // add the root panel to the JFrame
               jFrame.add(new MotionXGPSRawFileConverterGUI(new MotionXGPSRawFileConverter()));

               // set various properties for the JFrame
               jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
               jFrame.setBackground(Color.WHITE);
               jFrame.setResizable(true);
               jFrame.pack();
               jFrame.setLocationRelativeTo(null);// center the window on the screen
               jFrame.setVisible(true);
               }
            });
      }
   }
