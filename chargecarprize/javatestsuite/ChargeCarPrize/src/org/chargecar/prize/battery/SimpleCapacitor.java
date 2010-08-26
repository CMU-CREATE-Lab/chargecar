package org.chargecar.prize.battery;

import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;

/**
 * DO NOT EDIT
 * 
 * Extremely simple, 100% efficient capacitor model.
 * 
 * @author Alex Styler
 */
public class SimpleCapacitor extends BatteryModel {
    
    public SimpleCapacitor(double maxCharge, double charge, double voltage) {
	this.maxCharge = maxCharge;
	this.charge = charge;
	this.temperature = 0.0;
	this.current = 0.0;
	this.efficiency = 1.0;
	this.voltage = voltage;
    }
    
    @Override
    public void drawPower(double current, PointFeatures point)
	    throws PowerFlowException {
	super.drawPower(current, point);
	
	if (this.charge < -1E-6) {
	    throw new PowerFlowException("Capacitor overdrawn: " + this.charge);
	} else if (this.charge - this.maxCharge > 1E-6) {
	    throw new PowerFlowException("Capacitor overcharged: "
		    + this.charge);
	}
    }
    
    @Override
    public BatteryModel createClone() {
	final SimpleCapacitor clone = new SimpleCapacitor(this.maxCharge, this.charge, this.voltage);
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

    @Override
    public double calculateEfficiency(double current, int periodMS) {
	return 1.0;
    }

    @Override
    public double calculateTemperature(double current, int periodMS) {
	return 0.0;
    }

    @Override
    public double calculateVoltage(double current, int periodMS) {
	return this.voltage;
    }
}
