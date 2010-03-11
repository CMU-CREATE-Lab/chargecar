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
import org.chargecar.sensorboard.Efficiency;
import org.chargecar.sensorboard.EfficiencyModel;
import org.chargecar.sensorboard.Odometry;
import org.chargecar.sensorboard.PedalPositions;
import org.chargecar.sensorboard.PedalPositionsModel;
import org.chargecar.sensorboard.Power;
import org.chargecar.sensorboard.PowerAndOdometry;
import org.chargecar.sensorboard.PowerModel;
import org.chargecar.sensorboard.Speed;
import org.chargecar.sensorboard.SpeedAndOdometry;
import org.chargecar.sensorboard.SpeedAndOdometryModel;
import org.chargecar.sensorboard.Temperatures;
import org.chargecar.sensorboard.TemperaturesModel;
import org.chargecar.sensorboard.Voltages;
import org.chargecar.sensorboard.VoltagesAndCurrents;
import org.chargecar.sensorboard.serial.proxy.SensorBoardProxy;

/**
 * <p>
 * <code>InDashDisplayController</code> is a controller with acts as a bridge between the sensor board proxy (the
 * model) and its listeners (typcially GUI views).  It is a {@link SerialDeviceConnectionEventListener} which, upon
 * connection, starts a thread which reads from the sensor board once every second and publishes the data to its
 * listeners (e.g. the views).
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class InDashDisplayController
   {
   private static final Log LOG = LogFactory.getLog(InDashDisplayController.class);

   private final LifecycleManager lifecycleManager;
   private final SerialDeviceConnectivityManager serialDeviceConnectivityManager;
   private final ScheduledExecutorService dataAcquisitionExecutorService = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory("SensorBoardExecutorThreadFactory"));
   private ScheduledFuture<?> scheduledFuture = null;
   private final SpeedAndOdometryModel speedAndOdometryModel;
   private final TemperaturesModel temperaturesModel;
   private final PowerModel powerModel;
   private final EfficiencyModel efficiencyModel;
   private final PedalPositionsModel pedalPositionsModel;

   InDashDisplayController(final LifecycleManager lifecycleManager,
                           final SerialDeviceConnectivityManager serialDeviceConnectivityManager,
                           final SpeedAndOdometryModel speedAndOdometryModel,
                           final TemperaturesModel temperaturesModel,
                           final PowerModel powerModel,
                           final EfficiencyModel efficiencyModel,
                           final PedalPositionsModel pedalPositionsModel)
      {
      this.lifecycleManager = lifecycleManager;
      this.serialDeviceConnectivityManager = serialDeviceConnectivityManager;
      this.speedAndOdometryModel = speedAndOdometryModel;
      this.temperaturesModel = temperaturesModel;
      this.powerModel = powerModel;
      this.efficiencyModel = efficiencyModel;
      this.pedalPositionsModel = pedalPositionsModel;

      // register self as a SerialDeviceConnectionEventListener
      this.serialDeviceConnectivityManager.addConnectionEventListener(
            new SerialDeviceConnectionEventListener()
            {
            public void handleConnectionStateChange(final SerialDeviceConnectionState oldState, final SerialDeviceConnectionState newState, final String serialPortName)
               {
               if (LOG.isDebugEnabled())
                  {
                  LOG.debug("InDashDisplayController.handleConnectionStateChange(" + oldState.name() + "," + newState.name() + ")");
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
                        LOG.debug("InDashDisplayController.handleConnectionStateChange(): Successfully shut down data acquisition executor.");
                        }
                     catch (Exception e)
                        {
                        LOG.debug("InDashDisplayController.handleConnectionStateChange(): Exception caught while trying to shut down the data acquisition executor", e);
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
            final Currents currents = sensorBoardProxy.getCurrents();
            final Voltages voltages = sensorBoardProxy.getVoltages();
            final Boolean isCapacitorOverVoltage = sensorBoardProxy.isCapacitorOverVoltage();
            final PedalPositions pedalPositionsIn = sensorBoardProxy.getPedalPositions();
            final Temperatures temperaturesIn = sensorBoardProxy.getTemperatures();

            // update the models
            final Temperatures temperaturesOut = temperaturesModel.update(temperaturesIn);
            final SpeedAndOdometry speedAndOdometry = speedAndOdometryModel.update(speed);
            final Power power = powerModel.update(new VoltagesAndCurrentsImpl(voltages, currents, isCapacitorOverVoltage));
            final Efficiency efficiency = efficiencyModel.update(new PowerAndOdometryImpl(power, speedAndOdometry));
            final PedalPositions pedalPositionsOut = pedalPositionsModel.update(pedalPositionsIn);

            // write to log
            if (LOG.isInfoEnabled())
               {
               LOG.info(temperaturesOut.toLoggingString());
               LOG.info(speedAndOdometry.toLoggingString());
               LOG.info(power.toLoggingString());
               LOG.info(efficiency.toLoggingString());
               LOG.info(pedalPositionsOut.toLoggingString());
               }
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

   private static final class PowerAndOdometryImpl implements PowerAndOdometry
      {
      private final Power power;
      private final Odometry odometry;

      private PowerAndOdometryImpl(final Power power, final Odometry odometry)
         {
         this.power = power;
         this.odometry = odometry;
         }

      public Power getPower()
         {
         return power;
         }

      public Odometry getOdometry()
         {
         return odometry;
         }
      }
   }
