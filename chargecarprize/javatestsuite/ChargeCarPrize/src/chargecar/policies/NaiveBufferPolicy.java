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
	
	public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,	BatteryModel capacitorClone) 
	{
		modelCap = capacitorClone;
		modelBatt = batteryClone;		
	}
	
	public PowerFlows calculatePowerFlows(PointFeatures pf) 
	{
		double wattsDemanded = pf.getPowerDemand();
		int periodMS = pf.getPeriodMS();		
		
		//get min and max currents capacitor can handle based on charge-state and max charge
		double min = modelCap.getMinCurrent(periodMS);
		double max = modelCap.getMaxCurrent(periodMS);
		
		//limit watts from capacitor based on min/max
		double capToMotorWatts = wattsDemanded > max ?  max : wattsDemanded;		
		capToMotorWatts = capToMotorWatts < min ? min : capToMotorWatts;
		
		//battery handles whatever cap can't
		double batteryToMotorWatts = wattsDemanded - capToMotorWatts;
		if(batteryToMotorWatts > 0)
			batteryToMotorWatts = 0;//dont regen into battery to avoid bad i2
		//record what we're sending to the car with our local models
		try {
			modelCap.drawCurrent(capToMotorWatts, pf);
			modelBatt.drawCurrent(batteryToMotorWatts, pf);			
		} catch (PowerFlowException e) {}
		
		//send commands to car
		return new PowerFlows(batteryToMotorWatts, capToMotorWatts, 0);		
	}

	public void endTrip() 
	{
		//clear any trip-wide-state
		modelCap = null;
		modelBatt = null;
	}

	public void loadState() 
	{
		// no policy-state file to load
	}

	public String getName() {
		return name;
	}



}
