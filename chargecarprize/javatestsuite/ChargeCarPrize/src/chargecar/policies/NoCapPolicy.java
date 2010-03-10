/**
 * 
 */
package chargecar.policies;

import chargecar.battery.BatteryModel;
import chargecar.capacitor.CapacitorModel;
import chargecar.util.PointFeatures;
import chargecar.util.PowerFlows;
import chargecar.util.TripFeatures;

/**
 * @author astyler
 * DO NOT EDIT
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
	public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
			CapacitorModel capacitorClone) {
		//no trip-state for nocap policy		
	}

}
