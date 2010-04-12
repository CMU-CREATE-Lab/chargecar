package org.chargecar.sensorboard;

/**
 * <p>
 * <code>PowerModel</code> keeps track of efficiency.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class EfficiencyModel extends Model<PowerAndOdometry, Efficiency>
   {
   private final byte[] dataSynchronizationLock = new byte[0];
   private double distanceTraveled = 0.0;
   private double batteryPowerUsed = 0.0;

   @Override
   public Efficiency update(final PowerAndOdometry data)
      {
      if (data != null && data.getPower() != null && data.getOdometry() != null)
         {
         synchronized (dataSynchronizationLock)
            {
            distanceTraveled += data.getOdometry().getOdometerDelta();
            batteryPowerUsed += data.getPower().getBatteryPowerEquation().getKilowattHoursDelta();

            final EfficiencyImpl efficiency = new EfficiencyImpl(distanceTraveled, batteryPowerUsed);

            publishEventToListeners(efficiency);
            return efficiency;
            }
         }

      return null;
      }

   public void resetBatteryEfficiency()
      {
      synchronized (dataSynchronizationLock)
         {
         distanceTraveled = 0.0;
         batteryPowerUsed = 0.0;
         }
      }

   private static final class EfficiencyImpl implements Efficiency
      {
      private final double batteryEfficiency;

      private EfficiencyImpl(final double distanceTraveled, final double batteryPowerUsed)
         {
         if (Double.compare(0.0, batteryPowerUsed) == 0)
            {
            batteryEfficiency = 0.0;
            }
         else
            {
            // multiply by -1 since battery power used is negative and we want efficiency to be a positive number
            batteryEfficiency = distanceTraveled / batteryPowerUsed * -1;
            }
         }

      public double getBatteryEfficiency()
         {
         return batteryEfficiency;
         }

      @Override
      public boolean equals(final Object o)
         {
         if (this == o)
            {
            return true;
            }
         if (o == null || getClass() != o.getClass())
            {
            return false;
            }

         final EfficiencyImpl that = (EfficiencyImpl)o;

         if (Double.compare(that.batteryEfficiency, batteryEfficiency) != 0)
            {
            return false;
            }

         return true;
         }

      @Override
      public int hashCode()
         {
         final long temp = batteryEfficiency != +0.0d ? Double.doubleToLongBits(batteryEfficiency) : 0L;
         return (int)(temp ^ (temp >>> 32));
         }

      @Override
      public String toString()
         {
         return toString("batteryEfficiency=");
         }

      public String toLoggingString()
         {
         return toString("");
         }

      private String toString(final String field1)
         {
         final StringBuilder sb = new StringBuilder();
         sb.append("Efficiency");
         sb.append("{");
         sb.append(field1).append(batteryEfficiency);
         sb.append('}');
         return sb.toString();
         }
      }
   }
