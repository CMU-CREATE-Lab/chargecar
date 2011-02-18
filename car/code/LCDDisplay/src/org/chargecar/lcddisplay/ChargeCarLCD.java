package org.chargecar.lcddisplay;

import edu.cmu.ri.createlab.display.character.menu.CharacterDisplayMenu;
import edu.cmu.ri.createlab.menu.DefaultMenuStatusManager;
import edu.cmu.ri.createlab.menu.Menu;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class ChargeCarLCD
   {
   private static final Logger LOG = Logger.getLogger(ChargeCarLCD.class);

   public static void main(final String[] args)
      {
      for (final String arg : args)
         {
         final String[] keyValue = arg.split("=");
         if (keyValue.length == 2)
            {
            LOG.debug("Associating [" + keyValue[0] + "] with serial port [" + keyValue[1] + "]");
            System.setProperty(LCDConstants.SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX + keyValue[0].toLowerCase(), keyValue[1]);
            }
         else
            {
            LOG.info("Ignoring unexpected switch [" + arg + "]");
            }
         }

      new ChargeCarLCD();
      }

   private ChargeCarLCD()
      {
      // create the LCD panel and button panel
      final LCDPanel lcdPanel = new LCDPanel(4, 20);

      // create the menu status manager
      final MenuStatusManager menuStatusManager = new DefaultMenuStatusManager();

      LOG.debug("ChargeCarLCD(): about to connect to the BMS, GPS, MotorController, and SensorBoard...");
      // call .getInstance() on the various managers to kick off connection establishment to them
      BMSManager.getInstance();
      GPSManager.getInstance();
      MotorControllerManager.getInstance();
      SensorBoardManager.getInstance();

      LOG.debug("ChargeCarLCD(): about to call LCDProxy.getInstance()...");
      final LCD lcd = LCDProxy.getInstance();

      //always turn on the display back light
      lcd.turnOnDisplayBackLight();

      lcd.addButtonPanelEventListener(new MyButtonPanelEventListener(menuStatusManager));

      // build the menu
      LOG.debug("ChargeCarLCD(): about to build the menu...");
      final Menu menu = CharacterDisplayMenu.create("/org/chargecar/lcddisplay/menu/menu.xml", menuStatusManager, lcdPanel);

      // set the default text on the LCD
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

      @Override
      public void handleAccessoryOneEvent()
         {
         menuStatusManager.handleAccessoryOneEvent();
         }

      @Override
      public void handleAccessoryTwoEvent()
         {
         menuStatusManager.handleAccessoryTwoEvent();
         }

      @Override
      public void handleAccessoryThreeEvent()
         {
         menuStatusManager.handleAccessoryThreeEvent();
         }
      }
   }
