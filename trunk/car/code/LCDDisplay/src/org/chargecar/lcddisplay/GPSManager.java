package org.chargecar.lcddisplay;

import org.chargecar.honda.StreamingSerialPortDeviceController;
import org.chargecar.honda.StreamingSerialPortDeviceModel;
import org.chargecar.honda.gps.GPSController;
import org.chargecar.honda.gps.GPSEvent;
import org.chargecar.honda.gps.GPSModel;
import org.chargecar.serial.streaming.StreamingSerialPortDeviceManager;

/**
 * <code>GPSManager</code> is a singleton which acts as a front-end for GPS data.  The singleton instance is created
 * lazily and, upon creation, connects to the GPS system.  The serial port name is assumed to be defined in a system
 * property whose key is the concatenation of {@link LCDConstants#SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX} and
 * {@link #DEVICE_NAME}.  If no such system property exists, then {@link #getInstance()} will return <code>null</code>.
 * Once a connection is established, it immediately starts reading data.  The most recent data can be obtained via the
 * {@link #getData()} method.
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GPSManager extends StreamingSerialPortDeviceManager<GPSEvent, GPSEvent>
   {
   private static final String DEVICE_NAME = "gps";

   private static class LazyHolder
      {
      private static final GPSManager INSTANCE;

      static
         {
         final String serialPortName = System.getProperty(LCDConstants.SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX + DEVICE_NAME, null);
         if (serialPortName == null)
            {
            INSTANCE = null;
            }
         else
            {
            final GPSModel model = new GPSModel();
            final GPSController controller = GPSController.create(serialPortName, model);
            INSTANCE = new GPSManager(model, controller);
            }
         }

      private LazyHolder()
         {
         // private to prevent instantiation
         }
      }

   public static GPSManager getInstance()
      {
      return LazyHolder.INSTANCE;
      }

   private GPSManager(final StreamingSerialPortDeviceModel<GPSEvent, GPSEvent> model,
                      final StreamingSerialPortDeviceController<GPSEvent, GPSEvent> controller)
      {
      super(model, controller);
      }
   }
