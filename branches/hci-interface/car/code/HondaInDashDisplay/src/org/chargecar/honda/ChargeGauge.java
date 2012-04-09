package org.chargecar.honda;

import java.awt.Color;
import java.awt.*;
import java.awt.geom.Arc2D.*;
import java.awt.geom.Arc2D;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.dial.*;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;
import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.JPanel;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.jdesktop.layout.GroupLayout;

/**
 * @author Michael Len (mlen@andrew.cmu.edu) 
 */
public final class ChargeGauge<T> extends JPanel
   {
   private final JLabel value;

   private final String stringFormat;

   DefaultValueDataset dataset;
   public ChargeGauge(final String side, final String stringFormat)
      {
		  //TODO: use 'side' to flip things left and right
		  //TODO: use anther paramter to determine what to monitor
		  //TODO: figure out how to make a bar instead of friggen pin

		  value = new JLabel("hi");
		  this.stringFormat = "%d";
		  dataset = new DefaultValueDataset(50D);
		  DialPlot dialplot = new DialPlot();
		  dialplot.setView(0.40000000000000003D, 0D, 0.62D, 0.62000000000000001D);
		  dialplot.setDataset(dataset);
		  ArcDialFrame arcdialframe = new ArcDialFrame(0D, 90D);
		  arcdialframe.setInnerRadius(0.69999999999999996D);
		  arcdialframe.setOuterRadius(0.90000000000000002D);
		  arcdialframe.setForegroundPaint(Color.darkGray);
		  arcdialframe.setStroke(new BasicStroke(0F));
		  dialplot.setDialFrame(arcdialframe);
		  GradientPaint gradientpaint = new GradientPaint(new Point(), new Color(255, 255, 255), new Point(), new Color(240, 240, 240));
		  DialBackground dialbackground = new DialBackground(gradientpaint);
		  dialbackground.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.VERTICAL));
		  dialplot.addLayer(dialbackground);
		  StandardDialScale standarddialscale = new StandardDialScale(0.0D, 100D, 2D, 86D, 0D, 0);
		  standarddialscale.setTickRadius(0.9D);
		  standarddialscale.setTickLabelOffset(0.060000000000000001D);
		  standarddialscale.setMajorTickIncrement(25D);
		  standarddialscale.setTickLabelFont(new Font("Dialog", 0, 14));
		  dialplot.addScale(0, standarddialscale);

		  DialBar pin = new DialBar();

		  pin.setRadius(0.78D);
		  pin.setStroke(new BasicStroke(35F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		  dialplot.addLayer(pin);

		  JFreeChart jfreechart = new JFreeChart(dialplot);
		  ChartPanel chartpanel = new ChartPanel(jfreechart);
		  chartpanel.setPreferredSize(new Dimension(400, 350));
		  
		  add(chartpanel);
	  }

   /**
    * Sets the gauge's value using the string format given to the constructor.  Assumes it is being called in the GUI
    * thread.
    */
   public void setValue(final T s)
      {
      setValue(s, Color.BLACK);
      }


   /**
    * Sets the gauge's value using the string format given to the constructor.  Assumes it is being called in the GUI
    * thread.
    */
   public void setValue(final T s, final Color defaultColor)
      {
      if (s != null)
         {
			 dataset.setValue(Integer.parseInt(String.format(stringFormat, s)));
         }
      else
         {
         }
      }
   }
