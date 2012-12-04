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
    
    public SimpleCapacitor(double maxWattHours, double wattHours, double voltage) {
	this.maxWattHours = maxWattHours;
	this.wattHours = wattHours;
	this.temperature = 0.0;
	this.current = 0.0;
	this.efficiency = 1.0;
	this.voltage = voltage;
    }
    
    @Override
    public void drawPower(double current, int periodMS)
	    throws PowerFlowException {
	super.drawPower(current, periodMS);
	
	if (this.wattHours < -1E-6) {
	    throw new PowerFlowException("Capacitor overdrawn: " + this.wattHours);
	} else if (this.wattHours - this.maxWattHours > 1E-6) {
	    throw new PowerFlowException("Capacitor overcharged: "
		    + this.wattHours);
	}
    }
    
    @Override
    public BatteryModel createClone() {
	final SimpleCapacitor clone = new SimpleCapacitor(this.maxWattHours, this.wattHours, this.voltage);
	clone.wattHours = this.wattHours;
	clone.current = this.current;
	clone.efficiency = this.efficiency;
	clone.temperature = this.temperature;
	clone.wattHoursHistory.addAll(this.wattHoursHistory);
	clone.temperatureHistory.addAll(this.temperatureHistory);
	clone.currentDrawHistory.addAll(this.currentDrawHistory);
	clone.efficiencyHistory.addAll(this.efficiencyHistory);
	clone.periodHistory.addAll(this.periodHistory);
	
	return clone;
    }

    @Override
    public double calculateEfficiency(double current, int periodMS) {
	return 0.98;
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
