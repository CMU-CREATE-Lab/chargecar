package org.chargecar.lcddisplay.commadline;

import edu.cmu.ri.createlab.LCDConstants;
import edu.cmu.ri.createlab.LCD;
import edu.cmu.ri.createlab.LCDProxy;
import edu.cmu.ri.createlab.device.CreateLabDevicePingFailureEventListener;
import edu.cmu.ri.createlab.serial.commandline.SerialDeviceCommandLineApplication;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.SortedMap;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public class CommandLineLCD extends SerialDeviceCommandLineApplication
   {
   private static final Logger LOG = Logger.getLogger(CommandLineLCD.class);
   private static final int THIRTY_SECONDS_IN_MILLIS = 30000;


   public static void main(final String[] args)
      {
      final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

      new CommandLineLCD(in).run();
      }

   private LCD lcd;

   private CommandLineLCD(final BufferedReader in)
      {
      super(in);

      registerActions();

      }

   private final Runnable enumeratePortsAction =
         new Runnable()
         {
         public void run()
            {

            enumeratePorts();
            }
         };

   private final Runnable connectToLCDAction =
         new Runnable()
         {
         public void run()
            {
            if (isConnected())
               {
               println("You are already connected to a CarLCD.");
               }
            else
               {
               final SortedMap<Integer, String> portMap = enumeratePorts();

               if (!portMap.isEmpty())
                  {
                  final Integer index = readInteger("Connect to port number: ");

                  if (index == null)
                     {
                     println("Invalid port");
                     }
                  else
                     {
                     final String serialPortName = portMap.get(index);

                     if (serialPortName != null)
                        {
                        lcd = connectToLCD(serialPortName,
                                new CreateLabDevicePingFailureEventListener() {
                                    public void handlePingFailureEvent() {
                                        println("CarLCD ping failure detected.  You will need to reconnect.");
                                        lcd = null;
                                    }
                                });
                        if (lcd == null)
                           {
                           println("Connection failed!");
                           }
                        else
                           {
                           println("Connection successful!");
                           }
                        }
                     else
                        {
                        println("Invalid port");
                        }
                     }
                  }
               }
            }
         };

   private final Runnable disconnectFromLCDAction =
         new Runnable()
         {
         public void run()
            {
            disconnect();
            }
         };

    private final Runnable getControllerTemperatureInKelvin =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Controller Temperature: " + lcd.getControllerTemperatureInKelvin());
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

    private final Runnable getControllerTemperatureInCelsius =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Controller Temperature: " + lcd.getTemperatureInCelsius(lcd.getControllerTemperatureInKelvin()));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

     private final Runnable getControllerTemperatureInFahrenheit =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Controller Temperature: " + lcd.getTemperatureInFahrenheit(lcd.getControllerTemperatureInKelvin()));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };



      private final Runnable getMotorTemperatureInKelvin =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Motor Temperature: " + lcd.getMotorTemperatureInKelvin());
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };


      private final Runnable getMotorTemperatureInCelsius =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Motor Temperature in celsius: " + lcd.getTemperatureInCelsius(lcd.getMotorTemperatureInKelvin()));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

      private final Runnable getMotorTemperatureInFahrenheit =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Motor Temperature in celsius: " + lcd.getTemperatureInFahrenheit(lcd.getMotorTemperatureInKelvin()));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

      private final Runnable setAirConditioning =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Air Conditioning " + (lcd.turnOnAirConditioning() ? "was successfully set." : "failed to be set."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

      private final Runnable setPowerSteering =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Power Steering " + (lcd.turnOnPowerSteering() ? "was successfully set." : "failed to be set."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

       private final Runnable setCabinHeat =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Cabin Heat " + (lcd.turnOnCabinHeat() ? "was successfully set." : "failed to be set."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

       private final Runnable setDisplayBackLight =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Display Back Light " + (lcd.turnOnDisplayBackLight() ? "was successfully set." : "failed to be set."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

       private final Runnable setBrakeLight =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Brake Light " + (lcd.turnOnBrakeLight() ? "was successfully set." : "failed to be set."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

       private final Runnable setBatteryCooling =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Battery Cooling " + (lcd.turnOnBatteryCooling() ? "was successfully set." : "failed to be set."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

        private final Runnable setBatteryHeating =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Battery Heating " + (lcd.turnOnBatteryHeating() ? "was successfully set." : "failed to be set."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

       private final Runnable setAccessoryOneLED =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Accessory 1 LED " + (lcd.turnOnAccessoryOneLED() ? "was successfully set." : "failed to be set."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

       private final Runnable setAccessoryTwoLED =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Accessory 2 LED " + (lcd.turnOnAccessoryTwoLED() ? "was successfully set." : "failed to be set."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

        private final Runnable setAccessoryThreeLED =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Accessory 3 LED " + (lcd.turnOnAccessoryThreeLED() ? "was successfully set." : "failed to be set."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };
       private final Runnable clearAirConditioning =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Air Conditioning " + (lcd.turnOffAirConditioning() ? "was successfully cleared." : "failed to be cleared."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

       private final Runnable clearPowerSteering =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Power Steering " + (lcd.turnOffPowerSteering() ? "was successfully cleared." : "failed to be cleared."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

       private final Runnable clearCabinHeat =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Cabin Heat " + (lcd.turnOffCabinHeat() ? "was successfully cleared." : "failed to be cleared."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

        private final Runnable clearDisplayBackLight =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Display Back Light " + (lcd.turnOffDisplayBackLight() ? "was successfully cleared." : "failed to be cleared."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

        private final Runnable clearBrakeLight =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Brake Light " + (lcd.turnOffBrakeLight() ? "was successfully cleared." : "failed to be cleared."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

        private final Runnable clearBatteryCooling =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Battery Cooling " + (lcd.turnOffBatteryCooling() ? "was successfully cleared." : "failed to be cleared."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

       private final Runnable clearBatteryHeating =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Battery Heating " + (lcd.turnOffBatteryHeating() ? "was successfully cleared." : "failed to be cleared."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

       private final Runnable clearAccessoryOneLED =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Accessory 1 LED " + (lcd.turnOffAccessoryOneLED() ? "was successfully cleared." : "failed to be cleared."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

       private final Runnable clearAccessoryTwoLED =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Accessory 2 LED " + (lcd.turnOffAccessoryTwoLED() ? "was successfully cleared." : "failed to be cleared."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

        private final Runnable clearAccessoryThreeLED =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Accessory 3 LED " + (lcd.turnOffAccessoryThreeLED() ? "was successfully cleared." : "failed to be cleared."));
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

        private final Runnable getRPM =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Motor RPMMenuItemAction: " + lcd.getRPM());
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

       private final Runnable getMotorControllerErrorCodes =
        new Runnable()
        {
        public void run()
            {
            if (isConnected())
                {
                println("Motor Controller Error Codes: " + lcd.getMotorControllerErrorCodes());
                }
            else
                {
                println("You must be connected to the CarLCD first.");
                }
            }
        };

   private final Runnable quitAction =
         new Runnable()
         {
         public void run()
            {
            disconnect();
            println("Bye!");
            }
         };



   public void setText(final String text)
      {
      if (text != null && text.length() > 0)
         {
             //wrap text
             for (int charIndex = 0, row = 0; charIndex < Math.min(text.length(), LCDConstants.NUM_ROWS*LCDConstants.NUM_COLS); charIndex = charIndex + LCDConstants.NUM_COLS, row++)
             {
                 setLine(row,text.substring(charIndex));
             }
         }
      }

       public void setLine(final int lineNumber, final String text)
           {
               if (isValidRow(lineNumber))
                   {
                       if (text != null && text.length() > 0)
                           {
                               lcd.setText(lineNumber, 0, text.substring(0,Math.min(text.length(), LCDConstants.NUM_COLS)));
                           }
                   }
           }

          public void setCharacter(final int row, final int col, final char character)
              {
                  if (isValidRow(row) && isValidColumn(col))
                  {
                       lcd.setText(row, col, String.valueOf(character));
                  }
              }

           public void clear()
               {
                   for (int row = 0; row < LCDConstants.NUM_ROWS; row++)
                   {
                        clearLine(row);
                   }
               }

           public void clearLine(final int lineNumber)
               {
                   if (isValidRow(lineNumber))
                       {
                           setLine(lineNumber,LCDConstants.BLANK_LINE);
                       }
               }

           private boolean isValidColumn(final int col)
               {
               return (col >= 0 && col < LCDConstants.NUM_COLS);
               }

           private boolean isValidRow(final int row)
               {
               return (row >= 0 && row < LCDConstants.NUM_ROWS);
               }

    private final Runnable displayHelloWorld =
         new Runnable()
         {
         public void run()
            {
            String text = "HHHHHA";
            setLine(2,text);
            }
         };

    private final Runnable clearHelloWorld =
         new Runnable()
         {
         public void run()
            {
            clearLine(2);
            }
         };

    private final Runnable clearDisplay =
         new Runnable()
         {
         public void run()
            {
            clear();
            }
         };

   private void registerActions()
      {
      registerAction("?", enumeratePortsAction);
      registerAction("c", connectToLCDAction);
      registerAction("d", disconnectFromLCDAction);

      registerAction("dhw", displayHelloWorld);
      registerAction("chw", clearHelloWorld);
      registerAction("cd", clearDisplay);

      registerAction("ctk", getControllerTemperatureInKelvin);
      registerAction("ctc", getControllerTemperatureInCelsius);
      registerAction("ctf", getControllerTemperatureInFahrenheit);
      registerAction("mtk", getMotorTemperatureInKelvin);
      registerAction("mtc", getMotorTemperatureInCelsius);
      registerAction("mtf", getMotorTemperatureInFahrenheit);

      registerAction("sac", setAirConditioning);
      registerAction("sps", setPowerSteering);
      registerAction("sch", setCabinHeat);
      registerAction("sdl", setDisplayBackLight);
      registerAction("sbl", setBrakeLight);
      registerAction("sbc", setBatteryCooling);
      registerAction("sbh", setBatteryHeating);
      registerAction("sa1", setAccessoryOneLED);
      registerAction("sa2", setAccessoryTwoLED);
      registerAction("sa3", setAccessoryThreeLED);

      registerAction("cac", clearAirConditioning);
      registerAction("cps", clearPowerSteering);
      registerAction("cch", clearCabinHeat);
      registerAction("cdl", clearDisplayBackLight);
      registerAction("cbl", clearBrakeLight);
      registerAction("cbc", clearBatteryCooling);
      registerAction("cbh", clearBatteryHeating);
      registerAction("ca1", clearAccessoryOneLED);
      registerAction("ca2", clearAccessoryTwoLED);
      registerAction("ca3", clearAccessoryThreeLED);

      registerAction("s", getRPM);

      registerAction("e", getMotorControllerErrorCodes);

      //registerAction("r", resetDisplay);

      registerAction(QUIT_COMMAND, quitAction);
      }

   protected final void menu()
      {
      println("COMMANDS -----------------------------------");
      println("");
      println("?           List all available serial ports");
      println("");
      println("c           Connect to the CarLCD");
      println("d           Disconnect from the CarLCD");
      println("");
      println("dhw         Display Hello World at the top left.");
      println("chw         Clear text at the top left.");
      println("cd          Clear all text on the display.");
      println("");
      println("ctk         Get the controller temperature in kelvin");
      println("ctc         Get the controller temperature in celsius");
      println("ctf         Get the controller temperature in fahrenheit");
      println("mtk         Get the motor temperature in kelvin");
      println("mtc         Get the motor temperature in celsius");
      println("mtf         Get the motor temperature in fahrenheit");
      println("s           Get RPMMenuItemAction");
      println("e           Get Motor Controller Error Codes");
      println("");
      println("cr          Check if car is running");
      println("cc          Check if car is charging");
      println("");
      println("sac         Turn on air conditioning");
      println("cac         Turn off air conditioning");
      println("sps         Turn on power steering");
      println("cps         Turn off power steering");
      println("sch         Turn on cabin heat");
      println("cch         Turn off cabin heat");
      println("sdl         Turn on display back light");
      println("cdl         Turn off display back light");
      println("sbl         Turn on brake light");
      println("cbl         Turn off brake light");
      println("sbc         Turn on battery cooling");
      println("cbc         Turn off battery cooling");
      println("sbh         Turn on batter heating");
      println("cbh         Turn off battery heating");
      println("sa1         Turn on accessory 1 LED");
      println("ca1         Turn off accessory 1 LED");
      println("sa2         Turn on accessory 2 LED");
      println("ca2         Turn off accessory 2 LED");
      println("sa3         Turn on accessory 3 LED");
      println("ca3         Turn off accessory 3 LED");
      println("");
      println("q           Quit");
      println("");
      println("--------------------------------------------");
      }

   protected final void poll(final Runnable strategy)
      {
      final long startTime = System.currentTimeMillis();
      while (isConnected() && System.currentTimeMillis() - startTime < THIRTY_SECONDS_IN_MILLIS)
         {
         strategy.run();
         try
            {
            Thread.sleep(30);
            }
         catch (InterruptedException e)
            {
            LOG.error("InterruptedException while sleeping", e);
            }
         }
      }



   protected final LCDProxy connectToLCD(final String serialPortName, final CreateLabDevicePingFailureEventListener pingFailureEventListener)
      {
      final LCDProxy lcdProxy = LCDProxy.create(serialPortName);

      if (lcdProxy != null)
         {
         lcdProxy.addCreateLabDevicePingFailureEventListener(pingFailureEventListener);
         }

      return lcdProxy;
      }

   protected final boolean isConnected()
      {
      return lcd != null;
      }

   protected final void disconnect()
      {
      if (isConnected())
         {
         lcd.disconnect();
         lcd = null;
         }
      }
   }
