package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class UpdatableVoltages implements Voltages
   {
   public static UpdatableVoltages createMinimumVoltages()
      {
      return new UpdatableVoltages(UpdatableDouble.MINIMUM_DOUBLE_CREATION_STRATEGY);
      }

   public static UpdatableVoltages createMaximumVoltages()
      {
      return new UpdatableVoltages(UpdatableDouble.MAXIMUM_DOUBLE_CREATION_STRATEGY);
      }

   private final UpdatableDouble[] batteryVoltages;
   private final UpdatableDouble capacitorVoltage;
   private final UpdatableDouble accessoryVoltage;
   private long timestamp = System.currentTimeMillis();

   private UpdatableVoltages(final UpdatableDouble.InstanceCreationStrategy updatableDoubleCreationStrategy)
      {
      batteryVoltages = UpdatableDouble.createArray(updatableDoubleCreationStrategy, SensorBoardConstants.BATTERY_DEVICE_COUNT);
      capacitorVoltage = updatableDoubleCreationStrategy.createInstance(null);
      accessoryVoltage = updatableDoubleCreationStrategy.createInstance(null);
      }

   public Double getBatteryVoltage(final int batteryId)
      {
      if (batteryId >= 0 && batteryId < SensorBoardConstants.BATTERY_DEVICE_COUNT)
         {
         return batteryVoltages[batteryId].getValue();
         }
      throw new IllegalArgumentException("Invalid battery ID [" + batteryId + "], value must be a positive integer less than [" + SensorBoardConstants.BATTERY_DEVICE_COUNT + "]");
      }

   public void setBatteryVoltage(final int batteryId, final Double value)
      {
      if (batteryId >= 0 && batteryId < SensorBoardConstants.BATTERY_DEVICE_COUNT)
         {
         batteryVoltages[batteryId].setValue(value);
         }
      else
         {
         throw new IllegalArgumentException("Invalid battery ID [" + batteryId + "], value must be a positive integer less than [" + SensorBoardConstants.BATTERY_DEVICE_COUNT + "]");
         }
      }

   public Double getBatteryVoltage()
      {
      return batteryVoltages[0].getValue() + batteryVoltages[1].getValue() + batteryVoltages[2].getValue() + batteryVoltages[3].getValue();
      }

   public Double getCapacitorVoltage()
      {
      return capacitorVoltage.getValue();
      }

   public void setCapacitorVoltage(final Double value)
      {
      capacitorVoltage.setValue(value);
      }

   public Double getAccessoryVoltage()
      {
      return accessoryVoltage.getValue();
      }

   public void setAccessoryVoltage(final Double value)
      {
      accessoryVoltage.setValue(value);
      }

   public long getTimestampMilliseconds()
      {
      return timestamp;
      }

   public void update(final Voltages newVoltages)
      {
      if (newVoltages != null)
         {
         for (int i = 0; i < batteryVoltages.length; i++)
            {
            batteryVoltages[i].update(newVoltages.getBatteryVoltage(i));
            }

         capacitorVoltage.update(newVoltages.getCapacitorVoltage());
         accessoryVoltage.update(newVoltages.getAccessoryVoltage());

         timestamp = newVoltages.getTimestampMilliseconds();
         }
      }
   }
