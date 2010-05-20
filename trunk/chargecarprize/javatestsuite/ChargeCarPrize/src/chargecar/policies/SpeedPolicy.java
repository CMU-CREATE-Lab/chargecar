package chargecar.policies;

import chargecar.battery.BatteryModel;
import chargecar.util.PointFeatures;
import chargecar.util.PowerFlowException;
import chargecar.util.PowerFlows;
import chargecar.util.TripFeatures;

public class SpeedPolicy implements Policy {
	private BatteryModel modelCap;
	private BatteryModel modelBatt;
	private String name = "Speed Trickle Policy";
	
	@Override
	public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone, BatteryModel capacitorClone) {
		modelCap = capacitorClone;
		modelBatt = batteryClone;		
	}
	
	@Override
	public PowerFlows calculatePowerFlows(PointFeatures pf) {
		// TODO implement power flow calculation, will be called almost every two
		// seconds for a trip... this is where most of your logic will be
		double watts = pf.getPowerDemand();
		int periodMS = pf.getPeriodMS();
		double speed = pf.getSpeed();		
		double targetCharge = modelCap.getMaxCharge() - 1.7*speed;
		double min = modelCap.getMinCurrent(periodMS);
		double max = modelCap.getMaxCurrent(periodMS);		
		double rate = -5;
		double capToMotorWatts = 0.0;
		double batteryToCapWatts = 0.0;
		double batteryToMotorWatts = 0.0;
		if(watts < min){
			//drawing more than the cap has
			capToMotorWatts = min;
			batteryToMotorWatts = watts - capToMotorWatts;
			batteryToCapWatts = 0;
		}
		else if(watts > max){
			//overflowing cap
			capToMotorWatts = max;
			batteryToMotorWatts = watts-capToMotorWatts;
			batteryToCapWatts = 0;
		}
		else{			
			capToMotorWatts = watts;
			batteryToMotorWatts = 0;
			if(modelCap.getCharge() < targetCharge){
				batteryToCapWatts = rate;
			}
			if(capToMotorWatts - batteryToCapWatts > max){
				batteryToCapWatts = max - capToMotorWatts;				
			}			
		}  
		
		try {
			modelCap.drawCurrent(capToMotorWatts - batteryToCapWatts, pf);
			modelBatt.drawCurrent(batteryToMotorWatts + batteryToCapWatts, pf);
		} catch (PowerFlowException e) {}
		
		return new PowerFlows(batteryToMotorWatts, capToMotorWatts, batteryToCapWatts);
	}

	@Override
	public void endTrip() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadState() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return name;
	}
}
