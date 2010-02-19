package org.chargecar.sensorboard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Point;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.plot.dial.StandardDialRange;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class DefaultMeterConfig implements MeterConfig
   {
   private final Color[] datasetColors;
   private Color backgroundColor = GUIConstants.DEFAULT_METER_COLOR;
   private Color backgroundWarningColor = GUIConstants.METER_WARNING_COLOR;

   private int width;
   private int height;

   private double lowerBound;
   private double upperBound;

   private int majorTickIncrement;
   private int minorTickCount;

   private final List<DialRange> dialRanges = new ArrayList<DialRange>();

   private String labelLine1 = null;
   private String labelLine2 = null;
   private NumberFormat numberFormat;

   public DefaultMeterConfig(final int datasetCount)
      {
      if (datasetCount < 1)
         {
         throw new IllegalArgumentException("Dataset count must be a positive integer");
         }

      datasetColors = new Color[datasetCount];
      }

   public int getDatasetCount()
      {
      return datasetColors.length;
      }

   public Dimension getSize()
      {
      return new Dimension(width, height);
      }

   public void setSize(final int width, final int height)
      {
      this.width = Math.max(0, width);
      this.height = Math.max(0, height);
      }

   public void setRange(final double lowerBound, final double upperBound)
      {
      this.lowerBound = lowerBound;
      this.upperBound = upperBound;
      }

   public double getLowerBound()
      {
      return lowerBound;
      }

   public double getUpperBound()
      {
      return upperBound;
      }

   public int getMajorTickIncrement()
      {
      return majorTickIncrement;
      }

   public int getMinorTickCount()
      {
      return minorTickCount;
      }

   public void setTicks(final int majorTickIncrement, final int minorTickCount)
      {
      this.majorTickIncrement = majorTickIncrement;
      this.minorTickCount = minorTickCount;
      }

   public Color getDatasetColor(final int id)
      {
      if (id >= 0 && id < datasetColors.length)
         {
         return datasetColors[id];
         }
      return null;
      }

   public void setDatasetColor(final int id, final Color color)
      {
      if (id >= 0 && id < datasetColors.length && color != null)
         {
         datasetColors[id] = color;
         }
      }

   public Color getBackgroundColor()
      {
      return backgroundColor;
      }

   public void setBackgroundColor(final Color backgroundColor)
      {
      if (backgroundColor != null)
         {
         this.backgroundColor = backgroundColor;
         }
      }

   public Color getBackgroundWarningColor()
      {
      return backgroundWarningColor;
      }

   public void setBackgroundWarningColor(final Color backgroundWarningColor)
      {
      if (backgroundWarningColor != null)
         {
         this.backgroundWarningColor = backgroundWarningColor;
         }
      }

   public List<StandardDialRange> getDialRanges()
      {
      final List<StandardDialRange> standardDialRanges = new ArrayList<StandardDialRange>();
      for (final DialRange dialRange : dialRanges)
         {
         standardDialRanges.add(dialRange.getStandardDialRange());
         }
      return standardDialRanges;
      }

   public void addDialRange(final double lowerBound, final double upperBound, final Color color)
      {
      dialRanges.add(new DialRange(lowerBound, upperBound, color));
      }

   public void clearDialRanges()
      {
      dialRanges.clear();
      }

   public String getLabelLine1()
      {
      return labelLine1;
      }

   public String getLabelLine2()
      {
      return labelLine2;
      }

   public void setLabel(final String labelLine1, final String labelLine2)
      {
      this.labelLine1 = labelLine1;
      this.labelLine2 = labelLine2;
      }

   public void setNumberFormat(final NumberFormat numberFormat)
      {
      this.numberFormat = numberFormat;
      }

   public NumberFormat getNumberFormat()
      {
      return numberFormat;
      }

   private static final class DialRange
      {
      private final double lowerBound;
      private final double upperBound;
      private final Color color;

      private DialRange(final double lowerBound, final double upperBound, final Color color)
         {
         this.lowerBound = lowerBound;
         this.upperBound = upperBound;
         this.color = color;
         }

      private StandardDialRange getStandardDialRange()
         {
         return new StandardDialRange(lowerBound,
                                      upperBound,
                                      new GradientPaint(new Point(),
                                                        color,
                                                        new Point(),
                                                        color,
                                                        false));
         }
      }
   }
