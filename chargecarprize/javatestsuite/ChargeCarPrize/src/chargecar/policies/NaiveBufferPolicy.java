/**
 * 
 */
package chargecar.policies;

import chargecar.battery.BatteryModel;
import chargecar.capacitor.CapacitorModel;
import chargecar.util.PointFeatures;
import chargecar.util.PowerFlowException;
import chargecar.util.PowerFlows;
import chargecar.util.TripFeatures;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public class NaiveBufferPolicy implements Policy
{
	private BatteryModel modelCap;
	private BatteryModel modelBatt;
	
	public NaiveBufferPolicy(){
		//no policy-state
	}
	
	@Override
	public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,	BatteryModel capacitorClone) 
	{
		modelCap = capacitorClone;
		modelBatt = batteryClone;		
	}
	
	@Override
	public PowerFlows calculatePowerFlows(PointFeatures pf) 
	{
		double watts = pf.getPowerDemand();
		int periodMS = pf.getPeriodMS();
		double min = modelCap.getMinCurrent(periodMS);
		double max = modelCap.getMaxCurrent(periodMS);
		double capWatts = watts > max ?  max : watts;		
		capWatts = capWatts < min ? min : capWatts;
		double battWatts = watts - capWatts;//battery handles whatever cap can't
		try {
			modelCap.drawCurrent(capWatts, pf);
			modelBatt.drawCurrent(battWatts, pf);			
		} catch (PowerFlowException e) {}
		return new PowerFlows(battWatts, capWatts, 0);		
	}

	@Override
	public void endTrip() 
	{
		// nothing to do here		
	}

	@Override
	public void loadState() 
	{
		// no policy-state
		
	}

	@Override
	public String getName() {
		return "Naive Buffer Policy";
	}



}
