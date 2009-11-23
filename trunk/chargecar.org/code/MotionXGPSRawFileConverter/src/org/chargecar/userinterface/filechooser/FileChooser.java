package org.chargecar.userinterface.filechooser;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface FileChooser
   {
   Component getComponent();

   void setEnabled(final boolean isEnabled);

   /**
    * Returns <code>true</code> if a (valid) file is selected, <code>false</code> otherwise.  This method assumes it's
    * being called from within the Swing thread.
    */
   boolean isFileSelected();

   File getSelectedFile();

   String getSelectedFilePath();

   void setSelectedFilePath(final String path);

   void addFilePathFieldActionListener(final ActionListener listener);

   void addFileChooserEventListener(final FileChooserEventListener listener);
   }
