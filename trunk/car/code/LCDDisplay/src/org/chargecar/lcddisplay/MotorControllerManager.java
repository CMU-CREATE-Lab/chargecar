package org.chargecar.lcddisplay;

import org.chargecar.honda.StreamingSerialPortDeviceController;
import org.chargecar.honda.StreamingSerialPortDeviceModel;
import org.chargecar.honda.motorcontroller.MotorControllerController;
import org.chargecar.honda.motorcontroller.MotorControllerEvent;
import org.chargecar.honda.motorcontroller.MotorControllerModel;
import org.chargecar.serial.streaming.StreamingSerialPortDeviceManager;

/**
 * <code>MotorControllerManager</code> is a singleton which acts as a front-end for MotorController data.  The singleton
 * instance is created lazily and, upon creation, connects to the MotorController system.  The serial port name is
 * assumed to be defined in a system property whose key is the concatenation of
 * {@link LCDConstants#SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX} and {@link #DEVICE_NAME}.  If no such system property
 * exists, then {@link #getInstance()} will return <code>null</code>. Once a connection is established, it immediately
 * starts reading data.  The most recent data can be obtained via the {@link #getData()} method.
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class MotorControllerManager extends StreamingSerialPortDeviceManager<MotorControllerEvent, MotorControllerEvent>
   {
   private static final String DEVICE_NAME = "motor-controller";

   private static class LazyHolder
      {
      private static final MotorControllerManager INSTANCE;

      static
         {
         final String serialPortName = System.getProperty(LCDConstants.SERIAL_PORT_SYSTEM_PROPERTY_KEY_PREFIX + DEVICE_NAME, null);
         if (serialPortName == null)
            {
            INSTANCE = null;
            }
         else
            {
            final MotorControllerModel model = new MotorControllerModel();
            final MotorControllerController controller = MotorControllerController.create(serialPortName, model);
            INSTANCE = new MotorControllerManager(model, controller);
            }
         }

      private LazyHolder()
         {
         // private to prevent instantiation
         }
      }

   public static MotorControllerManager getInstance()
      {
      return LazyHolder.INSTANCE;
      }

   private MotorControllerManager(final StreamingSerialPortDeviceModel<MotorControllerEvent, MotorControllerEvent> model,
                                  final StreamingSerialPortDeviceController<MotorControllerEvent, MotorControllerEvent> controller)
      {
      super(model, controller);
      }
   }
