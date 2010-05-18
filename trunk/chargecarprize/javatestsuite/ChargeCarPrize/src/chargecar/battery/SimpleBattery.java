package chargecar.battery;

import chargecar.util.PointFeatures;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public class SimpleBattery extends BatteryModel {

	public SimpleBattery(double maxCharge, double charge){
		this.current = 0.0;
		this.temperature = 0.0;				
		this.efficiency = 1.0;
		this.charge = charge;
		this.maxCharge = maxCharge;
	}	


	@Override
	public void drawCurrent(double current, PointFeatures point) {
		this.current = current;
		this.periodMS = point.getPeriodMS();
		//record this current as starting at the current time
		recordHistory(point);
		//after the period is up, update charge, temp, and eff.
		this.charge = charge + current * (periodMS / MS_PER_HOUR);
		this.charge = charge > maxCharge ? maxCharge : charge;
	}

	@Override
	public BatteryModel createClone() {
		BatteryModel clone = new SimpleBattery(this.maxCharge, this.charge);
		clone.charge = this.charge;
		clone.current = this.current;
		clone.efficiency = this.efficiency;
		clone.temperature = this.temperature;
		clone.chargeHistory.addAll(this.chargeHistory);
		clone.temperatureHistory.addAll(this.temperatureHistory);
		clone.currentDrawHistory.addAll(this.currentDrawHistory);
		clone.efficiencyHistory.addAll(this.efficiencyHistory);
		clone.periodHistory.addAll(this.periodHistory);
		return clone;
	}
}
