package org.chargecar.lcddisplay.demo;

import java.awt.Color;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import edu.cmu.ri.createlab.display.character.lcd.SwingLCDPanel;
import edu.cmu.ri.createlab.display.character.menu.CharacterDisplayMenu;
import edu.cmu.ri.createlab.menu.DefaultMenuStatusManager;
import edu.cmu.ri.createlab.menu.Menu;
import edu.cmu.ri.createlab.menu.MenuStatusManager;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class LCDDemo extends JPanel
   {
   public static void main(final String[] args)
      {
      //Schedule a job for the event-dispatching thread: creating and showing this application's GUI.
      SwingUtilities.invokeLater(
            new Runnable()
            {
            public void run()
               {
               final JFrame jFrame = new JFrame("LCD");

               // add the root panel to the JFrame
               jFrame.add(new LCDDemo());

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

   private LCDDemo()
      {
      // create the LCD panel and button panel
      final SwingLCDPanel lcdPanel = new SwingLCDPanel(2, 16);
      final ButtonPanel buttonPanel = new ButtonPanel();

      // create the menu status manager
      final MenuStatusManager menuStatusManager = new DefaultMenuStatusManager();

      // register the listener for button panel events
      buttonPanel.addButtonPanelEventListener(new MyButtonPanelEventListener(menuStatusManager));

      // build the menu
      final Menu menu = CharacterDisplayMenu.create("/org/chargecar/lcddisplay/demo/menu.xml", menuStatusManager, lcdPanel);

      // layout the GUI components and set the default text on the LCD
      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      this.add(lcdPanel.getComponent());
      this.add(buttonPanel.getComponent());
      lcdPanel.setText(menu.getWelcomeText());

      // If there's welcome text, then delay the start of the menu manager so that the initial welcome screen is visible
      // for a bit
      final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
      executorService.schedule(
            new Runnable()
            {
            public void run()
               {
               menuStatusManager.setActiveMenuItem(menu.getDefaultMenuItem());
               }
            },
            menu.hasWelcomeText() ? 2 : 0,
            TimeUnit.SECONDS);
      }

   private static class MyButtonPanelEventListener implements ButtonPanelEventListener
      {
      private final MenuStatusManager menuStatusManager;

      private MyButtonPanelEventListener(final MenuStatusManager menuStatusManager)
         {
         this.menuStatusManager = menuStatusManager;
         }

      public void handleOKEvent()
         {
         menuStatusManager.handleStartEvent();
         }

      public void handleCancelEvent()
         {
         menuStatusManager.handleStopEvent();
         }

      public void handleUpEvent()
         {
         menuStatusManager.handleUpEvent();
         }

      public void handleRightEvent()
         {
         menuStatusManager.handleRightEvent();
         }

      public void handleDownEvent()
         {
         menuStatusManager.handleDownEvent();
         }

      public void handleLeftEvent()
         {
         menuStatusManager.handleLeftEvent();
         }
      }
   }
