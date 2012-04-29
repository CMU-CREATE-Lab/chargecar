package org.chargecar.swingdisplay;

import java.awt.Color;
import java.awt.*;
import java.awt.geom.Arc2D.*;
import java.awt.geom.Arc2D;
import java.text.NumberFormat;

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
	public static final int TYPE_CHARGE = 0;
	public static final int TYPE_RPM = 1;
	public static final int TYPE_ECO = 2;
	public static final int TYPE_ECO_SMALL = 3;
	public static final int TYPE_VOLTAGE = 4;
   private final String stringFormat;
   private int type;

   DefaultValueDataset dataset;
   public ChargeGauge(final int type)
      {
		  //TODO: use 'side' to flip things left and right
		  //TODO: figure out how to make a bar instead of friggen pin

		  this.type = type;
		  DialPlot dialplot;
		  Dimension chartSize;
		  if(type == TYPE_CHARGE)
		  {
			  dialplot = getChargeDial();
			  chartSize = new Dimension(400, 400);
			  this.stringFormat = "%d";
		  }
		  else if (type == TYPE_RPM)
		  {
			  dialplot = getRPMDial();
			  chartSize = new Dimension(400, 400);
			  this.stringFormat = "%d";
		  }
		  else if (type == TYPE_ECO)
		  {
			  dialplot = getEcoDial();
			  chartSize = new Dimension(230, 230);
			  this.stringFormat = "%d";
		  }
		  else if (type == TYPE_ECO_SMALL)
		  {
			  dialplot = getEcoDial();
			  chartSize = new Dimension(110, 110);
			  this.stringFormat = "%d";
		  }
		  else
		  {
			  dialplot = getVoltageDial();
			  chartSize = new Dimension(230, 230);
			  this.stringFormat = "%f";
		  }

		  JFreeChart jfreechart = new JFreeChart(dialplot);
		  jfreechart.setBackgroundPaint(new Color(0,0,0,0));
		  ChartPanel chartpanel = new ChartPanel(jfreechart);
		  chartpanel.setOpaque(false);
		  //chartpanel.setBackground(Color.pink);
		  chartpanel.setPreferredSize(chartSize);
		  this.setOpaque(false);
		  //setBackground(Color.orange);
		  this.setMaximumSize(chartSize);

		  add(chartpanel);

		  //GradientPaint gradientpaint = new GradientPaint(new Point(), new Color(255, 255, 255), new Point(), new Color(240, 240, 240));
		  //DialBackground dialbackground = new DialBackground(gradientpaint);
		  //dialbackground.setGradientPaintTransformer(new StandardGradientPaintTransformer(GradientPaintTransformType.VERTICAL));
		  //dialplot.addLayer(dialbackground);
	  }

   /**
    * Sets the gauge's value using the string format given to the constructor.  Assumes it is being called in the GUI
    * thread.
    */
   public void setValue(final T s)
      {
		  if(type == TYPE_VOLTAGE)
			  System.out.println("voltage: vlaue = " + s);

		  if(type == TYPE_RPM)
			  System.out.println("RPM: vlaue = " + s);
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
			 dataset.setValue((Number)s);
         }
      else
         {
         }
	  }

   private DialPlot getChargeDial() {
		  DialPlot dialplot = new DialPlot();
		  dialplot.setView(0.4D, 0D, 0.62D, 0.62D);

		  dataset = new DefaultValueDataset(50D);
		  dialplot.setDataset(dataset);

		  ArcDialFrame arcdialframe = new ArcDialFrame(0D, 90D);
		  arcdialframe.setInnerRadius(0.7D);
		  arcdialframe.setOuterRadius(0.9D);
		  arcdialframe.setForegroundPaint(Color.darkGray);
		  arcdialframe.setStroke(new BasicStroke(0F));
		  dialplot.setDialFrame(arcdialframe);

		  DialBar pin = new DialBar();
		  pin.setRadius(0.78D);
		  pin.setStroke(new BasicStroke(35F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		  dialplot.addLayer(pin);

		  StandardDialScale standarddialscale =
			  new StandardDialScale(0.0D, 100D, 2D, 86D, 0D, 0);
		  standarddialscale.setTickRadius(0.9D);
		  standarddialscale.setTickLabelOffset(0.06D);
		  standarddialscale.setMajorTickIncrement(25D);
		  standarddialscale.setTickLabelFont(new Font("Dialog", 0, 14));
		  dialplot.addScale(0, standarddialscale);

		  return dialplot;
   }

   private DialPlot getRPMDial() {
		  DialPlot dialplot = new DialPlot();
		  dialplot.setView(0D, 0D, 0.62D, 0.62D);

		  dataset = new DefaultValueDataset(50D);
		  dialplot.setDataset(dataset);

		  ArcDialFrame arcdialframe = new ArcDialFrame(90, 90D);
		  arcdialframe.setInnerRadius(0.7D);
		  arcdialframe.setOuterRadius(0.9D);
		  arcdialframe.setForegroundPaint(Color.darkGray);
		  arcdialframe.setStroke(new BasicStroke(0F));
		  dialplot.setDialFrame(arcdialframe);

		  DialBar pin = new DialBar();
		  pin.setRadius(0.78D);
		  pin.setStroke(new BasicStroke(35F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		  dialplot.addLayer(pin);

		  StandardDialScale standarddialscale =
			  new StandardDialScale(-1600D, 1600D, 178D, -86D, 0D, 0);
		  standarddialscale.setTickRadius(0.9D);
		  standarddialscale.setTickLabelOffset(0.06D);
		  standarddialscale.setMajorTickIncrement(800D);
		  standarddialscale.setTickLabelFont(new Font("Dialog", 0, 14));
		  dialplot.addScale(0, standarddialscale);
		  return dialplot;
   }

   private DialPlot getEcoDial() {
		  DialPlot dialplot = new DialPlot();
		  dialplot.setView(0D, 0D, 1D, 1D);

		  dataset = new DefaultValueDataset(1D);
		  dialplot.setDataset(dataset);

		  ArcDialFrame arcdialframe = new ArcDialFrame(90, 360D);
		  arcdialframe.setInnerRadius(0.4D);
		  arcdialframe.setOuterRadius(0.8D);
		  arcdialframe.setForegroundPaint(Color.darkGray);
		  arcdialframe.setStroke(new BasicStroke(0F));
		  dialplot.setDialFrame(arcdialframe);

		  DialBar pin = new DialBar();
		  pin.setRadius(0.68D);
		  pin.setStroke(new BasicStroke(90F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		  dialplot.addLayer(pin);

		  StandardDialScale standarddialscale =
			  new StandardDialScale(0D, 5D, 90D, -360D, 0D, 0);
		  standarddialscale.setTickRadius(0.9D);
		  standarddialscale.setMajorTickLength(.5D);
		  standarddialscale.setTickLabelOffset(0.06D);
		  standarddialscale.setTickLabelsVisible(false);
		  standarddialscale.setMajorTickIncrement(1D);
		  standarddialscale.setMajorTickStroke(new BasicStroke(15F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		  standarddialscale.setTickLabelFont(new Font("Dialog", 0, 14));
		  dialplot.addScale(0, standarddialscale);
		  return dialplot;
   }


   private DialPlot getVoltageDial() {
		  DialPlot dialplot = new DialPlot();
		  dialplot.setView(0.4D, 0D, 0.62D, 0.62D);

		  dataset = new DefaultValueDataset(4D);
		  dialplot.setDataset(dataset);

		  ArcDialFrame arcdialframe = new ArcDialFrame(0D, 90D);
		  arcdialframe.setInnerRadius(0.7D);
		  arcdialframe.setOuterRadius(0.9D);
		  arcdialframe.setForegroundPaint(Color.darkGray);
		  arcdialframe.setStroke(new BasicStroke(0F));
		  dialplot.setDialFrame(arcdialframe);

		  DialPointer.Pin minPin = new DialPointer.Pin();
		  minPin.setRadius(0.78D);
		  minPin.setStroke(new BasicStroke(2F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		  dialplot.addLayer(minPin);

//		  DialBar pin = new DialBar();
//		  pin.setRadius(0.78D);
//		  pin.setStroke(new BasicStroke(35F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
//		  dialplot.addLayer(pin);
//
		  StandardDialScale standarddialscale =
			  new StandardDialScale(80D, 140D, 2D, 86D, 0D, 0);
		  standarddialscale.setTickRadius(0.9D);
		  standarddialscale.setTickLabelOffset(0.06D);
		  standarddialscale.setMajorTickIncrement(10D);
		  standarddialscale.setTickLabelFont(new Font("Dialog", 0, 14));
		  dialplot.addScale(0, standarddialscale);

		  return dialplot;
   }
}
