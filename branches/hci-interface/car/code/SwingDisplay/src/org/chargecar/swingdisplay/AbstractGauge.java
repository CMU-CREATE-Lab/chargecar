package org.chargecar.swingdisplay;

import java.awt.Color;
import javax.swing.JPanel;

abstract class AbstractGauge<T> extends JPanel{

	abstract void setValue(T val);
    abstract void setValue(final T s, final Color defaultColor);
}


