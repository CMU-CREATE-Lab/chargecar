/**
 * 
 */
package chargecar.policies;

import chargecar.battery.BatteryModel;
import chargecar.util.PointFeatures;
import chargecar.util.PowerFlowException;
import chargecar.util.PowerFlows;
import chargecar.util.TripFeatures;

/**
 * DO NOT EDIT
 * 
 * A simple buffer policy that puts regen into the cap until it is full,
 * or takes energy from the cap until it is empty.  Overflow is handled 
 * by the battery.  Does not utilize Battery<->Capacitor flow degree of
 * freedom.
 * 
 * @author Alex Styler
 */
public class NaiveBufferPolicy implements Policy
{
	private BatteryModel modelCap;
	private BatteryModel modelBatt;
	private String name = "Naive Buffer Policy";
	
	public NaiveBufferPolicy(){
		//no policy-wide-state, only per-trip state
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
		double wattsDemanded = pf.getPowerDemand();
		int periodMS = pf.getPeriodMS();		
		
		//get min and max currents capacitor can handle based on charge-state and max charge
		double min = modelCap.getMinCurrent(periodMS);
		double max = modelCap.getMaxCurrent(periodMS);
		
		//limit watts from capacitor based on min/max
		double wattsFromCapacitor = wattsDemanded > max ?  max : wattsDemanded;		
		wattsFromCapacitor = wattsFromCapacitor < min ? min : wattsFromCapacitor;
		
		//battery handles whatever cap can't
		double wattsFromBattery = wattsDemanded - wattsFromCapacitor;
		
		//record what we're sending to the car with our local models
		try {
			modelCap.drawCurrent(wattsFromCapacitor, pf);
			modelBatt.drawCurrent(wattsFromBattery, pf);			
		} catch (PowerFlowException e) {}
		
		//send commands to car
		return new PowerFlows(wattsFromBattery, wattsFromCapacitor, 0);		
	}

	@Override
	public void endTrip() 
	{
		//clear any trip-wide-state
		modelCap = null;
		modelBatt = null;
	}

	@Override
	public void loadState() 
	{
		// no policy-state file to load
	}

	@Override
	public String getName() {
		return name;
	}



}
