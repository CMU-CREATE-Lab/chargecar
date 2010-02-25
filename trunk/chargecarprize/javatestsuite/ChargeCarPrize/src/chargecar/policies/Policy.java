/**
 * 
 */
package chargecar.policies;

import chargecar.util.PointFeatures;
import chargecar.util.PowerFlows;
import chargecar.util.TripFeatures;

/**
 * @author astyler
 * DO NOT EDIT
 */
public interface Policy {
	public void loadState();
	public void beginTrip(TripFeatures tf);
	public void endTrip();
	public PowerFlows calculatePowerFlows(PointFeatures pf); 
}
