package org.chargecar.sensorboard;

/**
 * <p>
 * <code>TemperaturesModel</code> keeps track of temperatures.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class TemperaturesModel extends Model<Temperatures, Temperatures>
   {
   private final byte[] dataSynchronizationLock = new byte[0];

   public Temperatures update(final Temperatures data)
      {
      synchronized (dataSynchronizationLock)
         {
         publishEventToListeners(data);

         return data;
         }
      }
   }
