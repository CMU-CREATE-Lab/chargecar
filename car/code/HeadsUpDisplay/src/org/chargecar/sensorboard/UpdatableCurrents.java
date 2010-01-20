package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class UpdatableCurrents implements Currents
   {
   public static UpdatableCurrents createMinimumCurrents()
      {
      return new UpdatableCurrents(UpdatableDouble.MINIMUM_DOUBLE_CREATION_STRATEGY);
      }

   public static UpdatableCurrents createMaximumCurrents()
      {
      return new UpdatableCurrents(UpdatableDouble.MAXIMUM_DOUBLE_CREATION_STRATEGY);
      }

   private final UpdatableDouble batteryCurrent;
   private final UpdatableDouble capacitorCurrent;
   private final UpdatableDouble accessoryCurrent;
   private final UpdatableDouble[] motorCurrents;
   private final UpdatableDouble[] auxiliaryCurrents;
   private long timestamp = System.currentTimeMillis();

   private UpdatableCurrents(final UpdatableDouble.InstanceCreationStrategy updatableDoubleCreationStrategy)
      {
      this.batteryCurrent = updatableDoubleCreationStrategy.createInstance(null);
      this.capacitorCurrent = updatableDoubleCreationStrategy.createInstance(null);
      this.accessoryCurrent = updatableDoubleCreationStrategy.createInstance(null);
      this.motorCurrents = UpdatableDouble.createArray(updatableDoubleCreationStrategy, SensorBoardConstants.MOTOR_DEVICE_COUNT);
      this.auxiliaryCurrents = UpdatableDouble.createArray(updatableDoubleCreationStrategy, SensorBoardConstants.AUXILIARY_DEVICE_COUNT);
      }

   public Double getBatteryCurrent()
      {
      return batteryCurrent.getValue();
      }

   public Double getCapacitorCurrent()
      {
      return capacitorCurrent.getValue();
      }

   public Double getAccessoryCurrent()
      {
      return accessoryCurrent.getValue();
      }

   public Double getMotorCurrent(final int motorId)
      {
      if (motorId >= 0 && motorId < SensorBoardConstants.MOTOR_DEVICE_COUNT)
         {
         return motorCurrents[motorId].getValue();
         }
      throw new IllegalArgumentException("Invalid motor ID [" + motorId + "], value must be a positive integer less than [" + SensorBoardConstants.MOTOR_DEVICE_COUNT + "]");
      }

   public Double getAuxiliaryCurrent(final int auxiliaryDeviceId)
      {
      if (auxiliaryDeviceId >= 0 && auxiliaryDeviceId < SensorBoardConstants.AUXILIARY_DEVICE_COUNT)
         {
         return auxiliaryCurrents[auxiliaryDeviceId].getValue();
         }
      throw new IllegalArgumentException("Invalid auxiliary device ID [" + auxiliaryDeviceId + "], value must be a positive integer less than [" + SensorBoardConstants.AUXILIARY_DEVICE_COUNT + "]");
      }

   public long getTimestampMilliseconds()
      {
      return timestamp;
      }

   public void update(final Currents newCurrents)
      {
      if (newCurrents != null)
         {
         batteryCurrent.update(newCurrents.getBatteryCurrent());
         capacitorCurrent.update(newCurrents.getCapacitorCurrent());
         accessoryCurrent.update(newCurrents.getAccessoryCurrent());

         for (int i = 0; i < motorCurrents.length; i++)
            {
            motorCurrents[i].update(newCurrents.getMotorCurrent(i));
            }
         for (int i = 0; i < auxiliaryCurrents.length; i++)
            {
            auxiliaryCurrents[i].update(newCurrents.getAuxiliaryCurrent(i));
            }

         timestamp = newCurrents.getTimestampMilliseconds();
         }
      }
   }
