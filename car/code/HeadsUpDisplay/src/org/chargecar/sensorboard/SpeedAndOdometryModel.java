package org.chargecar.sensorboard;

import java.util.ArrayList;
import java.util.List;
import edu.cmu.ri.createlab.collections.Dataset;

/**
 * <p>
 * <code>SpeedAndOdometryModel</code> keeps track of speed and odometry data.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SpeedAndOdometryModel
   {
   private static final double HOURS_PER_MILLISECOND = 1 / 3600000.0;

   private final byte[] dataSynchronizationLock = new byte[0];
   private List<SpeedEventListener> eventListeners = new ArrayList<SpeedEventListener>();
   private final Dataset<Integer> speedsDataset = new Dataset<Integer>(60);
   private Speed previousSpeed = null;
   private double odometer = 0.0;       // todo: initialize this with persistent value
   private double tripOdometer = 0.0;   // todo: initialize this with persistent value

   public void addEventListener(final SpeedEventListener listener)
      {
      if (listener != null)
         {
         eventListeners.add(listener);
         }
      }

   public void update(final Speed speed)
      {
      if (speed != null)
         {
         final SpeedAndOdometryImpl speedAndOdometry;
         synchronized (dataSynchronizationLock)
            {
            final Integer mph = speed.getSpeed();
            speedsDataset.append(mph);

            // if the previous speed isn't null, then calculate the odometry change
            if (previousSpeed != null)
               {
               final long elapsedMilliseconds = speed.getTimestampMilliseconds() - previousSpeed.getTimestampMilliseconds();
               final double distanceTraveledInMiles = mph * HOURS_PER_MILLISECOND * elapsedMilliseconds;
               odometer += distanceTraveledInMiles;
               tripOdometer += distanceTraveledInMiles;
               }

            // save the current speed 
            previousSpeed = speed;

            speedAndOdometry = new SpeedAndOdometryImpl(speed, odometer, tripOdometer);
            }

         // notify listeners
         if (!eventListeners.isEmpty())
            {
            for (final SpeedEventListener listener : eventListeners)
               {
               listener.handleEvent(speedAndOdometry);
               }
            }
         }
      }

   private static class SpeedAndOdometryImpl implements SpeedAndOdometry
      {
      private final Speed speed;
      private final double odometer;
      private final double tripOdometer;

      private SpeedAndOdometryImpl(final Speed speed, final double odometer, final double tripOdometer)
         {
         this.speed = speed;
         this.odometer = odometer;
         this.tripOdometer = tripOdometer;
         }

      public long getTimestampMilliseconds()
         {
         return speed.getTimestampMilliseconds();
         }

      public Integer getSpeed()
         {
         return speed.getSpeed();
         }

      public double getOdometer()
         {
         return odometer;
         }

      public double getTripOdometer()
         {
         return tripOdometer;
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

         final SpeedAndOdometryImpl that = (SpeedAndOdometryImpl)o;

         if (Double.compare(that.odometer, odometer) != 0)
            {
            return false;
            }
         if (Double.compare(that.tripOdometer, tripOdometer) != 0)
            {
            return false;
            }
         if (speed != null ? !speed.equals(that.speed) : that.speed != null)
            {
            return false;
            }

         return true;
         }

      @Override
      public int hashCode()
         {
         int result;
         long temp;
         result = speed != null ? speed.hashCode() : 0;
         temp = odometer != +0.0d ? Double.doubleToLongBits(odometer) : 0L;
         result = 31 * result + (int)(temp ^ (temp >>> 32));
         temp = tripOdometer != +0.0d ? Double.doubleToLongBits(tripOdometer) : 0L;
         result = 31 * result + (int)(temp ^ (temp >>> 32));
         return result;
         }

      @Override
      public String toString()
         {
         final StringBuilder sb = new StringBuilder();
         sb.append("SpeedAndOdometry");
         sb.append("{speed=").append(speed);
         sb.append(", odometer=").append(odometer);
         sb.append(", tripOdometer=").append(tripOdometer);
         sb.append('}');
         return sb.toString();
         }
      }
   }
