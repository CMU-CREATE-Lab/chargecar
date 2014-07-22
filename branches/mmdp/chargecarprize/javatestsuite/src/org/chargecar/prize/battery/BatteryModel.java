package org.chargecar.prize.battery;

import java.util.ArrayList;
import java.util.List;

import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;

/**
 * DO NOT EDIT
 * 
 * Abstract battery model class to be extended for different types of batteries
 * or capacitors.
 * 
 * @author Alex Styler
 */
public abstract class BatteryModel {
    public static double MS_PER_HOUR = 3600000;
    protected final List<Double> temperatureHistory = new ArrayList<Double>();
    protected final List<Double> wattHoursHistory = new ArrayList<Double>();
    protected final List<Double> efficiencyHistory = new ArrayList<Double>();
    protected final List<Double> currentDrawHistory = new ArrayList<Double>();
    protected final List<Double> voltageHistory = new ArrayList<Double>();
    protected final List<Integer> periodHistory = new ArrayList<Integer>();
    
    protected double temperature;
    protected double efficiency;
    protected double wattHours;
    protected double current;
    protected double voltage;
    protected int periodMS;
    protected double maxWattHours;
    
    public abstract BatteryModel createClone();
    
    public abstract double calculateTemperature(double current, int periodMS);
    
    public abstract double calculateEfficiency(double current, int periodMS);
    
    public abstract double calculateVoltage(double current, int periodMS);
    
    public void drawPower(double power, int periodMS) throws PowerFlowException {
	// positive means power is being drawn from this source
	// negative means power is being recharged to this source
	this.current = powerToCurrent(power);
	this.periodMS = periodMS;
	this.efficiency = calculateEfficiency(power, this.periodMS);
	// record this current as starting at the current time
	recordHistory();
	// after the period is up, update charge & temp
	this.temperature = calculateTemperature(power, this.periodMS);
	this.voltage = calculateVoltage(power, this.periodMS);
	if (power > 0) {
	    this.wattHours = this.wattHours - power / this.efficiency
		    * (periodMS / MS_PER_HOUR);
	} else {
	    this.wattHours = this.wattHours - power * this.efficiency
		    * (periodMS / MS_PER_HOUR);
	}
	if(this.wattHours < 0) throw new PowerFlowException();
	else if(this.wattHours > this.maxWattHours) throw new PowerFlowException();
    }
    
    public double getMaxWattHours() {
	return this.maxWattHours;
    }
    
    public double getWattHours() {
	return this.wattHours;
    }
    
    public double getEfficiency() {
	return this.efficiency;
    }
    
    public List<Double> getEfficiencyHistory() {
	return this.efficiencyHistory;
    }
    
    public double getTemperature() {
	return this.temperature;
    }
    
    public double getCurrent() {
	return this.current;
    }
    
    public double getVoltage() {
	return this.voltage;
    }
    
    public int getPeriodMS() {
	return this.periodMS;
    }
    
    public List<Double> getTemperatureHistory() {
	return this.temperatureHistory;
    }
    
    public List<Double> getWattHoursHistory() {
	return this.wattHoursHistory;
    }
    
    public List<Double> getCurrentDrawHistory() {
	return this.currentDrawHistory;
    }
    
    public List<Integer> getPeriodHistory() {
	return this.periodHistory;
    }
    
    protected void recordHistory() {
	this.temperatureHistory.add(temperature);
	this.wattHoursHistory.add(wattHours);
	this.efficiencyHistory.add(efficiency);
	this.periodHistory.add(periodMS);
	this.currentDrawHistory.add(current);
	this.voltageHistory.add(voltage);
    }
    
    public double getCurrentSquaredIntegral() {
	List<Double> currents = this.getCurrentDrawHistory();
	List<Integer> periods = this.getPeriodHistory();
	double integral = 0;
	for (int i = 0; i < currents.size(); i++) {
	    double currentSquared = Math.pow(currents.get(i), 2);
	    double timeLength = ((double) periods.get(i)) / 1000.0;
	    integral += currentSquared * timeLength;
	}
	return integral;
    }
    
    public double getMaxPowerDrawable(int periodMS) {
	double powerInternalMax = this.wattHours / (periodMS / MS_PER_HOUR);
	double powerAdjusted = powerInternalMax
		* calculateEfficiency(powerInternalMax, periodMS);
	return powerAdjusted;
	// max current is the maximum positive current that
	// would fill the capacitor to maximum over the given period
    }
    
    public double getMinPowerDrawable(int periodMS) {
	double powerInternalMin = (this.wattHours - this.maxWattHours)
		/ (periodMS / MS_PER_HOUR);
	double powerAdjusted = powerInternalMin
		/ calculateEfficiency(powerInternalMin, periodMS);
	return powerAdjusted;
	// min current is maximum negative current that
	// would empty the current charge over the given period
    }
    
    // public boolean check(PowerFlows pf, int periodMS) {
    // double power = pf.getCapacitorToMotor() - pf.getBatteryToCapacitor();
    // return power <= getMaxPower(periodMS)
    // && power >= getMinPower(periodMS);
    // }
    
    public double powerToCurrent(double power) {
	return power / this.voltage;
    }
    
    public double currentToPower(double current) {
	return current * this.voltage;
    }
}