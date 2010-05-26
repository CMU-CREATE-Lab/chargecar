package chargecar.policies;

import chargecar.battery.BatteryModel;
import chargecar.util.PointFeatures;
import chargecar.util.PowerFlowException;
import chargecar.util.PowerFlows;
import chargecar.util.TripFeatures;

/**
 * DO NOT EDIT
 *
 * An example policy that uses just the current speed of the vehicle to determine how 
 * much space to leave in the capacitor.  If it is under that space, it trickles power
 * into the capacitor if possible.  Illustrates the capacitor current limitations, the
 * difference between +/- current, and using the models to keep track of state.
 *
 * @author Alex Styler
 */

public class SpeedPolicy implements Policy
   {
   private BatteryModel modelCap;
   private BatteryModel modelBatt;
   private String name = "Speed Trickle Policy";

   public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone, BatteryModel capacitorClone)
      {
      modelCap = capacitorClone;
      modelBatt = batteryClone;
      }

   public PowerFlows calculatePowerFlows(PointFeatures pf)
      {
      double wattsDemanded = pf.getPowerDemand();
      int periodMS = pf.getPeriodMS();
      double speed = pf.getSpeed();
      double targetCharge = modelCap.getMaxCharge() - 1.7 * speed;
      double minCapCurrent = modelCap.getMinCurrent(periodMS);
      double maxCapCurrent = modelCap.getMaxCurrent(periodMS);
      double defaultTrickleRate = -5;
      double capToMotorWatts = 0.0;
      double batteryToCapWatts = 0.0;
      double batteryToMotorWatts = 0.0;
      if (wattsDemanded < minCapCurrent)
         {
         //drawing more than the cap has
         capToMotorWatts = minCapCurrent;
         batteryToMotorWatts = wattsDemanded - capToMotorWatts;
         batteryToCapWatts = 0;
         }
      else if (wattsDemanded > maxCapCurrent)
         {
         //overflowing cap with regen power
         capToMotorWatts = maxCapCurrent;
         batteryToMotorWatts = wattsDemanded - capToMotorWatts;
         batteryToCapWatts = 0;
         }
      else
         {
         //capacitor can handle the demand
         capToMotorWatts = wattsDemanded;
         batteryToMotorWatts = 0;
         if (modelCap.getCharge() < targetCharge)
            {
            batteryToCapWatts = defaultTrickleRate;
            }
         if (capToMotorWatts - batteryToCapWatts > maxCapCurrent)
            {
            batteryToCapWatts = maxCapCurrent - capToMotorWatts;
            }
         }

      try
         {
         modelCap.drawCurrent(capToMotorWatts - batteryToCapWatts, pf);
         modelBatt.drawCurrent(batteryToMotorWatts + batteryToCapWatts, pf);
         }
      catch (PowerFlowException e)
         {
         }

      return new PowerFlows(batteryToMotorWatts, capToMotorWatts, batteryToCapWatts);
      }

   public void endTrip()
      {
      modelCap = null;
      modelBatt = null;
      }

   public void loadState()
      {
      // nothing to do

      }

   public String getName()
      {
      return name;
      }
   }
