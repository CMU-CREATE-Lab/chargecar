package org.chargecar;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import edu.cmu.ri.createlab.serial.device.connectivity.SerialDeviceConnectionEventListener;
import edu.cmu.ri.createlab.serial.device.connectivity.SerialDeviceConnectionState;
import edu.cmu.ri.createlab.serial.device.connectivity.SerialDeviceConnectivityManager;
import edu.cmu.ri.createlab.util.thread.DaemonThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.sensorboard.Currents;
import org.chargecar.sensorboard.PedalPositions;
import org.chargecar.sensorboard.PowerModel;
import org.chargecar.sensorboard.Speed;
import org.chargecar.sensorboard.SpeedAndOdometryModel;
import org.chargecar.sensorboard.Temperatures;
import org.chargecar.sensorboard.TemperaturesModel;
import org.chargecar.sensorboard.Voltages;
import org.chargecar.sensorboard.VoltagesAndCurrents;
import org.chargecar.sensorboard.serial.proxy.SensorBoardProxy;

/**
 * <p>
 * <code>HeadsUpDisplayController</code> is a controller with acts as a bridge between the sensor board proxy (the
 * model) and its listeners (typcially GUI views).  It is a {@link SerialDeviceConnectionEventListener} which, upon
 * connection, starts a thread which reads from the sensor board once every second and publishes the data to its
 * listeners (e.g. the views).
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class HeadsUpDisplayController
   {
   private static final Log LOG = LogFactory.getLog(HeadsUpDisplayController.class);

   private final LifecycleManager lifecycleManager;
   private final SerialDeviceConnectivityManager serialDeviceConnectivityManager;
   private final ScheduledExecutorService dataAcquisitionExecutorService = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("SensorBoardExecutorThreadFactory"));
   private ScheduledFuture<?> scheduledFuture = null;
   private final SpeedAndOdometryModel speedAndOdometryModel;
   private final TemperaturesModel temperaturesModel;
   private final PowerModel powerModel;

   HeadsUpDisplayController(final LifecycleManager lifecycleManager,
                            final SerialDeviceConnectivityManager serialDeviceConnectivityManager,
                            final SpeedAndOdometryModel speedAndOdometryModel,
                            final TemperaturesModel temperaturesModel,
                            final PowerModel powerModel)
      {
      this.lifecycleManager = lifecycleManager;
      this.serialDeviceConnectivityManager = serialDeviceConnectivityManager;
      this.speedAndOdometryModel = speedAndOdometryModel;
      this.temperaturesModel = temperaturesModel;
      this.powerModel = powerModel;

      // register self as a SerialDeviceConnectionEventListener
      this.serialDeviceConnectivityManager.addConnectionEventListener(
            new SerialDeviceConnectionEventListener()
            {
            public void handleConnectionStateChange(final SerialDeviceConnectionState oldState, final SerialDeviceConnectionState newState, final String serialPortName)
               {
               if (LOG.isDebugEnabled())
                  {
                  LOG.debug("HeadsUpDisplayController.handleConnectionStateChange(" + oldState.name() + "," + newState.name() + ")");
                  }

               // don't bother doing anything if the state hasn't changed
               if (oldState.equals(newState))
                  {
                  return;
                  }

               if (SerialDeviceConnectionState.CONNECTED.equals(newState))
                  {
                  // start the data acquisition executor
                  scheduledFuture = dataAcquisitionExecutorService.scheduleAtFixedRate(new SensorBoardReader(), 0, 1, TimeUnit.SECONDS);
                  }
               else if (SerialDeviceConnectionState.DISCONNECTED.equals(newState))
                  {
                  // turn off the data acquisition executor
                  if (scheduledFuture != null)
                     {
                     try
                        {
                        scheduledFuture.cancel(false);
                        dataAcquisitionExecutorService.shutdownNow();
                        LOG.debug("HeadsUpDisplayController.handleConnectionStateChange(): Successfully shut down data acquisition executor.");
                        }
                     catch (Exception e)
                        {
                        LOG.debug("HeadsUpDisplayController.handleConnectionStateChange(): Exception caught while trying to shut down the data acquisition executor", e);
                        }
                     }
                  }
               }
            });
      }

   public void shutdown()
      {
      lifecycleManager.shutdown();
      }

   private final class SensorBoardReader implements Runnable
      {
      public void run()
         {
         // get a reference to the proxy
         final SensorBoardProxy sensorBoardProxy = (SensorBoardProxy)serialDeviceConnectivityManager.getSerialDeviceProxy();

         if (sensorBoardProxy != null)
            {
            // read the data
            final Speed speed = sensorBoardProxy.getSpeed();
            LOG.info(speed);

            final Currents currents = sensorBoardProxy.getCurrents();
            LOG.info(currents);

            final Voltages voltages = sensorBoardProxy.getVoltages();
            LOG.info(voltages);

            final Boolean isCapacitorOverVoltage = sensorBoardProxy.isCapacitorOverVoltage();
            LOG.info("isCapacitorOverVoltage = [" + isCapacitorOverVoltage + "]");

            final PedalPositions pedalPositions = sensorBoardProxy.getPedalPositions();
            LOG.info(pedalPositions);

            final Temperatures temperatures = sensorBoardProxy.getTemperatures();
            LOG.info(temperatures);

            // update the models
            speedAndOdometryModel.update(speed);
            temperaturesModel.update(temperatures);
            powerModel.update(new VoltagesAndCurrentsImpl(voltages, currents, isCapacitorOverVoltage));
            }
         }
      }

   private static final class VoltagesAndCurrentsImpl implements VoltagesAndCurrents
      {
      private final Voltages voltages;
      private final Currents currents;
      private final boolean isCapacitorOverVoltage;

      private VoltagesAndCurrentsImpl(final Voltages voltages, final Currents currents, final boolean isCapacitorOverVoltage)
         {
         this.voltages = voltages;
         this.currents = currents;
         this.isCapacitorOverVoltage = isCapacitorOverVoltage;
         }

      public Voltages getVoltages()
         {
         return voltages;
         }

      public Currents getCurrents()
         {
         return currents;
         }

      public boolean isCapacitorOverVoltage()
         {
         return isCapacitorOverVoltage;
         }
      }
   }
