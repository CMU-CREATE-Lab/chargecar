package org.chargecar.swingdisplay;

import java.awt.Color;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.plot.dial.*;
public class ChargeDialFrame extends ArcDialFrame {

	Paint paint;
	public ChargeDialFrame(double a, double b){
		super(a, b);
		this.paint = Color.black;
	}
	public void draw(Graphics2D g2, DialPlot plot, Rectangle2D frame,
			Rectangle2D view) {
		Shape window = getWindow(frame);
		Shape outerWindow = getOuterWindow(frame);

		Area area1 = new Area(outerWindow);
		Area area2 = new Area(window);
		area1.subtract(area2);
		g2.setStroke(new BasicStroke(1.0f));
		g2.setPaint(this.paint);
		g2.draw(area2);

		//g2.setPaint(Color.green);
		//g2.draw(window);
		//g2.draw(outerWindow);
	}
	public void setForegroundPaint(Paint paint) {

		this.paint = paint;
	}

}
