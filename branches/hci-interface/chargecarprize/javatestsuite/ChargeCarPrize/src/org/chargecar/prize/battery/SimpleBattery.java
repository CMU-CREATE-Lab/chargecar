package org.chargecar.prize.battery;

/**
 * DO NOT EDIT
 * 
 * Extremely naive battery implementation that operates at 100% efficiency at
 * all charges and temperatures. Used primarily to analyze current^2 on
 * batteries. More complex models of batteries would be used for temp/efficiency
 * optimization and range experimentation.
 * 
 * @author Alex Styler
 * 
 */
public class SimpleBattery extends BatteryModel {
    
    public SimpleBattery(double maxWattHours, double wattHours, double voltage) {
	this.current = 0.0;
	this.temperature = 0.0;
	this.efficiency = 1.0;
	this.wattHours = wattHours;
	this.maxWattHours  = maxWattHours;
	this.voltage = voltage;
    }

    @Override
    public BatteryModel createClone() {
	final BatteryModel clone = new SimpleBattery(this.maxWattHours, this.wattHours, this.voltage);
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
 