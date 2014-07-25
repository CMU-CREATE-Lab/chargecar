package org.chargecar.experiments.hybridBMW.fakeTrips;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.chargecar.experiments.hybridBMW.CostFunction;
import org.chargecar.experiments.hybridBMW.HybridSimResults;
import org.chargecar.experiments.hybridBMW.NaivePolicyHybrid;
import org.chargecar.experiments.hybridBMW.OptPolicyHybrid;
import org.chargecar.experiments.hybridBMW.PowerControls;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.battery.SimpleBattery;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.Vehicle;

/**
 * DO NOT EDIT Simulates the car over trips, making estimates for demand based
 * on a KNN lookup over past history of driving. History is defined by the
 * points stored in the *.knn files created using SimulatorTrainer
 * 
 * @author Alex Styler
 * 
 */
public class SimulatorFake {
    
    static Vehicle civic = new Vehicle(1200, 1.988, 0.31, 0.015);
    static double systemVoltage = 120;
    static double batteryWhr = 5000;
    static double startingWhr = 0;
    
    // 5 kWh battery
    
    /**
     * @param args
     *            A pathname to a folder containing *.knn files for each driver
     *            to be tested e.g. java SimulatorKNN "C:\ccpdata\gpxdata\test"
     *            "C:\ccpdata\knnfolder"
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	if (args == null || args.length < 1) {
	    System.err.println("ERROR: Provide OPT");
	    System.exit(1);
	}
	
	String optFolder = args[0];
	HybridSimResults res;
	
	List<Trip> trips = getTrips();
	
	OptPolicyHybrid op = new OptPolicyHybrid(optFolder, new int[] { 0,
		5000, 10000, 15000, 20000, 25000, 30000, 35000, 40000, 45000,
		50000 });
	op.loadState();
	op.setShortName("OptAll");
	
	res = simulateTrips(op, trips, true);
	System.out.println("Policy: " + op.getShortName());
	System.out.println("Total Costs: " + res.getTotalCost());
	
	/*
	 * op = new OptPolicyHybrid(optFolder, new int[] {25000,0});
	 * op.loadState(); op.setShortName("OptLim");
	 * 
	 * res = simulateTrips(op, trips, true); System.out.println("Policy: " +
	 * op.getShortName()); System.out.println("Total Costs: " +
	 * res.getTotalCost());
	 */
	NaivePolicyHybrid np = new NaivePolicyHybrid();
	
	res = simulateTrips(np, trips, false);
	System.out.println("Policy: " + np.getShortName());
	System.out.println("Total Costs: " + res.getTotalCost());
    }
    
    private static List<Trip> getTrips() {
	List<Trip> trips = new ArrayList<Trip>();
	trips.add(FakeTripMaker.getTrip(civic, 4));
	return trips;
    }
    
    private static HybridSimResults simulateTrips(OptPolicyHybrid op,
	    List<Trip> tripsToTest, boolean debug_writeResults)
	    throws IOException {
	HybridSimResults res = new HybridSimResults();
	
	for (Trip t : tripsToTest) {
	    if (t.getPoints().size() > 3600) continue;
	    System.out.print('.');
	    try {
		op.parseTrip(t);
		res.addTrip(simulateTrip(op, t, debug_writeResults));
		
	    } catch (PowerFlowException e) {
		e.printStackTrace();
	    }
	}
	
	System.out.println();
	System.out.println("Trips tested: " + tripsToTest.size());
	return res;
	
    }
    
    private static double simulateTrip(OptPolicyHybrid policy, Trip trip,
	    boolean debug_writeResults) throws PowerFlowException {
	BatteryModel tripBattery = new SimpleBattery(batteryWhr, startingWhr,
		systemVoltage);
	return simulate(policy, trip, tripBattery, debug_writeResults);
	
    }
    
    private static double simulate(OptPolicyHybrid policy, Trip trip,
	    BatteryModel battery, boolean debug_writeResults)
	    throws PowerFlowException {
	policy.beginTrip(trip.getFeatures(), battery.createClone());
	
	List<PowerControls> pc = new ArrayList<PowerControls>();
	List<Double> costs = new ArrayList<Double>();
	List<Double> charge = new ArrayList<Double>();
	
	int i = 0;
	policy.parseTrip(trip);
	
	double totalCost = 0;
	
	for (PointFeatures point : trip.getPoints()) {
	    PowerControls pf = policy.calculatePowerFlows(point, i);
	    pc.add(pf);
	    i++;
	    battery.drawPower(pf.getMotorWatts(), point.getPeriodMS());
	    totalCost += CostFunction.getCost(pf.getEngineWatts());
	    costs.add(totalCost);
	    charge.add(battery.getWattHours());
	}
	policy.endTrip(trip);
	
	if (debug_writeResults)
	    writeResults(policy.getShortName(), pc, costs, charge);
	
	return totalCost;
    }
    
    public static void writeResults(String id, List<PowerControls> controls,
	    List<Double> costs, List<Double> wh) {
	FileWriter fstream;
	try {
	    
	    fstream = new FileWriter(
		    "/home/astyler/Dropbox/experiments/hybridtest/" + id
			    + Integer.toString((int) startingWhr) + ".csv",
		    false);
	    BufferedWriter out = new BufferedWriter(fstream);
	    for (int i = 0; i < controls.size(); i++) {
		PowerControls pc = controls.get(i);
		out.write(pc.getEngineWatts() + "," + pc.getMotorWatts() + ","
			+ costs.get(i) + "," + wh.get(i) + "\n");
	    }
	    out.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
}
