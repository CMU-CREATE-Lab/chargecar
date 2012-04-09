package org.chargecar.honda;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.HashUtilities;
import org.jfree.io.SerialUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.*;

    /**
     * A dial pointer that draws a thin line (like a pin).
     */
    public class DialBar extends DialPointer {

        /** For serialization. */
        static final long serialVersionUID = -8445860485367689757L;

        /** The paint. */
        private transient Paint paint;

        private transient Stroke stroke;
        /**
         * Creates a new instance.
         */
        public DialBar() {
            this(0);
        }

        /**
         * Creates a new instance.
         *
         * @param datasetIndex  the dataset index.
         */
        public DialBar(int datasetIndex) {
            super(datasetIndex);
            this.paint = Color.red;

            this.stroke = new BasicStroke(20.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL);
        }

        /**
         * Returns the paint.
         *
         * @return The paint (never <code>null</code>).
         *
         * @see #setPaint(Paint)
         */
        public Paint getPaint() {
            return this.paint;
        }

        /**
         * Sets the paint and sends a {@link DialLayerChangeEvent} to all
         * registered listeners.
         *
         * @param paint  the paint (<code>null</code> not permitted).
         *
         * @see #getPaint()
         */
        public void setPaint(Paint paint) {
            if (paint == null) {
                throw new IllegalArgumentException("Null 'paint' argument.");
            }
            this.paint = paint;
            notifyListeners(new DialLayerChangeEvent(this));
        }

        /**
         * Returns the stroke.
         *
         * @return The stroke (never <code>null</code>).
         *
         * @see #setStroke(Stroke)
         */
        public Stroke getStroke() {
            return this.stroke;
        }

        /**
         * Sets the stroke and sends a {@link DialLayerChangeEvent} to all
         * registered listeners.
         *
         * @param stroke  the stroke (<code>null</code> not permitted).
         *
         * @see #getStroke()
         */
        public void setStroke(Stroke stroke) {
            if (stroke == null) {
                throw new IllegalArgumentException("Null 'stroke' argument.");
            }
            this.stroke = stroke;
            notifyListeners(new DialLayerChangeEvent(this));
        }

        /**
         * Draws the pointer.
         *
         * @param g2  the graphics target.
         * @param plot  the plot.
         * @param frame  the dial's reference frame.
         * @param view  the dial's view.
         */
        public void draw(Graphics2D g2, DialPlot plot, Rectangle2D frame,
            Rectangle2D view) {

            g2.setPaint(this.paint);
            g2.setStroke(this.stroke);

            Rectangle2D arcRect = DialPlot.rectangleByRadius(frame,
                   getRadius(), getRadius());

            double value = plot.getValue(getDatasetIndex());
            //DialScale scale = plot.getScaleForDataset(getDatasetIndex());
            DialScale scale = plot.getScale(getDatasetIndex());
			//Rectangle2D arcRect = plot.getDialFrame().getWindow(frame).getBounds2D();
            double angle = scale.valueToAngle(value);
            double origin = scale.valueToAngle(0);

            Arc2D arc = new Arc2D.Double(arcRect, origin, angle - origin, Arc2D.OPEN);
            Point2D pt = arc.getEndPoint();

            Line2D line = new Line2D.Double(frame.getCenterX(),
                    frame.getCenterY(), pt.getX(), pt.getY());
            //g2.fill(arc);
            g2.draw(arc);
        }

        /**
         * Tests this pointer for equality with an arbitrary object.
         *
         * @param obj  the object (<code>null</code> permitted).
         *
         * @return A boolean.
         */
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof DialBar)) {
                return false;
            }
            DialBar that = (DialBar) obj;
            if (!PaintUtilities.equal(this.paint, that.paint)) {
                return false;
            }
            return super.equals(obj);
        }

        /**
         * Returns a hash code for this instance.
         *
         * @return A hash code.
         */
        public int hashCode() {
            int result = super.hashCode();
            result = HashUtilities.hashCode(result, this.paint);
            return result;
        }

        /**
         * Provides serialization support.
         *
         * @param stream  the output stream.
         *
         * @throws IOException  if there is an I/O error.
         */
        private void writeObject(ObjectOutputStream stream) throws IOException {
            stream.defaultWriteObject();
            SerialUtilities.writePaint(this.paint, stream);
        }

        /**
         * Provides serialization support.
         *
         * @param stream  the input stream.
         *
         * @throws IOException  if there is an I/O error.
         * @throws ClassNotFoundException  if there is a classpath problem.
         */
        private void readObject(ObjectInputStream stream)
                throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            this.paint = SerialUtilities.readPaint(stream);
        }

    }
