package chargecar.battery;

import chargecar.util.PointFeatures;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public class NaiveBattery extends BatteryModel {

	public NaiveBattery(){
		this.current = 0.0;
		this.temperature = 0.0;
		this.charge = 0.0;		
		this.efficiency = 1.0;	
	}	


	@Override
	public void drawCurrent(double current, PointFeatures point) {
		this.current = current;
		//record this current as starting at the current time
		recordHistory(point);
		//after the period is up, update charge, temp, and eff.
		this.charge = charge + current * (point.getPeriodMS() / MS_PER_HOUR);
	}

	@Override
	public BatteryModel createClone() {
		BatteryModel clone = new NaiveBattery();
		clone.charge = this.charge;
		clone.current = this.current;
		clone.efficiency = this.efficiency;
		clone.temperature = this.temperature;
		clone.chargeHistory.addAll(cloneCollection(this.chargeHistory));
		clone.temperatureHistory.addAll(cloneCollection(this.temperatureHistory));
		clone.currentDrawHistory.addAll(cloneCollection(this.currentDrawHistory));
		clone.efficiencyHistory.addAll(cloneCollection(this.efficiencyHistory));
		clone.tripHistory.addAll(cloneTripCollection(this.tripHistory));
		return clone;
	}
}
