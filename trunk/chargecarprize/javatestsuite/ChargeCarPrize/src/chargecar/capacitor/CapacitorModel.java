package chargecar.capacitor;

import chargecar.battery.BatteryModel;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public abstract class CapacitorModel extends BatteryModel{
	protected double maxCharge;
	public double getMaxCharge(){
		return maxCharge;
	}	
	public abstract double getMaxCurrent(double periodMS);
	public abstract double getMinCurrent(double periodMS);
	
	public abstract CapacitorModel createClone();
}
