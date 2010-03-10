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
 *
 */
public class UserPolicy implements Policy {
	@Override
	public void loadState() {
		// TODO Auto-generated method stub

	}

	@Override
	public PowerFlows calculatePowerFlows(PointFeatures pf) {
		// TODO Auto-generated method stub
		return new PowerFlows(pf.getPowerDemand(),0,0);
	}

	@Override
	public void endTrip() {
		// TODO Auto-generated method stub
	}

	@Override
	public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
			CapacitorModel capacitorClone) {
		// TODO Auto-generated method stub		
	}

}
