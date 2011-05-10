package org.chargecar.prize.util;

import java.util.ArrayList;
import java.util.List;

import org.chargecar.prize.battery.BatteryModel;


/**
 * DO NOT EDIT Stores the result of running a simulation over multiple trips for
 * a single policy. At the moment, only stores the batteryCurrent integral for
 * each trip, as that is what we're interested in with the simple batteries.
 * 
 * @author Alex Styler
 * 
 */
public class SimulationResults {
    private final List<Double> batteryCurrentSquaredIntegrals;
    private final List<Double> chargeSpent;
    private final List<Double> ampHoursSums;
    private final List<String> tripStrings;
    private final String policyName;
    
    public SimulationResults(String policyName) {
	this.policyName = policyName;
	tripStrings = new ArrayList<String>();
	batteryCurrentSquaredIntegrals = new ArrayList<Double>();
	chargeSpent = new ArrayList<Double>();
	ampHoursSums = new ArrayList<Double>();
    }
    
    public void addTrip(Trip trip, BatteryModel battery, BatteryModel capacitor) {
	batteryCurrentSquaredIntegrals.add(battery.getCurrentSquaredIntegral());
	ampHoursSums.add(battery.getTotalAmpHoursServed());
	chargeSpent.add(battery.getMaxCharge() - battery.getCharge()
		- capacitor.getCharge());
	tripStrings.add(trip.toString());
    }
    
    public List<Double> getBatteryCurrentSquaredIntegrals() {
	return batteryCurrentSquaredIntegrals;
    }

    public List<Double> getAmpHoursServedSums() {
	return ampHoursSums;
    }
    public List<Double> getChargeSpent() {
	return chargeSpent;
    }
    
    public List<String> getTripStrings() {
	return tripStrings;
    }
    
    public String getPolicyName() {
	return policyName;
    }
    
}
