package chargecar.capacitor;

import chargecar.util.PointFeatures;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public class SimpleCapacitor extends CapacitorModel {

	public SimpleCapacitor(double maxCharge){
		this.maxCharge = maxCharge;
		this.charge = 0;
		this.temperature = 0;
		this.efficiency = 1.0;
	}
	@Override
	public double getMaxCurrent(double periodMS) {
		return (this.maxCharge - this.charge) / (periodMS / MS_PER_HOUR);
		//100% efficient, max current is the maximum positive current that
		//would fill the capacitor to maximum over the given period
	}

	@Override
	public double getMinCurrent(double periodMS) {
		return (-1.0) * this.charge / (periodMS / MS_PER_HOUR);
		//100% efficient, min current is maximum negative current that 
		//would empty the current charge over the given period
	}

	@Override
	public void drawCurrent(double current, PointFeatures point) {
		this.current = current;
		//record this current as starting at the current time
		recordHistory(point);
		//after the period is up, update charge, temp, and eff.
		this.charge = this.charge + current * (point.getPeriodMS() / MS_PER_HOUR);
		//temp and eff don't update in naive model
	}

	@Override
	public CapacitorModel createClone() {
		SimpleCapacitor clone = new SimpleCapacitor(this.maxCharge);
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
