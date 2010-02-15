package org.chargecar.sensorboard;

import java.awt.Color;
import java.awt.Dimension;
import java.text.NumberFormat;
import java.util.List;
import org.jfree.chart.plot.dial.StandardDialRange;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface MeterConfig
   {
   int getDatasetCount();

   Dimension getSize();

   double getLowerBound();

   double getUpperBound();

   int getMajorTickIncrement();

   int getMinorTickCount();

   Color getDatasetColor(final int id);

   List<StandardDialRange> getDialRanges();

   String getLabelLine1();

   String getLabelLine2();

   NumberFormat getNumberFormat();
   }