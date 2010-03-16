package chargecar.battery;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public class NaiveBattery extends BatteryModel {

	public NaiveBattery(){
		this.time = 0.0;
		this.current = 0.0;
		this.temperature = 0.0;
		this.charge = Double.POSITIVE_INFINITY;		
		this.efficiency = calculateEfficiency();		
	}	

	@Override
	protected double calculateEfficiency() {
		return 1.0; //naive battery operates at 100% efficiency
	}

	@Override
	protected double calculateTemperatureAfterDraw(double current, double period) {
		return this.temperature; //naive ideal battery
	}

	@Override
	public void drawCurrent(double current, double periodMS) {
		this.current = current;
		//record this current as starting at the current time
		recordHistory();
		//after the period is up, update charge, temp, and eff.
		this.time = this.time + periodMS;
		this.charge = calculateChargeAfterDraw(current, periodMS);
		//temp and eff do not update in naive model
		//this.temperature = calculateTemperatureAfterDraw(current, period);
		//this.efficiency = calculateEfficiency();
	}

	@Override
	protected double calculateChargeAfterDraw(double current, double periodMS) {
		return this.charge; //infinite battery
	}

	@Override
	public BatteryModel createClone() {
		BatteryModel clone = new NaiveBattery();
		clone.charge = this.charge;
		clone.current = this.current;
		clone.efficiency = this.efficiency;
		clone.temperature = this.temperature;
		clone.time = this.time;
		clone.chargeHistory.addAll(cloneCollection(this.chargeHistory));
		clone.temperatureHistory.addAll(cloneCollection(this.temperatureHistory));
		clone.currentDrawHistory.addAll(cloneCollection(this.currentDrawHistory));
		clone.efficiencyHistory.addAll(cloneCollection(this.efficiencyHistory));
		clone.timeHistory.addAll(cloneCollection(this.timeHistory));
		return clone;
	}
}
