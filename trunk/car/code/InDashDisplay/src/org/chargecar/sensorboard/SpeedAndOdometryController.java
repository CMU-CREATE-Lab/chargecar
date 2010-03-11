package org.chargecar.sensorboard;

/**
 * <p>
 * <code>SpeedAndOdometryController</code> is the MVC controller class for the {@link SpeedAndOdometryModel} and {@link SpeedAndOdometryView}.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class SpeedAndOdometryController
   {
   private final SpeedAndOdometryModel model;

   public SpeedAndOdometryController(final SpeedAndOdometryModel model)
      {
      this.model = model;
      }

   public void resetTripOdometer1()
      {
      model.resetTripOdometer1();
      }

   public void resetTripOdometer2()
      {
      model.resetTripOdometer2();
      }
   }
