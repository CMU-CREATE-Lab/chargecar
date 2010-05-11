package chargecar.util;

import java.util.ArrayList;
import java.util.List;

import chargecar.battery.BatteryModel;
import chargecar.capacitor.CapacitorModel;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public class SimulationResults {
	private List<Double> batteryCurrentSquaredIntegrals;
	private List<String> tripStrings;

	public SimulationResults()
	{
		tripStrings = new ArrayList<String>();
		batteryCurrentSquaredIntegrals = new ArrayList<Double>();		
	}
	
	public void addTrip(Trip trip, BatteryModel battery, CapacitorModel capactior)
	{
		batteryCurrentSquaredIntegrals.add(battery.currentSquaredIntegral());
		tripStrings.add(trip.toString());
	}
	
	public List<Double> getBatteryCurrentSquaredIntegrals(){
		return batteryCurrentSquaredIntegrals;
	}
	
	public List<String> getTripStrings(){
		return tripStrings;
	}
	
}
