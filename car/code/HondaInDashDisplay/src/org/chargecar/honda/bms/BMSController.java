package org.chargecar.honda.bms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.honda.StreamingSerialPortDeviceController;

/**
 * <p>
 * <code>BMSController</code> is the MVC controller class for the {@link BMSModel} and {@link BMSView}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class BMSController extends StreamingSerialPortDeviceController<BMSEvent, BMSEvent>
   {
   private static final Log LOG = LogFactory.getLog(BMSController.class);

   private final BMSModel model;

   public static BMSController create(final String serialPortName, final BMSModel model)
      {
      final BMSReader reader;
      final String deviceName;
      if (StreamingSerialPortDeviceController.shouldUseFakeDevice())
         {
         deviceName = "Fake BMS";
         reader = new BMSReader(new FakeBMS());
         }
      else
         {
         deviceName = "BMS";
         if (serialPortName == null)
            {
            reader = null;
            }
         else
            {
            reader = new BMSReader(serialPortName);
            }
         }

      return new BMSController(deviceName, reader, model);
      }

   private BMSController(final String deviceName, final BMSReader reader, final BMSModel model)
      {
      super(deviceName, reader, model);
      this.model = model;
      }
   }
