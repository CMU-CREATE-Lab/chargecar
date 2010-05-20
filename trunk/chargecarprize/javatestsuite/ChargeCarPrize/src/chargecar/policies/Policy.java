/**
 * 
 */
package chargecar.policies;

import chargecar.battery.BatteryModel;
import chargecar.util.PointFeatures;
import chargecar.util.PowerFlows;
import chargecar.util.TripFeatures;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public interface Policy {
	public String getName();
	public void loadState();
	public void endTrip();
	public PowerFlows calculatePowerFlows(PointFeatures pointFeatures);
	public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone, BatteryModel capacitorClone); 
}
