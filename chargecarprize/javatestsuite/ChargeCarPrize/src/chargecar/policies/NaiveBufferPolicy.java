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
public class NaiveBufferPolicy implements Policy{
	private CapacitorModel modelCap;
	private BatteryModel modelBatt;
	
	public NaiveBufferPolicy(){
		//no policy-state
	}
	
	@Override
	public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
			CapacitorModel capacitorClone) {
		modelCap = capacitorClone;
		modelBatt = batteryClone;		
	}
	
	@Override
	public PowerFlows calculatePowerFlows(PointFeatures pf) {
		double watts = pf.getPowerDemand();
		double period = pf.getPeriod();
		double min = modelCap.getMinCurrent(period);
		double max = modelCap.getMaxCurrent(period);
		double capWatts = watts > max ?  max : watts;		
		capWatts = capWatts < min ? min : capWatts;
		double battWatts = watts - capWatts;//battery handles whatever cap can't
		modelCap.drawCurrent(capWatts, period);
		modelBatt.drawCurrent(battWatts, period);
		return new PowerFlows(battWatts, capWatts, 0);
	}

	@Override
	public void endTrip() {
		// nothing to do here		
	}

	@Override
	public void loadState() {
		// no policy-state
		
	}



}
