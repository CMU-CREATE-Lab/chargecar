package org.chargecar.sensorboard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * <code>SpeedAndOdometryModel</code> keeps track of speed and odometry data.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SpeedAndOdometryModel extends Model<Speed, SpeedAndOdometry>
   {
   private static final Log LOG = LogFactory.getLog(SpeedAndOdometryModel.class);

   private static final File ODOMETER_DATA_STORE_FILE = new File("Odometer.txt");
   private static final String ODOMETER_PROPERTY_KEY = "odometer";
   private static final String TRIP_ODOMETER_1_PROPERTY_KEY = "trip-odometer-1";
   private static final String TRIP_ODOMETER_2_PROPERTY_KEY = "trip-odometer-2";

   private final byte[] dataSynchronizationLock = new byte[0];
   private final Properties odometerDataStore = new Properties();
   private Speed previousSpeed = null;
   private double odometer = 0.0;
   private double tripOdometer1 = 0.0;
   private double tripOdometer2 = 0.0;

   public SpeedAndOdometryModel()
      {
      synchronized (dataSynchronizationLock)
         {
         try
            {
            odometerDataStore.loadFromXML(new FileInputStream(ODOMETER_DATA_STORE_FILE));
            odometer = Double.parseDouble(odometerDataStore.getProperty(ODOMETER_PROPERTY_KEY, "0.0"));
            tripOdometer1 = Double.parseDouble(odometerDataStore.getProperty(TRIP_ODOMETER_1_PROPERTY_KEY, "0.0"));
            tripOdometer2 = Double.parseDouble(odometerDataStore.getProperty(TRIP_ODOMETER_2_PROPERTY_KEY, "0.0"));
            }
         catch (Exception e)
            {
            LOG.info("Failed to load the odometer data store, creating a new one...");
            writeOdometerDataStore();
            }
         }
      }

   public void update(final Speed speed)
      {
      if (speed != null)
         {
         synchronized (dataSynchronizationLock)
            {
            final Integer mph = speed.getSpeed();

            // if the previous speed isn't null, then calculate the odometry change
            if (previousSpeed != null)
               {
               final long elapsedMilliseconds = speed.getTimestampMilliseconds() - previousSpeed.getTimestampMilliseconds();
               final double distanceTraveledInMiles = mph * SensorBoardConstants.HOURS_PER_MILLISECOND * elapsedMilliseconds;
               odometer += distanceTraveledInMiles;
               tripOdometer1 += distanceTraveledInMiles;
               tripOdometer2 += distanceTraveledInMiles;
               }

            // save the current speed 
            previousSpeed = speed;

            // update the data store
            writeOdometerDataStore();

            // notify listeners
            publishEventToListeners(new SpeedAndOdometryImpl(speed, odometer, tripOdometer1, tripOdometer2));
            }
         }
      }

   private void writeOdometerDataStore()
      {
      try
         {
         odometerDataStore.setProperty(ODOMETER_PROPERTY_KEY, String.valueOf(odometer));
         odometerDataStore.setProperty(TRIP_ODOMETER_1_PROPERTY_KEY, String.valueOf(tripOdometer1));
         odometerDataStore.setProperty(TRIP_ODOMETER_2_PROPERTY_KEY, String.valueOf(tripOdometer2));
         odometerDataStore.storeToXML(new FileOutputStream(ODOMETER_DATA_STORE_FILE), "GENERATED FILE!  DO NOT EDIT!");
         }
      catch (Exception e)
         {
         LOG.error("Exception while trying to write to the odometer data store [" + ODOMETER_DATA_STORE_FILE.getAbsolutePath() + "]", e);
         }
      }

   private static final class SpeedAndOdometryImpl implements SpeedAndOdometry
      {
      private final Speed speed;
      private final double odometer;
      private final double tripOdometer1;
      private final double tripOdometer2;

      private SpeedAndOdometryImpl(final Speed speed, final double odometer, final double tripOdometer1, final double tripOdometer2)
         {
         this.speed = speed;
         this.odometer = odometer;
         this.tripOdometer1 = tripOdometer1;
         this.tripOdometer2 = tripOdometer2;
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

      public double getTripOdometer1()
         {
         return tripOdometer1;
         }

      public double getTripOdometer2()
         {
         return tripOdometer2;
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
         if (Double.compare(that.tripOdometer1, tripOdometer1) != 0)
            {
            return false;
            }
         if (Double.compare(that.tripOdometer2, tripOdometer2) != 0)
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
         temp = tripOdometer1 != +0.0d ? Double.doubleToLongBits(tripOdometer1) : 0L;
         result = 31 * result + (int)(temp ^ (temp >>> 32));
         temp = tripOdometer2 != +0.0d ? Double.doubleToLongBits(tripOdometer2) : 0L;
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
         sb.append(", tripOdometer1=").append(tripOdometer1);
         sb.append(", tripOdometer2=").append(tripOdometer2);
         sb.append('}');
         return sb.toString();
         }
      }
   }
