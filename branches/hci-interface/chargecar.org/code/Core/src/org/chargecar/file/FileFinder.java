package org.chargecar.file;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class FileFinder
   {
   public interface EventHandler
      {
      void handFileFoundEvent(final File file);
      }

   private static final FileFilter DEFAULT_FILENAME_FILTER = new FileFilter()
   {
   public boolean accept(final File pathname)
      {
      return true;
      }
   };

   private final File rootDirectory;
   private final FileFilter fileFilter;
   private final List<EventHandler> eventHandlers = new ArrayList<EventHandler>();

   public FileFinder(final File rootDirectory)
      {
      this(rootDirectory, DEFAULT_FILENAME_FILTER);
      }

   public FileFinder(final File rootDirectory, final FileFilter fileFilter)
      {
      this.fileFilter = fileFilter;
      if (rootDirectory.exists() && rootDirectory.isDirectory())
         {
         this.rootDirectory = rootDirectory;
         }
      else
         {
         throw new IllegalArgumentException("The specified root directory path [" + rootDirectory + "] does not exist or is not a directory.");
         }
      }

   public void addEventHandler(final EventHandler eventHandler)
      {
      if (eventHandler != null)
         {
         eventHandlers.add(eventHandler);
         }
      }

   public void find()
      {
      find(rootDirectory);
      }

   private void find(final File directory)
      {
      final File[] files = directory.listFiles();
      for (final File file : files)
         {
         if (file.isDirectory())
            {
            find(file);
            }
         else if (fileFilter.accept(file))
            {
            for (final EventHandler eventHandler : eventHandlers)
               {
               eventHandler.handFileFoundEvent(file);
               }
            }
         }
      }
   }
