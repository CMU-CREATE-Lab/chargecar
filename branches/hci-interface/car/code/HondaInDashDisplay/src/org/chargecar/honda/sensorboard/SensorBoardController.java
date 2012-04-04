package org.chargecar.honda.sensorboard;

import org.chargecar.honda.StreamingSerialPortDeviceController;

/**
 * <p>
 * <code>SensorBoardController</code> is the MVC controller class for the {@link SensorBoardModel} and {@link SensorBoardView}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SensorBoardController extends StreamingSerialPortDeviceController<SensorBoardEvent, SensorBoardEvent>
   {
   private final SensorBoardModel model;

   public static SensorBoardController create(final String serialPortName, final SensorBoardModel model)
      {
      final SensorBoardReader reader;
      final String deviceName;
      if (StreamingSerialPortDeviceController.shouldUseFakeDevice())
         {
         deviceName = "Fake SensorBoard";
         reader = new SensorBoardReader(new FakeSensorBoard());
         }
      else
         {
         deviceName = "SensorBoard";
         if (serialPortName == null)
            {
            reader = null;
            }
         else
            {
            reader = new SensorBoardReader(serialPortName);
            }
         }

      return new SensorBoardController(deviceName, reader, model);
      }

   private SensorBoardController(final String deviceName, final SensorBoardReader reader, final SensorBoardModel model)
      {
      super(deviceName, reader, model);
      this.model = model;
      }
   }
