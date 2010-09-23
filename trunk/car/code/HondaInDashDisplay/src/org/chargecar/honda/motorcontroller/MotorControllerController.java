package org.chargecar.honda.motorcontroller;

import org.apache.log4j.Logger;
import org.chargecar.honda.StreamingSerialPortDeviceController;

/**
 * <p>
 * <code>MotorControllerController</code> is the MVC controller class for the {@link MotorControllerModel} and {@link MotorControllerView}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class MotorControllerController extends StreamingSerialPortDeviceController<MotorControllerEvent, MotorControllerEvent>
   {
   private static final Logger LOG = Logger.getLogger(MotorControllerController.class);

   private final MotorControllerModel model;

   public static MotorControllerController create(final String serialPortName, final MotorControllerModel model)
      {
      final MotorControllerReader reader;
      final String deviceName;
      if (StreamingSerialPortDeviceController.shouldUseFakeDevice())
         {
         deviceName = "Fake MotorController";
         reader = new MotorControllerReader(new FakeMotorController());
         }
      else
         {
         deviceName = "MotorController";
         if (serialPortName == null)
            {
            reader = null;
            }
         else
            {
            reader = new MotorControllerReader(serialPortName);
            }
         }

      return new MotorControllerController(deviceName, reader, model);
      }

   private MotorControllerController(final String deviceName, final MotorControllerReader reader, final MotorControllerModel model)
      {
      super(deviceName, reader, model);
      this.model = model;
      }
   }
