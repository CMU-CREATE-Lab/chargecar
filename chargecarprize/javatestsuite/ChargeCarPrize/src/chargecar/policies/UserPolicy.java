package chargecar.policies;

import chargecar.battery.BatteryModel;
import chargecar.util.PointFeatures;
import chargecar.util.PowerFlows;
import chargecar.util.TripFeatures;

/**
 * The policy to be implemented by the competitor.  The policy is constructed once,
 * loadState is called once (so stored data may be incorporated), and beginTrip and 
 * endTrip are called for each trip in the test.  For each point in a trip, calculatePowerFlows
 * is called.  The user must take the power demanded and decide where to allocate it among the
 * capacitor or battery.
 *   
 * @author You
 */
public class UserPolicy implements Policy {
	@Override
	public void loadState() {
		// TODO implement state loading from file or other source
	}

	@Override
	public PowerFlows calculatePowerFlows(PointFeatures pf) {
		// TODO implement power flow calculation, will be called almost every
		// second for a trip... this is where most of your logic will be
		return new PowerFlows(pf.getPowerDemand(),0,0);
	}

	@Override
	public void endTrip() {
		// TODO implement cleanup if necessary, another trip may start soon after
	}

	@Override
	public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,	BatteryModel capacitorClone) {
		// TODO implement the initiating/reseting policy for a new trip, given a clone of 
		// the battery and cap as a reference, can spawn additional clones with .createClone()
	}

	@Override
	public String getName() {
		// TODO return the name of your policy, e.g. Potential/Kinetic Energy Policy
		return "User Policy";
	}

}
