package org.chargecar.sensorboard;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class UpdatableDouble
   {
   interface InstanceCreationStrategy
      {
      UpdatableDouble createInstance(Double initalValue);
      }

   private interface UpdateStrategy
      {
      boolean shouldUpdateToNewValue(final Double oldValue, final Double newValue);
      }

   static final InstanceCreationStrategy MINIMUM_DOUBLE_CREATION_STRATEGY =
         new InstanceCreationStrategy()
         {
         public UpdatableDouble createInstance(final Double initalValue)
            {
            return UpdatableDouble.createMinimumDouble(initalValue);
            }
         };

   static final InstanceCreationStrategy MAXIMUM_DOUBLE_CREATION_STRATEGY =
         new InstanceCreationStrategy()
         {
         public UpdatableDouble createInstance(final Double initalValue)
            {
            return UpdatableDouble.createMaximumDouble(initalValue);
            }
         };

   private static final UpdateStrategy MINIMUM_DOUBLE_UPDATE_STRATEGY =
         new UpdateStrategy()
         {
         public boolean shouldUpdateToNewValue(final Double oldValue, final Double newValue)
            {
            return Double.compare(newValue, oldValue) < 0;
            }
         };

   private static final UpdateStrategy MAXIMUM_DOUBLE_UPDATE_STRATEGY =
         new UpdateStrategy()
         {
         public boolean shouldUpdateToNewValue(final Double oldValue, final Double newValue)
            {
            final int i = Double.compare(newValue, oldValue);
            return i > 0;
            }
         };

   public static UpdatableDouble createMinimumDouble(final Double initialValue)
      {
      return new UpdatableDouble(initialValue, MINIMUM_DOUBLE_UPDATE_STRATEGY);
      }

   public static UpdatableDouble createMaximumDouble(final Double initialValue)
      {
      return new UpdatableDouble(initialValue, MAXIMUM_DOUBLE_UPDATE_STRATEGY);
      }

   static UpdatableDouble[] createArray(final UpdatableDouble.InstanceCreationStrategy updatableDoubleCreationStrategy, final int length)
      {
      final UpdatableDouble[] array = new UpdatableDouble[length];
      for (int i = 0; i < array.length; i++)
         {
         array[i] = updatableDoubleCreationStrategy.createInstance(null);
         }

      return array;
      }

   private Double value;
   private final UpdateStrategy updateStrategy;

   private UpdatableDouble(final Double initialValue, final UpdateStrategy updateStrategy)
      {
      this.value = initialValue;
      this.updateStrategy = updateStrategy;
      }

   public Double update(final Double newValue)
      {
      if (newValue != null)
         {
         if ((value == null) || updateStrategy.shouldUpdateToNewValue(value, newValue))
            {
            value = newValue;
            }
         }
      return value;
      }

   public Double getValue()
      {
      return value;
      }

   public void setValue(final Double newValue)
      {
      value = newValue;
      }
   }
