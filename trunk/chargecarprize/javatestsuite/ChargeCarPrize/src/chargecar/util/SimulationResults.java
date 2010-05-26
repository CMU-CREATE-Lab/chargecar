package chargecar.util;

import java.util.ArrayList;
import java.util.List;

import chargecar.battery.BatteryModel;


/**
 * DO NOT EDIT
 * Stores the result of running a simulation over multiple trips for a single policy.
 * At the moment, only stores the batteryCurrent integral for each trip, as that is
 * what we're interested in with the simple batteries.
 * @author Alex Styler
 * 
 */
public class SimulationResults {
	private List<Double> batteryCurrentSquaredIntegrals;
	private List<Double> chargeSpent;
	private List<String> tripStrings;
	private String policyName;

	public SimulationResults(String policyName)
	{
		this.policyName = policyName;
		tripStrings = new ArrayList<String>();
		batteryCurrentSquaredIntegrals = new ArrayList<Double>();	
		chargeSpent = new ArrayList<Double>();
	}
	
	public void addTrip(Trip trip, BatteryModel battery, BatteryModel capacitor)
	{
		batteryCurrentSquaredIntegrals.add(battery.currentSquaredIntegral());
		chargeSpent.add(battery.getMaxCharge() - battery.getCharge() - capacitor.getCharge());
		tripStrings.add(trip.toString());
	}
	
	public List<Double> getBatteryCurrentSquaredIntegrals(){
		return batteryCurrentSquaredIntegrals;
	}
	
	public List<Double> getChargeSpent(){
		return chargeSpent;
	}
	
	public List<String> getTripStrings(){
		return tripStrings;
	}

	public String getPolicyName() {
		return policyName;
	}
	
}
