package org.chargecar.userinterface.filechooser;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.layout.GroupLayout;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class StandardFileChooser implements FileChooser
   {
   private static final Log LOG = LogFactory.getLog(StandardFileChooser.class);

   public static void main(final String[] args)
      {
      //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
      SwingUtilities.invokeLater(
            new Runnable()
            {
            public void run()
               {
               final JFrame jFrame = new JFrame("Standard File Chooser");

               // add the root panel to the JFrame
               jFrame.add(new StandardFileChooser(10, "Choose...", ".kmz", "KMZ Files").getComponent());

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

   private final JPanel panel = new JPanel();
   private final JFileChooser fileChooser = new JFileChooser();
   private final JTextField filePathTextField;
   private final JButton findClipButton;
   private final Set<FileChooserEventListener> audioClipChooserEventListeners = new HashSet<FileChooserEventListener>();

   public StandardFileChooser(final int numTextFieldColumns, final String buttonLabel, final String targetFileExtension, final String targetFileDescription)
      {
      filePathTextField = createTextField(numTextFieldColumns);
      findClipButton = GUIConstants.createButton(buttonLabel, true);

      filePathTextField.addKeyListener(
            new KeyAdapter()
            {
            public void keyReleased(final KeyEvent e)
               {
               for (final FileChooserEventListener listener : audioClipChooserEventListeners)
                  {
                  listener.handleSelectedFileChange();
                  }
               }
            }
      );
      findClipButton.addActionListener(
            new ActionListener()
            {
            public void actionPerformed(final ActionEvent e)
               {
               final int returnValue = fileChooser.showOpenDialog(panel);
               if (returnValue == JFileChooser.APPROVE_OPTION)
                  {
                  final File file = fileChooser.getSelectedFile();
                  filePathTextField.setText(file.getAbsolutePath());
                  for (final FileChooserEventListener listener : audioClipChooserEventListeners)
                     {
                     listener.handleSelectedFileChange();
                     }

                  LOG.info("File chosen: " + file.getName());
                  }
               else
                  {
                  LOG.debug("Find file dialog cancelled by user.");
                  }
               }
            });

      fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
      fileChooser.setAcceptAllFileFilterUsed(false);
      fileChooser.setFileFilter(
            new FileFilter()
            {
            public boolean accept(final File file)
               {
               if (file != null && file.exists())
                  {
                  if (file.isDirectory())
                     {
                     return true;
                     }
                  return file.getName().toLowerCase().endsWith(targetFileExtension);
                  }
               return false;
               }

            public String getDescription()
               {
               return targetFileDescription;
               }
            });

      final GroupLayout layout = new GroupLayout(panel);
      panel.setLayout(layout);
      panel.setBackground(Color.WHITE);

      layout.setHorizontalGroup(
            layout.createSequentialGroup()
                  .add(filePathTextField)
                  .add(findClipButton)
      );
      layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.CENTER)
                  .add(filePathTextField)
                  .add(findClipButton)
      );
      }

   public Component getComponent()
      {
      return panel;
      }

   public void setEnabled(final boolean isEnabled)
      {
      filePathTextField.setEnabled(isEnabled);
      findClipButton.setEnabled(isEnabled);
      }

   public boolean isFileSelected()
      {
      if (isTextFieldNonEmpty(filePathTextField))
         {
         final File file = getTextFieldValueAsFile(filePathTextField);
         return file != null && file.exists() && file.isFile();
         }
      return false;
      }

   public File getSelectedFile()
      {
      return getTextFieldValueAsFile(filePathTextField);
      }

   public String getSelectedFilePath()
      {
      final File file = getSelectedFile();

      if (file != null)
         {
         return file.getAbsolutePath();
         }

      return null;
      }

   public void setSelectedFilePath(final String path)
      {
      if (SwingUtilities.isEventDispatchThread())
         {
         filePathTextField.setText(path);
         }
      else
         {
         SwingUtilities.invokeLater(
               new Runnable()
               {
               public void run()
                  {
                  filePathTextField.setText(path);
                  }
               });
         }
      }

   public void addFilePathFieldActionListener(final ActionListener listener)
      {
      if (listener != null)
         {
         filePathTextField.addActionListener(listener);
         }
      }

   public void addFileChooserEventListener(final FileChooserEventListener listener)
      {
      if (listener != null)
         {
         audioClipChooserEventListeners.add(listener);
         }
      }

   private JTextField createTextField(final int numColumns)
      {
      final JTextField textField = new JTextField(numColumns);
      textField.setFont(GUIConstants.FONT_NORMAL);
      textField.setMinimumSize(textField.getPreferredSize());
      textField.setMaximumSize(textField.getPreferredSize());
      return textField;
      }

   private boolean isTextFieldNonEmpty(final JTextField textField)
      {
      final String text1 = textField.getText();
      final String trimmedText1 = (text1 != null) ? text1.trim() : null;
      return (trimmedText1 != null) && (trimmedText1.length() > 0);
      }

   /** Retrieves the value from the specified text field as a {@link File}; returns <code>null</code> if the file does not exist. */
   private File getTextFieldValueAsFile(final JTextField textField)
      {
      final String filePath = getTextFieldValueAsString(textField);
      if (filePath != null)
         {
         final File file = new File(filePath);
         if (file.exists())
            {
            return file;
            }
         }
      return null;
      }

   /** Retrieves the value from the specified text field as a {@link String}. */
   @SuppressWarnings({"UnusedCatchParameter"})
   private String getTextFieldValueAsString(final JTextField textField)
      {
      final String text;
      if (SwingUtilities.isEventDispatchThread())
         {
         text = textField.getText();
         }
      else
         {
         final String[] textFieldValue = new String[1];
         try
            {
            SwingUtilities.invokeAndWait(
                  new Runnable()
                  {
                  public void run()
                     {
                     textFieldValue[0] = textField.getText();
                     }
                  });
            }
         catch (Exception e)
            {
            LOG.error("Exception while getting the value from text field.  Returning null instead.");
            textFieldValue[0] = null;
            }

         text = textFieldValue[0];
         }

      return (text != null) ? text.trim() : null;
      }
   }
