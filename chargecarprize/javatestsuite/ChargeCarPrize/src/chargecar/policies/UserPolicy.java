/**
 * 
 */
package chargecar.policies;

import chargecar.util.PointFeatures;
import chargecar.util.PowerFlows;
import chargecar.util.TripFeatures;

/**
 * @author astyler
 *
 */
public class UserPolicy implements Policy {
	/* (non-Javadoc)
	 * @see chargecar.policies.Policy#loadState()
	 */
	@Override
	public void loadState() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see chargecar.policies.Policy#beginTrip()
	 */
	@Override
	public void beginTrip(TripFeatures tf) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see chargecar.policies.Policy#calculatePowerFlows(chargecar.util.PointFeatures)
	 */
	@Override
	public PowerFlows calculatePowerFlows(PointFeatures pf) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see chargecar.policies.Policy#endTrip()
	 */
	@Override
	public void endTrip() {
		// TODO Auto-generated method stub

	}

}
