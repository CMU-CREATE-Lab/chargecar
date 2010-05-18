package chargecar.policies;

import chargecar.battery.BatteryModel;
import chargecar.capacitor.CapacitorModel;
import chargecar.util.PointFeatures;
import chargecar.util.PowerFlows;
import chargecar.util.TripFeatures;

/**
 * @author You
 *
 */
public class UserPolicy implements Policy {
	@Override
	public void loadState() {
		// TODO implement state loading from file or other source
	}

	@Override
	public PowerFlows calculatePowerFlows(PointFeatures pf) {
		// TODO implement power flow calculation, will be called almost every two
		// seconds for a trip... this is where most of your logic will be
		return new PowerFlows(pf.getPowerDemand(),0,0);
	}

	@Override
	public void endTrip() {
		// TODO implement cleanup if necessary.
	}

	@Override
	public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,	BatteryModel capacitorClone) {
		// TODO implement the initiating/reseting policy for a new trip, given a clone of 
		// the battery and cap as a reference, can spawn additional clones with .createClone()
	}

}
