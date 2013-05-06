package org.chargecar.prize.visualization;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.util.DriverResults;
import org.chargecar.prize.util.SimulationResults;
import org.chargecar.prize.util.Trip;

/**
 * DO NOT EDIT
 * 
 * A simple visualizer that outputs simulation details to the console screen.
 * 
 * @author Alex Styler
 */
public class ConsoleWriter implements Visualizer {
    DecimalFormat d = new DecimalFormat("0.000E0");
    DecimalFormat d2 = new DecimalFormat("0.000");
    DecimalFormat p = new DecimalFormat("0.000%");
    
    public void visualizeDrivers(List<DriverResults> results) {
	for(DriverResults r : results){
	    System.out.println("Policy: "+r.getPolicyName());
	    for(Entry<String, Double> e : r.getBatteryCurrentSquaredIntegrals().entrySet()){
		double i2base = results.get(0).getBatteryCurrentSquaredIntegrals().get(e.getKey());
		double i2Percent = 1 - (e.getValue() / i2base);
		System.out.println("   "+ e.getKey()+": "+d.format(e.getValue())+" :: "+ p.format(i2Percent) + " i2 reduction.");
	    }	    

	    for(Entry<String, Double> e : r.getTotalWattHoursServedSums().entrySet()){
		double ahbase = results.get(0).getTotalWattHoursServedSums().get(e.getKey());
		double ahPercent = 1 - (e.getValue() / ahbase);
		System.out.println("   "+ e.getKey()+": "+d.format(e.getValue())+" :: "+ p.format(ahPercent) + " total Amp Hours served reduction");
	    }	
	}
	
    }
    
    public void visualizeTrips(List<SimulationResults> results) {
	int numTrips = results.get(0).getBatteryCurrentSquaredIntegrals().size();
	
	for(int i=0;i<numTrips;i++){
	    double baseline = results.get(0).getBatteryCurrentSquaredIntegrals().get(i);
	    System.out.println("Trip "+i+":");
	    for(int j=1;j<results.size();j++){
		double i2used = results.get(j).getBatteryCurrentSquaredIntegrals().get(i);
		double i2Percent = (i2used / baseline);
		System.out.println("\t "+results.get(j).getPolicyName()+": "+p.format(i2Percent));		
	    }
	}
	
    }
    
    public void visualizeSummary(List<SimulationResults> results) {
	int tripsTested = 0;
	List<Double> currentSquaredSums = new ArrayList<Double>();
	List<Double> kwhSums = new ArrayList<Double>();
	List<Double> distSums = new ArrayList<Double>();
	for (SimulationResults r : results) {
	    tripsTested = r.getTripStrings().size();
	    double currentSquaredSum = 0.0;
	    for (Double d : r.getBatteryCurrentSquaredIntegrals()) {
		currentSquaredSum += d;
	    }
	    currentSquaredSums.add(currentSquaredSum);
	    
	    double distSum = 0.0;
	    for(Double d : r.getDistances()){
		distSum += d;
	    }
	    distSums.add(distSum); 
	    
	    double whSpentSum = 0.0;
	    for (Double wh : r.getWhSpent()) {
		whSpentSum += wh;
	    }
	    kwhSums.add(whSpentSum/1000.0);
	}
	
	double i2BaseSum = currentSquaredSums.get(0);
	double kwhBaseSum = kwhSums.get(0);
	System.out.println("Trips tested: "+tripsTested);
	System.out.println("Distance Traveled: "+distSums.get(0));
	System.out.println("Baseline, " + results.get(0).getPolicyName()
		+ ", i^2: " + d.format(i2BaseSum));
	System.out.println("Baseline, " + results.get(0).getPolicyName()
		+ ", kWh: " + d.format(kwhBaseSum) + " kWh :: "+
		d2.format(milesPerKwh(kwhBaseSum,distSums.get(0))) + " miles/kWh");
	
	for (int i = 1; i < results.size(); i++) {
	    double i2Percent = 1 - (currentSquaredSums.get(i) / i2BaseSum);
	    System.out.println(results.get(i).getPolicyName()
		    + " i^2: "
		    + d.format(currentSquaredSums.get(i)) + " :: "
		    + p.format(i2Percent) + " reduction");
	    double rangePercent =(kwhBaseSum / kwhSums.get(i))-1;
	    System.out.println(results.get(i).getPolicyName()
		    + " kWh: "
		    + d.format(kwhSums.get(i)) + " kWh :: "
		    + d2.format(milesPerKwh(kwhSums.get(i),distSums.get(i))) + " miles/kWh :: "
		    + p.format(rangePercent));

	}
    }
    
    private double milesPerKwh(double kwh, double meters){
	double miles = meters / 1609.34;
	return miles/kwh;
    }
    public void visualizeTrip(Trip trip, BatteryModel battery,
	    BatteryModel capacitor) {
	System.out.println(trip);
	System.out.println("Integral of current squared: "
		+ d.format(battery.getCurrentSquaredIntegral()));
    }
    
    public void visualizeTrips(SimulationResults simResults) {
	String policyName = simResults.getPolicyName();
	System.out.println("=============");
	System.out.println(policyName + " i^2 results:");
	for (int i = 0; i < simResults.getTripStrings().size(); i++) {
	    String trip = simResults.getTripStrings().get(i);
	    double currentSquaredIntegral = simResults
		    .getBatteryCurrentSquaredIntegrals().get(i);
	    System.out.println(d.format(currentSquaredIntegral) + "; " + trip);
	}
    }
}
