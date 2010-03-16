package chargecar.capacitor;

public class NaiveCapacitor extends CapacitorModel {

	public NaiveCapacitor(double maxCharge){
		this.maxCharge = maxCharge;
		this.charge = 0;
		this.temperature = 0;
		this.efficiency = 1.0;
		this.time = 0.0;
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
	protected double calculateChargeAfterDraw(double currentWatts, double periodMS) {
		double wattHoursDrawn = currentWatts * (periodMS / MS_PER_HOUR);
		return this.charge + wattHoursDrawn;
	}

	@Override
	protected double calculateEfficiency() {
		return 1.0;//always 100% efficient
	}

	@Override
	protected double calculateTemperatureAfterDraw(double current, double time) {
		return this.temperature;//no temp changing
	}

	@Override
	public void drawCurrent(double current, double periodMS) {
		this.current = current;
		//record this current as starting at the current time
		recordHistory();
		//after the period is up, update charge, temp, and eff.
		this.time = this.time + periodMS;
		this.charge = calculateChargeAfterDraw(current, periodMS);
		//temp and eff don't update in naive model
		//this.temperature = calculateTemperatureAfterDraw(current, periodMS);
		//this.efficiency = calculateEfficiency();
	}

	@Override
	public CapacitorModel createClone() {
		NaiveCapacitor clone = new NaiveCapacitor(this.maxCharge);
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
