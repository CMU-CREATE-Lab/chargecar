package org.chargecar.prize.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chargecar.prize.battery.BatteryModel;


/**
 * DO NOT EDIT Stores the result of running a simulation over multiple trips for
 * a single policy. At the moment, only stores the batteryCurrent integral for
 * each trip, as that is what we're interested in with the simple batteries.
 * 
 * @author Alex Styler
 * 
 */
public class DriverResults {
    private Map<String,Double> batteryCurrentSquaredIntegrals;
    private String policyName;
    
    public DriverResults(String policyName) {
	this.policyName = policyName;
	batteryCurrentSquaredIntegrals = new HashMap<String,Double>();
    }
    
    public void addTrip(Trip trip, BatteryModel battery, BatteryModel capacitor) {
	String driver = trip.getFeatures().getDriver();	
	if(this.batteryCurrentSquaredIntegrals.containsKey(driver)){
	    double sum = this.batteryCurrentSquaredIntegrals.get(driver);
	    sum += battery.currentSquaredIntegral();
	}
	else{
	    this.batteryCurrentSquaredIntegrals.put(driver, battery.currentSquaredIntegral());
	}
    }
    
    public Map<String,Double> getBatteryCurrentSquaredIntegrals() {
	return batteryCurrentSquaredIntegrals;
    }    

    public String getPolicyName() {
	return policyName;
    }
    
}
