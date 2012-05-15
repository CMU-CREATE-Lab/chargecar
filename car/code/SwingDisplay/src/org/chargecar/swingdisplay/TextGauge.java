package org.chargecar.swingdisplay;

import java.awt.Color;
import java.awt.*;

//import javax.swing.*;
import org.jdesktop.layout.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.Box;

/**
 * @author Michael Len (mlen@andrew.cmu.edu) 
 */
public final class TextGauge<T> extends AbstractGauge<T> {
	public static final int TYPE_RANGE = 0;
	public static final int TYPE_RANGE_SMALL = 1;
	public static final int TYPE_AVERAGE = 2;
	private int type;

	private T data;
	private JLabel first, second, third;
	private int editable;
	public TextGauge(final int type)
	{
		this.setOpaque(false);
		if(type == TYPE_RANGE)
		{

			makeRangeGauge();
		}
		else if(type == TYPE_AVERAGE)
		{
			makeAverageRangeGauge();	
		}
		else
		{
			makeSmallRangeGauge();
		}
	}

	/**
	 * Sets the gauge's value using the string format given to the constructor.  Assumes it is being called in the GUI
	 * thread.
	 */
	public void setValue(final T s)
	{
		setValue(s, Color.black);
	}


	/**
	 * Sets the gauge's value using the string format given to the constructor.  Assumes it is being called in the GUI
	 * thread.
	 */
	public void setValue(final T s, final Color defaultColor)
	{
		data = s;
		first.setText(data.toString());
	}
	public void makeRangeGauge(){
		Dimension chartSize = new Dimension(230, 230);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		first = new JLabel();
		first.setFont(new Font("SansSerif", Font.PLAIN, 36));
		first.setMaximumSize(new Dimension(230,70));

		second = new JLabel("MILES REMAINING");
		second.setFont(new Font("SansSerif", Font.ITALIC, 20));

		first.setText("hoi world");
		first.setOpaque(false);
		second.setOpaque(false);
		first.setFocusable(false);
		second.setFocusable(false);



		this.setOpaque(false);
		//this.setMaximumSize(chartSize);
		//this.setPreferredSize(chartSize);

//		this.add(Box.createHorizontalGlue());
		this.add(first);
		this.add(second);
	}

	public void makeSmallRangeGauge(){
		Dimension chartSize = new Dimension(130, 130);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		first = new JLabel();
		first.setFont(new Font("SansSerif", Font.PLAIN, 20));
		first.setMaximumSize(new Dimension(230,70));

		second = new JLabel("MILES REMAINING");
		second.setFont(new Font("SansSerif", Font.ITALIC, 14));

		first.setText("hoi world");
		first.setOpaque(false);
		second.setOpaque(false);
		first.setFocusable(false);
		second.setFocusable(false);



		this.setOpaque(false);
		//this.setMaximumSize(chartSize);
		//this.setPreferredSize(chartSize);

//		this.add(Box.createHorizontalGlue());
		this.add(first);
		this.add(second);
	}


	public void makeAverageRangeGauge(){
		Dimension chartSize = new Dimension(80, 80);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		//this.setMaximumSize(chartSize);

		first = new JLabel();
		first.setFont(new Font("SansSerif", Font.PLAIN, 20));
		//first.setMaximumSize(new Dimension(80,60));

		second = new JLabel("AVERAGE MILES");
		third = new JLabel("PER CHARGE");
		//third.setMaximumSize(chartSize);

		second.setFont(new Font("SansSerif", Font.ITALIC, 14));
		third.setFont(new Font("SansSerif", Font.ITALIC, 14));

		first.setText("0");
		first.setOpaque(false);
		second.setOpaque(false);
		first.setFocusable(false);
		second.setFocusable(false);
		third.setFocusable(false);


      final GroupLayout layout = new GroupLayout(this);
      this.setLayout(layout);
      this.setBackground(Color.WHITE);

      layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.CENTER)
                  .add(second)
                  .add(third)
                  .add(first));
      layout.setVerticalGroup(
            layout.createSequentialGroup()
                  .add(second)
                  .add(third)
                  .add(first));

		this.setOpaque(false);
		this.setBackground(Color.blue);
	}
}
