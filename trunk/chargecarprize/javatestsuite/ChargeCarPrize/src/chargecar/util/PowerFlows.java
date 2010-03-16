/**
 * 
 */
package chargecar.util;

/**
 * @author Alex Styler
 * DO NOT EDIT
 * Contains the flow commands to meet the power demand for 
 * an individual point of a trip. (Battery to Motor, Cap to Motor,
 * and Battery to Cap)
 */
public class PowerFlows {
	private double batteryToMotor;
	private double capacitorToMotor;
	private double batteryToCapacitor;
	
	public PowerFlows(double batteryToMotor, double capacitorToMotor, double batteryToCapacitor)
	{
		this.batteryToMotor = batteryToMotor;
		this.capacitorToMotor = capacitorToMotor;
		this.batteryToCapacitor = batteryToCapacitor;
	}
	public double getBatteryToMotor() {
		return batteryToMotor;
	}
	public double getCapacitorToMotor() {
		return capacitorToMotor;
	}
	public double getBatteryToCapacitor() {
		return batteryToCapacitor;
	}
}
