package org.chargecar.sensorboard;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.SortedMap;
import edu.cmu.ri.createlab.serial.commandline.SerialDeviceCommandLineApplication;
import edu.cmu.ri.createlab.util.ArrayUtils;

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

   private final Runnable voltagesAction =
         new Runnable()
         {
         public void run()
            {
            if (isInitialized())
               {
               println(convertVoltagesToString());
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
      registerAction("v", voltagesAction);
      registerAction("d", disconnectAction);
      registerAction(QUIT_COMMAND, quitAction);
      }

   private String convertVoltagesToString()
      {
      return "Voltages: " + ArrayUtils.arrayToString(getVoltages().toArray());
      }

   protected abstract boolean connect(final String serialPortName);

   protected abstract ArrayList<Double> getVoltages();

   protected abstract boolean isInitialized();

   protected final void menu()
      {
      println("COMMANDS -----------------------------------");
      println("");
      println("?         List all available serial ports");
      println("");
      println("c         Connect to the sensor board on the given serial port");
      println("v         Display voltages");
      println("d         Disconnect from the sensor board");
      println("");
      println("q         Quit");
      println("");
      println("--------------------------------------------");
      }
   }
