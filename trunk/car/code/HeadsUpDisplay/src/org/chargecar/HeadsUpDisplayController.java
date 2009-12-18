package org.chargecar;

import edu.cmu.ri.createlab.serial.device.SerialDeviceProxyProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.sensorboard.Currents;
import org.chargecar.sensorboard.PedalPositions;
import org.chargecar.sensorboard.Temperatures;
import org.chargecar.sensorboard.Voltages;
import org.chargecar.sensorboard.serial.proxy.SensorBoardProxy;

/**
 * <p>
 * <code>HeadsUpDisplayController</code> is a simple controller with acts as a bridge between the sensor board (the
 * model) and the HUD (the view).
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class HeadsUpDisplayController implements Runnable
   {
   private static final Log LOG = LogFactory.getLog(HeadsUpDisplayController.class);

   private final SerialDeviceProxyProvider serialDeviceProxyProvider;
   private final HeadsUpDisplayView headsUpDisplayView;

   HeadsUpDisplayController(final SerialDeviceProxyProvider serialDeviceProxyProvider, final HeadsUpDisplayView headsUpDisplayView)
      {
      this.serialDeviceProxyProvider = serialDeviceProxyProvider;
      this.headsUpDisplayView = headsUpDisplayView;
      }

   public void run()
      {
      final SensorBoardProxy sensorBoardProxy = (SensorBoardProxy)serialDeviceProxyProvider.getSerialDeviceProxy();

      final Integer speed = sensorBoardProxy.getSpeed();
      LOG.info("Speed:\t" + speed);

      final Currents currents = sensorBoardProxy.getCurrents();
      LOG.info(currents);

      final PedalPositions pedalPositions = sensorBoardProxy.getPedalPositions();
      LOG.info(pedalPositions);

      final Temperatures temperatures = sensorBoardProxy.getTemperatures();
      LOG.info(temperatures);

      final Voltages voltages = sensorBoardProxy.getVoltages();
      LOG.info(voltages);

      // update the GUI
      headsUpDisplayView.setSpeed(speed);
      }
   }
