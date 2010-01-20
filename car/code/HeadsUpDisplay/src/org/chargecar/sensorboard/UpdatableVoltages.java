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
   private final UpdatableDouble[] auxiliaryVoltages;
   private long timestamp = System.currentTimeMillis();

   private UpdatableVoltages(final UpdatableDouble.InstanceCreationStrategy updatableDoubleCreationStrategy)
      {
      batteryVoltages = UpdatableDouble.createArray(updatableDoubleCreationStrategy, SensorBoardConstants.BATTERY_DEVICE_COUNT);
      capacitorVoltage = updatableDoubleCreationStrategy.createInstance(null);
      accessoryVoltage = updatableDoubleCreationStrategy.createInstance(null);
      auxiliaryVoltages = UpdatableDouble.createArray(updatableDoubleCreationStrategy, SensorBoardConstants.AUXILIARY_DEVICE_COUNT);
      }

   public Double getBatteryVoltage(final int batteryId)
      {
      if (batteryId >= 0 && batteryId < SensorBoardConstants.BATTERY_DEVICE_COUNT)
         {
         return batteryVoltages[batteryId].getValue();
         }
      throw new IllegalArgumentException("Invalid battery ID [" + batteryId + "], value must be a positive integer less than [" + SensorBoardConstants.BATTERY_DEVICE_COUNT + "]");
      }

   public Double getBatteryVoltage()
      {
      return batteryVoltages[0].getValue() + batteryVoltages[1].getValue() + batteryVoltages[2].getValue() + batteryVoltages[3].getValue();
      }

   public Double getCapacitorVoltage()
      {
      return capacitorVoltage.getValue();
      }

   public Double getAccessoryVoltage()
      {
      return accessoryVoltage.getValue();
      }

   public Double getAuxiliaryVoltage(final int auxiliaryDeviceId)
      {
      if (auxiliaryDeviceId >= 0 && auxiliaryDeviceId < SensorBoardConstants.AUXILIARY_DEVICE_COUNT)
         {
         return auxiliaryVoltages[auxiliaryDeviceId].getValue();
         }
      throw new IllegalArgumentException("Invalid auxiliary device ID [" + auxiliaryDeviceId + "], value must be a positive integer less than [" + SensorBoardConstants.AUXILIARY_DEVICE_COUNT + "]");
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

         for (int i = 0; i < auxiliaryVoltages.length; i++)
            {
            auxiliaryVoltages[i].update(newVoltages.getAuxiliaryVoltage(i));
            }

         timestamp = newVoltages.getTimestampMilliseconds();
         }
      }
   }
