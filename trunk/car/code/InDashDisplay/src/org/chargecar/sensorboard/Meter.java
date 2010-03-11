package org.chargecar.sensorboard;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Point;
import javax.swing.JPanel;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.DialValueIndicator;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialRange;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;

public class Meter extends JPanel
   {
   private final DefaultValueDataset[] datasets;
   private final ChartPanel chartPanel;
   private final GradientPaint gradientPaint;
   private final GradientPaint warningGradientPaint;
   private final DialBackground dialBackground;

   public Meter(final MeterConfig meterConfig)
      {
      this(meterConfig, 0);
      }

   public Meter(final MeterConfig meterConfig, final double initialValue)
      {
      this.setBackground(Color.WHITE);

      this.datasets = new DefaultValueDataset[meterConfig.getDatasetCount()];
      for (int i = 0; i < datasets.length; i++)
         {
         datasets[i] = new DefaultValueDataset(initialValue);
         }

      final DialPlot plot = new DialPlot();
      plot.setView(0.0, 0.0, 1.0, 1.0);
      for (int i = 0; i < datasets.length; i++)
         {
         plot.setDataset(i, datasets[i]);
         }

      final StandardDialFrame dialFrame = new StandardDialFrame();
      dialFrame.setBackgroundPaint(Color.lightGray);
      dialFrame.setForegroundPaint(Color.darkGray);
      plot.setDialFrame(dialFrame);

      gradientPaint = new GradientPaint(new Point(),
                                        new Color(255, 255, 255),
                                        new Point(),
                                        meterConfig.getBackgroundColor());
      warningGradientPaint = new GradientPaint(new Point(),
                                               new Color(255, 255, 255),
                                               new Point(),
                                               meterConfig.getBackgroundWarningColor());

      dialBackground = new DialBackground(gradientPaint);
      dialBackground.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.VERTICAL));
      plot.setBackground(dialBackground);

      if (meterConfig.getLabelLine1() != null)
         {
         final DialTextAnnotation annotation1 = new DialTextAnnotation(meterConfig.getLabelLine1());
         annotation1.setFont(new Font("Dialog", Font.BOLD, 12));
         annotation1.setRadius(0.60);
         annotation1.setAngle(-90);
         plot.addLayer(annotation1);
         }

      if (meterConfig.getLabelLine2() != null)
         {
         final DialTextAnnotation annotation2 = new DialTextAnnotation(meterConfig.getLabelLine2());
         annotation2.setFont(new Font("Dialog", Font.BOLD, 12));
         annotation2.setRadius(0.73);
         annotation2.setAngle(-90);
         plot.addLayer(annotation2);
         }

      final DialValueIndicator dialValueIndicator = new DialValueIndicator(0);
      dialValueIndicator.setFont(new Font("Dialog", Font.PLAIN, 12));
      dialValueIndicator.setOutlinePaint(meterConfig.getDatasetColor(0));
      dialValueIndicator.setRadius(0.45);
      dialValueIndicator.setAngle(-90);
      dialValueIndicator.setNumberFormat(meterConfig.getNumberFormat());
      plot.addLayer(dialValueIndicator);

      final StandardDialScale scale = new StandardDialScale(meterConfig.getLowerBound(),
                                                            meterConfig.getUpperBound(),
                                                            -120,
                                                            -300,
                                                            meterConfig.getMajorTickIncrement(),
                                                            meterConfig.getMinorTickCount());

      scale.setTickRadius(0.88);
      scale.setTickLabelOffset(0.2);
      scale.setTickLabelFont(new Font("Dialog", Font.PLAIN, 13));
      plot.addScale(0, scale);

      for (final StandardDialRange dialRange : meterConfig.getDialRanges())
         {
         dialRange.setInnerRadius(.89);
         dialRange.setOuterRadius(.90);
         plot.addLayer(dialRange);
         }

      final DialPointer.Pin needle = new DialPointer.Pin(0);
      needle.setRadius(.88);
      plot.addLayer(needle);

      // add the needles for the other datasets, if any
      for (int i = 1; i < datasets.length; i++)
         {
         final DialPointer.Pin datasetNeedle = new DialPointer.Pin(i);
         datasetNeedle.setRadius(0.75);
         datasetNeedle.setPaint(meterConfig.getDatasetColor(i));
         plot.addLayer(datasetNeedle);
         }

      final DialCap dialCap = new DialCap();
      dialCap.setRadius(0.10);
      plot.setCap(dialCap);

      chartPanel = new ChartPanel(new JFreeChart(plot));
      chartPanel.setPreferredSize(meterConfig.getSize());

      this.add(chartPanel);
      this.setPreferredSize(meterConfig.getSize());
      }

   public void addChartMouseListener(final ChartMouseListener listener)
      {
      if (listener != null)
         {
         chartPanel.addChartMouseListener(listener);
         }
      }

   public void setValues(final Double... values)
      {
      if (values != null)
         {
         for (int i = 0; i < Math.min(values.length, datasets.length); i++)
            {
            if (values[i] == null)
               {
               // todo
               }
            else
               {
               datasets[i].setValue(values[i]);
               }
            }
         }
      else
         {
         // todo
         }
      }

   public void setIsWarningMode(final boolean isInWarningMode)
      {
      dialBackground.setPaint(isInWarningMode ? warningGradientPaint : gradientPaint);
      }
   }