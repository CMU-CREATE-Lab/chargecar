package org.chargecar.sensorboard;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;

/**
 * <p>
 * <code>ChartMouseAdapter</code> is an abstract, no-op {@link ChartMouseListener} which enables subclasses to choose
 * which methods to implement instead of having to implement the entire {@link ChartMouseListener} interface.

 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
abstract class ChartMouseAdapter implements ChartMouseListener
   {
   public void chartMouseClicked(final ChartMouseEvent event)
      {
      }

   public void chartMouseMoved(final ChartMouseEvent event)
      {
      }
   }
