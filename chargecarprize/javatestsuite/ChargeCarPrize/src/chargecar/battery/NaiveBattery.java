package chargecar.battery;

public class NaiveBattery extends BatteryModel {

	public NaiveBattery(){
		this.time = 0.0;
		this.current = 0.0;
		this.temperature = 0.0;
		this.charge = Double.POSITIVE_INFINITY;		
		this.efficiency = calculateEfficiency();
		
	}
	
	public NaiveBattery(BatteryModel battery){
		this.temperature = battery.getTemperature();
		this.efficiency = battery.getEfficiency();
		this.charge = battery.getCharge();
		this.time = battery.getTime();	
		this.current = battery.getCurrent();
		this.temperatureHistory.addAll(battery.getTemperatureHistory());
		this.efficiencyHistory.addAll(battery.getEfficiencyHistory());
		this.timeHistory.addAll(battery.getTimeHistory());
		this.chargeHistory.addAll(battery.getChargeHistory());
		this.currentDrawHistory.addAll(battery.getCurrentDrawHistory());
	}
	
	@Override
	protected double calculateEfficiency() {
		return 100.0; //naive battery operates at 100% efficiency
	}

	@Override
	protected double calculateTemperatureAfterDraw(double current, double period) {
		return this.temperature; //naive ideal battery
	}

	@Override
	public void drawCurrent(double current, double period) {
		this.current = current;
		//record this current as starting at the current time
		recordHistory();
		//after the period is up, update charge, temp, and eff.
		this.time = this.time + period;
		this.charge = calculateChargeAfterDraw(current, period);
		this.temperature = calculateTemperatureAfterDraw(current, period);
		this.efficiency = calculateEfficiency();
	}

	@Override
	protected double calculateChargeAfterDraw(double current, double period) {
		return this.charge; //infinite battery
	}

}
