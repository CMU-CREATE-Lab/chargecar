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
    protected final List<Double> chargeHistory = new ArrayList<Double>();
    protected final List<Double> efficiencyHistory = new ArrayList<Double>();
    protected final List<Double> currentDrawHistory = new ArrayList<Double>();
    protected final List<Double> voltageHistory = new ArrayList<Double>();
    protected final List<Integer> periodHistory = new ArrayList<Integer>();
    
    protected double temperature;
    protected double efficiency;
    protected double charge;
    protected double current;
    protected double voltage;
    protected int periodMS;
    protected double maxCharge;
    
    public abstract BatteryModel createClone();
    
    public abstract double calculateTemperature(double current, int periodMS);
    public abstract double calculateEfficiency(double current, int periodMS);
    public abstract double calculateVoltage(double current, int periodMS);
    
    public void drawPower(double power, PointFeatures point) throws PowerFlowException{
	this.current = powerToCurrent(power);
	this.periodMS = point.getPeriodMS();
	this.efficiency = calculateEfficiency(this.current, this.periodMS);
	// record this current as starting at the current time
	recordHistory(point);
	// after the period is up, update charge & temp
	this.temperature = calculateTemperature(this.current, this.periodMS);
	this.voltage = calculateVoltage(this.current, this.periodMS);
	if (current < 0) {
	    this.charge = charge + current / this.efficiency
		    * (periodMS / MS_PER_HOUR);
	} else {
	    this.charge = charge + current * this.efficiency
		    * (periodMS / MS_PER_HOUR);
	}
    }
    
    public double getMaxCharge() {
	return this.maxCharge;
    }
    
    public double getCharge() {
	return this.charge;
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
    
    public List<Double> getChargeHistory() {
	return this.chargeHistory;
    }
    
    public List<Double> getCurrentDrawHistory() {
	return this.currentDrawHistory;
    }
    
    public List<Integer> getPeriodHistory() {
	return this.periodHistory;
    }
    
    protected void recordHistory(PointFeatures point) {
	this.temperatureHistory.add(temperature);
	this.chargeHistory.add(charge);
	this.efficiencyHistory.add(efficiency);
	this.periodHistory.add(periodMS);
	this.currentDrawHistory.add(current);
	this.voltageHistory.add(voltage);
    }
    
    public Double currentSquaredIntegral() {
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
    
    public double getMaxPower(int periodMS) {
	double currentInternalMax = (this.maxCharge - this.charge)
		/ (periodMS / MS_PER_HOUR);
	double currentAdjusted = currentInternalMax * calculateEfficiency(currentInternalMax, periodMS);
	return currentToPower(currentAdjusted);
	//  max current is the maximum positive current that
	// would fill the capacitor to maximum over the given period
    }
    
    public double getMinPower(int periodMS) {
	double currentInternalMin = (-1.0) * this.charge
		/ (periodMS / MS_PER_HOUR);
	double currentAdjusted = currentInternalMin * calculateEfficiency(currentInternalMin, periodMS);
	return currentToPower(currentAdjusted);
	// min current is maximum negative current that
	// would empty the current charge over the given period
    }
    
    public boolean check(PowerFlows pf, int periodMS) {
	double current = pf.getCapacitorToMotor() - pf.getBatteryToCapacitor();
	return current <= getMaxPower(periodMS)
		&& current >= getMinPower(periodMS);
    }
    
    public double powerToCurrent(double power){
	return power/this.voltage;
    }
    
    public double currentToPower(double current){
	return current*this.voltage;
    }
}