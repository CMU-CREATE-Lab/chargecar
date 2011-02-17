package org.chargecar.lcddisplay;

import org.chargecar.honda.StreamingSerialPortDeviceController;
import org.chargecar.honda.StreamingSerialPortDeviceModel;
import org.chargecar.honda.sensorboard.SensorBoardController;
import org.chargecar.honda.sensorboard.SensorBoardEvent;
import org.chargecar.honda.sensorboard.SensorBoardModel;
import org.chargecar.serial.streaming.StreamingSerialPortDeviceManager;

/**
 * <code>SensorBoardManager</code> is a singleton which acts as a front-end for SensorBoard data.  The singleton
 * instance is created lazily and, upon creation, connects to the SensorBoard system.  The serial port name is assumed
 * to be defined in a system property whose key is the concatenation of
 * {@link LCDConstants#SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX} and {@link #DEVICE_NAME}.  If no such system property
 * exists, then {@link #getInstance()} will return <code>null</code>. Once a connection is established, it immediately
 * starts reading data.  The most recent data can be obtained via the {@link #getData()} method.
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SensorBoardManager extends StreamingSerialPortDeviceManager<SensorBoardEvent, SensorBoardEvent>
   {
   private static final String DEVICE_NAME = "sensor-board";

   private static class LazyHolder
      {
      private static final SensorBoardManager INSTANCE;

      static
         {
         final String serialPortName = System.getProperty(LCDConstants.SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX + DEVICE_NAME, null);
         if (serialPortName == null)
            {
            INSTANCE = null;
            }
         else
            {
            final SensorBoardModel model = new SensorBoardModel();
            final SensorBoardController controller = SensorBoardController.create(serialPortName, model);
            INSTANCE = new SensorBoardManager(model, controller);
            }
         }

      private LazyHolder()
         {
         // private to prevent instantiation
         }
      }

   public static SensorBoardManager getInstance()
      {
      return LazyHolder.INSTANCE;
      }

   private SensorBoardManager(final StreamingSerialPortDeviceModel<SensorBoardEvent, SensorBoardEvent> model,
                              final StreamingSerialPortDeviceController<SensorBoardEvent, SensorBoardEvent> controller)
      {
      super(model, controller);
      }
   }
