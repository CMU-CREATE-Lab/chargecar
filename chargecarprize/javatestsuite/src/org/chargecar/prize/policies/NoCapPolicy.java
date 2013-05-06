package org.chargecar.prize.policies;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripFeatures;

/**
 * DO NOT EDIT
 * 
 * A baseline policy that uses the battery for all energy storage. This serves
 * as a comparison to traditional electric vehicles.
 * 
 * @author Alex Styler
 */
public class NoCapPolicy implements Policy {
    public PowerFlows calculatePowerFlows(PointFeatures pf) {
	return new PowerFlows(pf.getPowerDemand(), 0, 0);// get all power from
	// battery
    }
    
    public void endTrip(Trip t) {
	// no trip-state
    }
    
    public void loadState() {
	// no policy-state
    }
    
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
	    BatteryModel capacitorClone) {
	// no trip-state for nocap policy
    }
    
    public String getName() {
	return "No Capacitor Policy";
    }

    @Override
    public String getShortName() {
	return "nocap";
    }

    @Override
    public void clearState() {
	// TODO Auto-generated method stub
	
    }
}
