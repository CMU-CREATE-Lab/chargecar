package org.chargecar.honda.gps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.chargecar.honda.StreamingSerialPortDeviceController;

/**
 * <p>
 * <code>GPSController</code> is the MVC controller class for the {@link GPSModel} and {@link GPSView}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GPSController extends StreamingSerialPortDeviceController<GPSEvent, GPSEvent>
   {
   private static final Log LOG = LogFactory.getLog(GPSController.class);

   private final GPSModel model;

   public static GPSController create(final String serialPortName, final GPSModel model)
      {
      final NMEAReader reader;
      final String deviceName;
      if (StreamingSerialPortDeviceController.shouldUseFakeDevice())
         {
         deviceName = "Fake GPS";
         reader = new NMEAReader(new FakeGPS());
         }
      else
         {
         deviceName = "GPS";
         if (serialPortName == null)
            {
            reader = null;
            }
         else
            {
            reader = new NMEAReader(serialPortName);
            }
         }

      return new GPSController(deviceName, reader, model);
      }

   private GPSController(final String deviceName, final NMEAReader reader, final GPSModel model)
      {
      super(deviceName, reader, model);
      this.model = model;
      }
   }
