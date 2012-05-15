package org.chargecar.swingdisplay.views;
import org.chargecar.swingdisplay.ChargeGauge;

import java.awt.Color;
import java.awt.*;
import java.awt.geom.Arc2D.*;
import java.awt.geom.Arc2D;
import java.text.NumberFormat;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import javax.swing.*;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.jdesktop.layout.GroupLayout;

/**
 * @author Michael Len (mlen@andrew.cmu.edu) 
 */
public final class HistoryView {

	IntervalXYDataset data;
	public HistoryView() {

		TimeSeries series = new TimeSeries("History Data");
		series.add(new Minute(1,1,1,1,2111), 41);
		series.add(new Minute(2,1,1,1,2111), 51);
		series.add(new Minute(3,1,1,1,2111), 71);
		series.add(new Minute(4,1,1,1,2111), 61);
		series.add(new Minute(5,1,1,1,2111), 71);
		series.add(new Minute(6,1,1,1,2111), 51);
		series.add(new Minute(7,1,1,1,2111), 31);
		series.add(new Minute(8,1,1,1,2111), 41);
		series.add(new Minute(9,1,1,1,2111), 51);
		series.add(new Minute(10,1,1,1,2111), 71);
		series.add(new Minute(11,1,1,1,2111), 61);
		series.add(new Minute(12,1,1,1,2111), 71);
		series.add(new Minute(13,1,1,1,2111), 51);
		series.add(new Minute(14,1,1,1,2111), 31);


		data = new TimeSeriesCollection(series);
	}

	public JPanel getHistoryGraph() {
		final XYItemRenderer renderer1 = new XYBarRenderer(0.20);
		final DateAxis domainAxis = new DateAxis();
		final ValueAxis rangeAxis = new NumberAxis();
		final XYPlot plot = new XYPlot(data, domainAxis, rangeAxis, renderer1);
		plot.setFixedLegendItems(null);

		JFreeChart jfreechart = new JFreeChart(plot);
		jfreechart.setBackgroundPaint(null);

		ChartPanel chartpanel = new ChartPanel(jfreechart);
		chartpanel.setOpaque(false);

		
		Dimension chartSize = new Dimension(200,200);
		chartpanel.setBackground(ChargeGauge.backgroundColor);
		chartpanel.setPreferredSize(chartSize);
		chartpanel.setMaximumSize(chartSize);
		//setBackground(Color.orange);
		//chartpanel.setMaximumSize(chartSize);

		return chartpanel;
	}
	public JPanel getHighScores() {

		JPanel ret = new JPanel();
		ret.setOpaque(false);
		ret.setLayout(new BoxLayout(ret, BoxLayout.Y_AXIS));
		Dimension chartSize = new Dimension(200,200);
		ret.setPreferredSize(chartSize);
		ret.setMaximumSize(chartSize);

		ret.add(new JLabel("High Scores:"));
		ret.add(new JLabel("1: 50 MPC Mike"));
		ret.add(new JLabel("2: 48 MPC Dan"));
		ret.add(new JLabel("3: 44 MPC Ashley"));

		return ret;
	}
}
