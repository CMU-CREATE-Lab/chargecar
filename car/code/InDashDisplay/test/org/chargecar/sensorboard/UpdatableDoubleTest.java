package org.chargecar.sensorboard;

import junit.framework.TestCase;

/**
 * <p>
 * <code>UpdatableDoubleTest</code> tests the {@link UpdatableDouble} class.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class UpdatableDoubleTest extends TestCase
   {
   public UpdatableDoubleTest(final String test)
      {
      super(test);
      }

   public void testConstruction()
      {
      constructionHelper(0.0);
      constructionHelper(Double.MAX_VALUE);
      constructionHelper(Double.MIN_VALUE);
      constructionHelper(0.01);
      constructionHelper(-0.01);
      constructionHelper(null);
      }

   public void testUpdate()
      {
      final UpdatableDouble minimumDouble1 = UpdatableDouble.createMinimumDouble(10000.0);
      final UpdatableDouble minimumDouble2 = UpdatableDouble.createMinimumDouble(null);

      for (double v = 10.0; v > 0.0; v--)
         {
         minimumDouble1.update(v);
         minimumDouble2.update(v);
         assertEquals(v, minimumDouble1.getValue());
         assertEquals(v, minimumDouble2.getValue());
         }

      final UpdatableDouble maximumDouble1 = UpdatableDouble.createMaximumDouble(-10000.0);
      final UpdatableDouble maximumDouble2 = UpdatableDouble.createMaximumDouble(null);

      for (double v = 0; v < 10.0; v++)
         {
         maximumDouble1.update(v);
         maximumDouble2.update(v);
         assertEquals(v, maximumDouble1.getValue());
         assertEquals(v, maximumDouble2.getValue());
         }

      final UpdatableDouble minimumDouble3 = UpdatableDouble.createMinimumDouble(-10000.0);
      final UpdatableDouble maximumDouble3 = UpdatableDouble.createMaximumDouble(10000.0);
      for (double v = -10; v < 10.0; v++)
         {
         minimumDouble3.update(v);
         maximumDouble3.update(v);
         assertEquals(-10000.0, minimumDouble3.getValue());
         assertEquals(10000.0, maximumDouble3.getValue());
         }
      }

   public void testSetValue()
      {
      final UpdatableDouble minimumDouble1 = UpdatableDouble.createMinimumDouble(10000.0);
      final UpdatableDouble minimumDouble2 = UpdatableDouble.createMinimumDouble(null);

      for (double v = 10.0; v > 0.0; v--)
         {
         minimumDouble1.setValue(v);
         minimumDouble2.setValue(v);
         assertEquals(v, minimumDouble1.getValue());
         assertEquals(v, minimumDouble2.getValue());
         }

      final UpdatableDouble maximumDouble1 = UpdatableDouble.createMaximumDouble(-10000.0);
      final UpdatableDouble maximumDouble2 = UpdatableDouble.createMaximumDouble(null);

      for (double v = 0; v < 10.0; v++)
         {
         maximumDouble1.setValue(v);
         maximumDouble2.setValue(v);
         assertEquals(v, maximumDouble1.getValue());
         assertEquals(v, maximumDouble2.getValue());
         }

      final UpdatableDouble minimumDouble3 = UpdatableDouble.createMinimumDouble(-10000.0);
      final UpdatableDouble maximumDouble3 = UpdatableDouble.createMaximumDouble(10000.0);
      for (double v = -10; v < 10.0; v++)
         {
         minimumDouble3.setValue(v);
         maximumDouble3.setValue(v);
         assertEquals(v, minimumDouble3.getValue());
         assertEquals(v, maximumDouble3.getValue());
         }
      }

   private void constructionHelper(final Double initialValue)
      {
      final UpdatableDouble minimumDouble = UpdatableDouble.createMinimumDouble(initialValue);
      assertNotNull(minimumDouble);
      assertEquals(initialValue, minimumDouble.getValue());

      final UpdatableDouble maximumDouble = UpdatableDouble.createMaximumDouble(initialValue);
      assertNotNull(maximumDouble);
      assertEquals(initialValue, maximumDouble.getValue());
      }
   }