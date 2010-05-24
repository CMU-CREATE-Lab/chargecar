package chargecar.policies;

import chargecar.battery.BatteryModel;
import chargecar.util.PointFeatures;
import chargecar.util.PowerFlows;
import chargecar.util.TripFeatures;

/**
 * DO NOT EDIT
 * 
 * A baseline policy that uses the battery for all energy storage.
 * This serves as a comparison to traditional electric vehicles.
 * 
 * @author Alex Styler 
 */
public class NoCapPolicy implements Policy {
	@Override
	public PowerFlows calculatePowerFlows(PointFeatures pf) {
		return new PowerFlows(pf.getPowerDemand(),0,0);//get all power from battery
	}

	@Override
	public void endTrip() {
		//no trip-state		
	}

	@Override
	public void loadState() {
		//no policy-state		
	}

	@Override
	public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,	BatteryModel capacitorClone) {
		//no trip-state for nocap policy		
	}

	@Override
	public String getName() {
		return "No Capacitor Policy";
	}

}
