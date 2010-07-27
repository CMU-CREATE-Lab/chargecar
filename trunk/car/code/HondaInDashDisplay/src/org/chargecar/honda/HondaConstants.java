package org.chargecar.honda;

import java.awt.Color;

/**
 * <p>
 * <code>HondaConstants</code> defines various constact for the Honda.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class HondaConstants
   {
   public static final int NUM_BATTERIES = 33;

   public static final String UNKNOWN_VALUE = "?";
   public static final String USE_FAKE_DEVICES_SYSTEM_PROPERTY_KEY = "use-fake-devices";
   public static final Color GREEN = new Color(0, 170, 0);
   public static final Color RED = new Color(170, 0, 0);

   private HondaConstants()
      {
      // private to prevent instantiation
      }
   }
