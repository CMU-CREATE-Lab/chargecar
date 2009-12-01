package org.chargecar.sensorboard;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import edu.cmu.ri.createlab.serial.device.SerialDevicePingFailureEventListener;
import org.chargecar.sensorboard.serial.proxy.SensorBoardProxy;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SensorBoardConsole extends BaseSensorBoardConsole
   {
   public static void main(final String[] args)
      {
      final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

      new SensorBoardConsole(in).run();
      }

   private SensorBoardProxy sensorBoardProxy;

   private SensorBoardConsole(BufferedReader in)
      {
      super(in);
      }

   protected boolean connect(final String serialPortName)
      {
      sensorBoardProxy = SensorBoardProxy.create(serialPortName);

      if (sensorBoardProxy == null)
         {
         println("Connection failed.");
         return false;
         }
      else
         {
         println("Connection successful.");
         sensorBoardProxy.addSerialDevicePingFailureEventListener(
               new SerialDevicePingFailureEventListener()
               {
               public void handlePingFailureEvent()
                  {
                  println("Finch ping failure detected.  You will need to reconnect.");
                  sensorBoardProxy = null;
                  }
               });
         return true;
         }
      }

   protected ArrayList<Double> getVoltages()
      {
      return sensorBoardProxy.getVoltages();
      }

   protected boolean isInitialized()
      {
      return sensorBoardProxy != null;
      }

   protected void disconnect()
      {
      if (sensorBoardProxy != null)
         {
         sensorBoardProxy.disconnect();
         sensorBoardProxy = null;
         }
      }
   }
