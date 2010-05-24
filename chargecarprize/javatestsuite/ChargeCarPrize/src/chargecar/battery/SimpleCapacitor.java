package chargecar.battery;

import chargecar.util.PointFeatures;
import chargecar.util.PowerFlowException;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public class SimpleCapacitor extends BatteryModel {

	public SimpleCapacitor(double maxCharge, double charge){
		this.maxCharge = maxCharge;
		this.charge = charge;
		this.temperature = 0.0;
		this.current = 0.0;
		this.efficiency = 1.0;
	}

	@Override
	public void drawCurrent(double current, PointFeatures point) throws PowerFlowException {
		this.current = current;
		//record this current as starting at the current time
		recordHistory(point);
		//after the period is up, update charge, temp, and eff.
		this.charge = this.charge + current * (point.getPeriodMS() / MS_PER_HOUR);
		this.charge = this.charge > this.maxCharge ? this.maxCharge : this.charge;
		if(this.charge < -1E-6){
			throw new PowerFlowException("Capacitor overdrawn: "+this.charge);
		}
		//temp and eff don't update in naive model
	}

	@Override
	public BatteryModel createClone() {
		SimpleCapacitor clone = new SimpleCapacitor(this.maxCharge, this.charge);
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
