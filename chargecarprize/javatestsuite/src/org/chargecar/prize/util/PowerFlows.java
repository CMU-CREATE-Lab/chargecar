package org.chargecar.prize.util;

/**
 * DO NOT EDIT
 * 
 * Contains the flow commands to meet the power demand for an individual point
 * of a trip. (Battery to Motor, Cap to Motor, and Battery to Cap)
 * 
 * @author Alex Styler
 */
public class PowerFlows {
    private double batteryToMotor;
    private double capacitorToMotor;
    private double batteryToCapacitor;
    
    public PowerFlows(double batteryToMotor, double capacitorToMotor,
	    double batteryToCapacitor) {
	this.batteryToMotor = batteryToMotor;
	this.capacitorToMotor = capacitorToMotor;
	this.batteryToCapacitor = batteryToCapacitor;
    }
    
    public void adjust(double powerDemand) {
	//if (powerDemand < 0.0) {
	    this.batteryToMotor = powerDemand - this.capacitorToMotor;
	//}
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
