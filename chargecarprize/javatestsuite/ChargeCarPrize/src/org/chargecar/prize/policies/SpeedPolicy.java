package org.chargecar.prize.policies;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.TripFeatures;

/**
 * DO NOT EDIT
 * 
 * An example policy that uses just the current speed of the vehicle to
 * determine how much space to leave in the capacitor. If it is under that
 * space, it trickles power into the capacitor if possible. Illustrates the
 * capacitor current limitations, the difference between +/- current, and using
 * the models to keep track of state.
 * 
 * @author Alex Styler
 */

public class SpeedPolicy implements Policy {
    private BatteryModel modelCap;
    private BatteryModel modelBatt;
    private String name = "Speed Trickle Policy";
    
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
	    BatteryModel capacitorClone) {
	modelCap = capacitorClone;
	modelBatt = batteryClone;
    }
    
    public PowerFlows calculatePowerFlows(PointFeatures pf) {
	double wattsDemanded = pf.getPowerDemand();
	int periodMS = pf.getPeriodMS();
	double speed = pf.getSpeed();
	// leave more room in cap for regen braking from higher speeds
	double targetWattHours = modelCap.getMaxWattHours() - 2.27*speed;
	double minCapPower = modelCap.getMinPower(periodMS);
	double maxCapPower = modelCap.getMaxPower(periodMS);
	double defaultTrickleRate = -4500.0;
	double capToMotorWatts = 0.0;
	double batteryToCapWatts = 0.0;
	double batteryToMotorWatts = 0.0;
	if (wattsDemanded < minCapPower) {
	    // drawing more than the cap has
	    // battery is already getting drawn, don't trickle cap
	    capToMotorWatts = minCapPower;
	    batteryToMotorWatts = wattsDemanded - capToMotorWatts;
	    batteryToCapWatts = 0;
	} else if (wattsDemanded > maxCapPower) {
	    // overflowing cap with regen power
	    // cap is full, no need to trickle.
	    capToMotorWatts = maxCapPower;
	    batteryToMotorWatts = wattsDemanded - capToMotorWatts;
	    batteryToCapWatts = 0;
	} else {
	    // capacitor can handle the demand
	    capToMotorWatts = wattsDemanded;
	    batteryToMotorWatts = 0;
	    if (modelCap.getWattHours() < targetWattHours) {
		batteryToCapWatts = defaultTrickleRate;
	    } else {
		//batteryToCapWatts = -defaultTrickleRate;
	    }
	    if (capToMotorWatts - batteryToCapWatts > maxCapPower) {
		batteryToCapWatts = capToMotorWatts - maxCapPower;
	    } else if(capToMotorWatts - batteryToCapWatts < minCapPower){
		batteryToCapWatts = capToMotorWatts - minCapPower;
	    }
	}
	
	try {
	    modelCap.drawPower(capToMotorWatts - batteryToCapWatts, pf);
	    modelBatt.drawPower(batteryToMotorWatts + batteryToCapWatts, pf);
	} catch (PowerFlowException e) {
	}
	
	return new PowerFlows(batteryToMotorWatts, capToMotorWatts,
		batteryToCapWatts);
    }
    
    public void endTrip() {
	modelCap = null;
	modelBatt = null;
    }
    
    public void loadState() {
	// nothing to do
    }
    
    public String getName() {
	return name;
    }
}
