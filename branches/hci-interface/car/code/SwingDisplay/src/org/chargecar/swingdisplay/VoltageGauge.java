package org.chargecar.swingdisplay;

import java.awt.Color;
import java.awt.*;

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
public final class VoltageGauge extends JPanel
   {
   private final String stringFormat;

   DefaultValueDataset minData;
   DefaultValueDataset maxData;
   DefaultValueDataset avgData;

   private final static int DATA_MIN = 0;
   private final static int DATA_MAX = 1;
   private final static int DATA_AVG = 2;

   public VoltageGauge(final int side)
      {
		  //TODO: use 'side' to flip things left and right
		  //TODO: figure out how to make a bar instead of friggen pin

		  DialPlot dialplot;
		  Dimension chartSize;

		  dialplot = getCurrentDial();

		  chartSize = new Dimension(200, 200);

		  this.stringFormat = "%f"; JFreeChart jfreechart = new JFreeChart(dialplot);
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
   public void setVoltageValues(final double min, final double max, final double avg) 
      {
	//	  if (min != null)
	//	  {
	//		  //minData.setValue(Double.parseDouble(String.format(stringFormat, min)));
			  minData.setValue(min);
	//	  }

	//	  if (max != null)
	//	  {
			  maxData.setValue(max);
	//	  }

	//	  if (avg != null)
	//	  {
			  avgData.setValue(avg);
	//	  }
	  revalidate();
	  }

   private DialPlot getCurrentDial() {
		  DialPlot dialplot = new DialPlot();
		  dialplot.setView(0D, 0D, 0.62D, 0.62D);

		  minData = new DefaultValueDataset(3.3D);
		  maxData = new DefaultValueDataset(3.0D);
		  avgData = new DefaultValueDataset(2D);
		  dialplot.setDataset(DATA_MIN, minData);
		  dialplot.setDataset(DATA_MAX, maxData);
		  dialplot.setDataset(DATA_AVG, avgData);

		  ArcDialFrame arcdialframe = new ArcDialFrame(90D, 90D);
		  arcdialframe.setInnerRadius(0.7D);
		  arcdialframe.setOuterRadius(0.9D);
		  arcdialframe.setForegroundPaint(Color.darkGray);
		  arcdialframe.setStroke(new BasicStroke(0F));
		  dialplot.setDialFrame(arcdialframe);

		  DialPointer.Pin minPin = new DialPointer.Pin(DATA_MIN);
		  minPin.setRadius(0.78D);
		  minPin.setStroke(new BasicStroke(2F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		  dialplot.addLayer(minPin);

		  DialPointer.Pin maxPin = new DialPointer.Pin(DATA_MAX);
		  maxPin.setRadius(0.78D);
		  maxPin.setStroke(new BasicStroke(2F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		  dialplot.addLayer(maxPin);
		  

		  DialPointer.Pin avgPin = new DialPointer.Pin(DATA_AVG);
		  avgPin.setRadius(0.78D);
		  avgPin.setStroke(new BasicStroke(2F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		  dialplot.addLayer(avgPin);

//		  DialBar pin = new DialBar();
//		  pin.setRadius(0.78D);
//		  pin.setStroke(new BasicStroke(35F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
//		  dialplot.addLayer(pin);
//
		  StandardDialScale standarddialscale =
			  new StandardDialScale(2.0D, 5D, 178D, -86D, 0D, 0);
		  standarddialscale.setTickRadius(0.9D);
		  standarddialscale.setTickLabelOffset(0.06D);
		  standarddialscale.setMajorTickIncrement(.5D);
		  standarddialscale.setTickLabelFont(new Font("Dialog", 0, 14));
		  dialplot.addScale(0, standarddialscale);

		  return dialplot;
   }
}
