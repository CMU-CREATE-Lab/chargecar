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
	public double getMaxCurrent(double period) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMinCurrent(double period) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected double calculateChargeAfterDraw(double current, double time) {
		// TODO Auto-generated method stub
		return 0;
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
	public void drawCurrent(double current, double time) {
		// TODO Auto-generated method stub
		
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
