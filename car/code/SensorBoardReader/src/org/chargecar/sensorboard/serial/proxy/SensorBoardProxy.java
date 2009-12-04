package org.chargecar.sensorboard.serial.proxy;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import edu.cmu.ri.createlab.serial.SerialPortCommandExecutionQueue;
import edu.cmu.ri.createlab.serial.SerialPortCommandResponse;
import edu.cmu.ri.createlab.serial.SerialPortCommandStrategy;
import edu.cmu.ri.createlab.serial.config.BaudRate;
import edu.cmu.ri.createlab.serial.config.CharacterSize;
import edu.cmu.ri.createlab.serial.config.FlowControl;
import edu.cmu.ri.createlab.serial.config.Parity;
import edu.cmu.ri.createlab.serial.config.SerialIOConfiguration;
import edu.cmu.ri.createlab.serial.config.StopBits;
import edu.cmu.ri.createlab.serial.device.SerialDevicePingFailureEventListener;
import edu.cmu.ri.createlab.serial.device.SerialDeviceProxy;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.sensorboard.Currents;
import org.chargecar.sensorboard.PedalPositions;
import org.chargecar.sensorboard.Temperatures;
import org.chargecar.sensorboard.Voltages;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class SensorBoardProxy implements SerialDeviceProxy
   {
   private static final Log LOG = LogFactory.getLog(SensorBoardProxy.class);

   public static final String APPLICATION_NAME = "SensorBoardProxy";
   private static final int DELAY_BETWEEN_PEER_PINGS = 2;

   /**
    * Tries to create a <code>SensorBoardProxy</code> for the the serial port specified by the given
    * <code>serialPortName</code>. Returns <code>null</code> if the connection could not be established.
    *
    * @param serialPortName - the name of the serial port device which should be used to establish the connection
    *
    * @throws IllegalArgumentException if the <code>serialPortName</code> is <code>null</code>
    */
   public static SensorBoardProxy create(final String serialPortName)
      {
      // a little error checking...
      if (serialPortName == null)
         {
         throw new IllegalArgumentException("The serial port name may not be null");
         }

      // create the serial port configuration
      final SerialIOConfiguration config = new SerialIOConfiguration(serialPortName,
                                                                     BaudRate.BAUD_19200,
                                                                     CharacterSize.EIGHT,
                                                                     Parity.NONE,
                                                                     StopBits.ONE,
                                                                     FlowControl.NONE);

      try
         {
         // create the serial port command queue
         final SerialPortCommandExecutionQueue commandQueue = SerialPortCommandExecutionQueue.create(APPLICATION_NAME, config);

         // see whether its creation was successful
         if (commandQueue == null)
            {
            if (LOG.isErrorEnabled())
               {
               LOG.error("Failed to open serial port '" + serialPortName + "'");
               }
            }
         else
            {
            if (LOG.isDebugEnabled())
               {
               LOG.debug("Serial port '" + serialPortName + "' opened.");
               }

            // now try to do the handshake with the sensor board to establish communication
            final boolean wasHandshakeSuccessful = commandQueue.executeAndReturnStatus(new HandshakeCommandStrategy());

            // see if the handshake was a success
            if (wasHandshakeSuccessful)
               {
               LOG.info("SensorBoard handshake successful!");

               // now create and return the proxy
               return new SensorBoardProxy(commandQueue);
               }
            else
               {
               LOG.error("Failed to handshake with sensor board");
               }

            // the handshake failed, so shutdown the command queue to release the serial port
            commandQueue.shutdown();
            }
         }
      catch (Exception e)
         {
         LOG.error("Exception while trying to create the SensorBoardProxy", e);
         }

      return null;
      }

   private final SerialPortCommandExecutionQueue commandQueue;
   private final GetSpeedCommandStrategy getSpeedCommandStrategy = new GetSpeedCommandStrategy();
   private final GetTemperaturesCommandStrategy getTemperaturesCommandStrategy = new GetTemperaturesCommandStrategy();
   private final GetCurrentsCommandStrategy getCurrentsCommandStrategy = new GetCurrentsCommandStrategy();
   private final GetPedalPositionsCommandStrategy getPedalPositionsCommandStrategy = new GetPedalPositionsCommandStrategy();
   private final GetVoltagesCommandStrategy getVoltagesCommandStrategy = new GetVoltagesCommandStrategy();
   private final SerialPortCommandStrategy disconnectCommandStrategy = new DisconnectCommandStrategy();
   private final ScheduledExecutorService peerPingScheduler = Executors.newScheduledThreadPool(1, new DaemonThreadFactory("SensorBoardProxy.peerPingScheduler"));
   private final ScheduledFuture<?> peerPingScheduledFuture;
   private final Collection<SerialDevicePingFailureEventListener> serialDevicePingFailureEventListeners = new HashSet<SerialDevicePingFailureEventListener>();

   public SensorBoardProxy(final SerialPortCommandExecutionQueue commandQueue)
      {
      this.commandQueue = commandQueue;

      // schedule periodic peer pings
      peerPingScheduledFuture = peerPingScheduler.scheduleAtFixedRate(new SensorBoardPinger(),
                                                                      DELAY_BETWEEN_PEER_PINGS, // delay before first ping
                                                                      DELAY_BETWEEN_PEER_PINGS, // delay between pings
                                                                      TimeUnit.SECONDS);
      }

   public void addSerialDevicePingFailureEventListener(final SerialDevicePingFailureEventListener listener)
      {
      if (listener != null)
         {
         serialDevicePingFailureEventListeners.add(listener);
         }
      }

   public void removeSerialDevicePingFailureEventListener(final SerialDevicePingFailureEventListener listener)
      {
      if (listener != null)
         {
         serialDevicePingFailureEventListeners.remove(listener);
         }
      }

   /**
    * Returns the speed; returns <code>null</code> if an error occurred while trying to read the value.
    */
   public Integer getSpeed()
      {
      final SerialPortCommandResponse response = commandQueue.execute(getSpeedCommandStrategy);

      return getSpeedCommandStrategy.convertResponse(response);
      }

   public Temperatures getTemperatures()
      {
      final SerialPortCommandResponse response = commandQueue.execute(getTemperaturesCommandStrategy);

      return getTemperaturesCommandStrategy.convertResponse(response);
      }

   public Currents getCurrents()
      {
      final SerialPortCommandResponse response = commandQueue.execute(getCurrentsCommandStrategy);

      return getCurrentsCommandStrategy.convertResponse(response);
      }

   public PedalPositions getPedalPositions()
      {
      final SerialPortCommandResponse response = commandQueue.execute(getPedalPositionsCommandStrategy);

      return getPedalPositionsCommandStrategy.convertResponse(response);
      }

   public Voltages getVoltages()
      {
      final SerialPortCommandResponse response = commandQueue.execute(getVoltagesCommandStrategy);

      return getVoltagesCommandStrategy.convertResponse(response);
      }

   public void disconnect()
      {
      disconnect(true);
      }

   private void disconnect(final boolean willAddDisconnectCommandToQueue)
      {
      if (LOG.isDebugEnabled())
         {
         LOG.debug("SensorBoardProxy.disconnect(" + willAddDisconnectCommandToQueue + ")");
         }

      // turn off the peer pinger
      try
         {
         peerPingScheduledFuture.cancel(false);
         peerPingScheduler.shutdownNow();
         LOG.debug("SensorBoardProxy.disconnect(): Successfully shut down sensor board pinger.");
         }
      catch (Exception e)
         {
         LOG.error("SensorBoardProxy.disconnect(): Exception caught while trying to shut down peer pinger", e);
         }

      // optionally send goodbye command to the sensor board
      if (willAddDisconnectCommandToQueue)
         {
         LOG.debug("SensorBoardProxy.disconnect(): Now attempting to send the disconnect command to the sensor board");
         if (commandQueue.executeAndReturnStatus(disconnectCommandStrategy))
            {
            LOG.debug("SensorBoardProxy.disconnect(): Successfully disconnected from the sensor board.");
            }
         else
            {
            LOG.error("SensorBoardProxy.disconnect(): Failed to disconnect from the sensor board.");
            }
         }

      // shut down the command queue, which closes the serial port
      commandQueue.shutdown();
      }

   private class SensorBoardPinger implements Runnable
      {
      public void run()
         {
         try
            {
            // for pings, we simply get the speed
            final Integer speed = getSpeed();

            // if the speed is null, then we know we have a problem so disconnect (which probably won't work) and then
            // notify the listeners
            if (speed == null)
               {
               try
                  {
                  LOG.debug("SensorBoardPinger.run(): Peer ping failed (received a null speed).  Attempting to disconnect...");
                  disconnect(false);
                  LOG.debug("SensorBoardPinger.run(): Done disconnecting from the sensor board");
                  }
               catch (Exception e)
                  {
                  LOG.error("SensorBoardPinger.run(): Exeption caught while trying to disconnect from the sensor board", e);
                  }

               if (LOG.isDebugEnabled())
                  {
                  LOG.debug("SensorBoardPinger.run(): Notifying " + serialDevicePingFailureEventListeners.size() + " listeners of ping failure...");
                  }
               for (final SerialDevicePingFailureEventListener listener : serialDevicePingFailureEventListeners)
                  {
                  try
                     {
                     if (LOG.isDebugEnabled())
                        {
                        LOG.debug("   SensorBoardPinger.run(): Notifying " + listener);
                        }
                     listener.handlePingFailureEvent();
                     }
                  catch (Exception e)
                     {
                     LOG.error("SensorBoardPinger.run(): Exeption caught while notifying SerialDevicePingFailureEventListener", e);
                     }
                  }
               }
            }
         catch (Exception e)
            {
            LOG.error("SensorBoardPinger.run(): Exception caught while executing the peer pinger", e);
            }
         }
      }
   }
