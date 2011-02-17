package org.chargecar.lcddisplay;

import org.chargecar.honda.StreamingSerialPortDeviceController;
import org.chargecar.honda.StreamingSerialPortDeviceModel;
import org.chargecar.honda.bms.BMSAndEnergy;
import org.chargecar.honda.bms.BMSController;
import org.chargecar.honda.bms.BMSEvent;
import org.chargecar.honda.bms.BMSModel;
import org.chargecar.serial.streaming.StreamingSerialPortDeviceManager;

/**
 * <code>BMSManager</code> is a singleton which acts as a front-end for BMS data.  The singleton instance is created
 * lazily and, upon creation, connects to the BMS system.  The serial port name is assumed to be defined in a system
 * property whose key is the concatenation of {@link LCDConstants#SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX} and
 * {@link #DEVICE_NAME}.  If no such system property exists, then {@link #getInstance()} will return <code>null</code>.
 * Once a connection is established, it immediately starts reading data.  The most recent data can be obtained via the
 * {@link #getData()} method.
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class BMSManager extends StreamingSerialPortDeviceManager<BMSEvent, BMSAndEnergy>
   {
   private static final String DEVICE_NAME = "bms";

   private static class LazyHolder
      {
      private static final BMSManager INSTANCE;

      static
         {
         final String serialPortName = System.getProperty(LCDConstants.SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX + DEVICE_NAME, null);
         if (serialPortName == null)
            {
            INSTANCE = null;
            }
         else
            {
            final BMSModel model = new BMSModel();
            final BMSController controller = BMSController.create(serialPortName, model);
            INSTANCE = new BMSManager(model, controller);
            }
         }

      private LazyHolder()
         {
         // private to prevent instantiation
         }
      }

   public static BMSManager getInstance()
      {
      return LazyHolder.INSTANCE;
      }

   private BMSManager(final StreamingSerialPortDeviceModel<BMSEvent, BMSAndEnergy> model,
                      final StreamingSerialPortDeviceController<BMSEvent, BMSAndEnergy> controller)
      {
      super(model, controller);
      }
   }
