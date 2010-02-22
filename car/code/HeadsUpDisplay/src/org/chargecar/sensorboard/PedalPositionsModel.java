package org.chargecar.sensorboard;

/**
 * <p>
 * <code>PedalPositionsModel</code> keeps track of pedal positions.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class PedalPositionsModel extends Model<PedalPositions, PedalPositions>
   {
   private final byte[] dataSynchronizationLock = new byte[0];

   @Override
   public PedalPositions update(final PedalPositions data)
      {
      synchronized (dataSynchronizationLock)
         {
         publishEventToListeners(data);

         return data;
         }
      }
   }
