package org.chargecar.sensorboard;

import java.io.BufferedReader;
import java.util.SortedMap;
import edu.cmu.ri.createlab.serial.commandline.SerialDeviceCommandLineApplication;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
abstract class BaseSensorBoardConsole extends SerialDeviceCommandLineApplication
   {
   private final Runnable enumeratePortsAction =
         new Runnable()
         {
         public void run()
            {
            enumeratePorts();
            }
         };

   private final Runnable connectAction =
         new Runnable()
         {
         public void run()
            {
            if (isInitialized())
               {
               println("You are already connected.");
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
                        if (!connect(serialPortName))
                           {
                           println("Connection failed!");
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

   private final Runnable temperaturesAction =
         new Runnable()
         {
         public void run()
            {
            if (isInitialized())
               {
               println("Temperatures: " + getTemperatures());
               }
            else
               {
               println("You must be connected to the sensor board first.");
               }
            }
         };

   private final Runnable currentsAction =
         new Runnable()
         {
         public void run()
            {
            if (isInitialized())
               {
               println("Currents: " + getCurrents());
               }
            else
               {
               println("You must be connected to the sensor board first.");
               }
            }
         };

   private final Runnable pedalPositionsAction =
         new Runnable()
         {
         public void run()
            {
            if (isInitialized())
               {
               println("Pedal Positions: " + getPedalPositions());
               }
            else
               {
               println("You must be connected to the sensor board first.");
               }
            }
         };

   private final Runnable voltagesAction =
         new Runnable()
         {
         public void run()
            {
            if (isInitialized())
               {
               println("Voltages: " + getVoltages());
               }
            else
               {
               println("You must be connected to the sensor board first.");
               }
            }
         };

   private final Runnable disconnectAction =
         new Runnable()
         {
         public void run()
            {
            disconnect();
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

   BaseSensorBoardConsole(BufferedReader in)
      {
      super(in);

      registerAction("?", enumeratePortsAction);
      registerAction("c", connectAction);
      registerAction("t", temperaturesAction);
      registerAction("i", currentsAction);
      registerAction("p", pedalPositionsAction);
      registerAction("v", voltagesAction);
      registerAction("d", disconnectAction);
      registerAction(QUIT_COMMAND, quitAction);
      }

   protected abstract boolean connect(final String serialPortName);

   protected abstract Temperatures getTemperatures();

   protected abstract Currents getCurrents();

   protected abstract PedalPositions getPedalPositions();

   protected abstract Voltages getVoltages();

   protected abstract boolean isInitialized();

   protected final void menu()
      {
      println("COMMANDS -----------------------------------");
      println("");
      println("?         List all available serial ports");
      println("");
      println("c         Connect to the sensor board on the given serial port");
      println("i         Display currents");
      println("p         Display pedeal positions");
      println("t         Display temperatures");
      println("v         Display voltages");
      println("d         Disconnect from the sensor board");
      println("");
      println("q         Quit");
      println("");
      println("--------------------------------------------");
      }
   }
