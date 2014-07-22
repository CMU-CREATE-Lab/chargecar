package org.chargecar.experiments.hybridBMW;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.battery.SimpleBattery;
import org.chargecar.prize.util.GPXTripParser;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripFeatures;
import org.chargecar.prize.util.Vehicle;

/**
 * DO NOT EDIT Simulates the car over trips, making estimates for demand based
 * on a KNN lookup over past history of driving. History is defined by the
 * points stored in the *.knn files created using SimulatorTrainer
 * 
 * @author Alex Styler
 * 
 */
public class Simulator {
    
    static Vehicle civic = new Vehicle(1200, 1.988, 0.31, 0.015);
    static double systemVoltage = 120;
    static double batteryWhr = 5000;
    
    // 5 kWh battery
    
    /**
     * @param args
     *            A pathname to a folder containing *.knn files for each driver
     *            to be tested e.g. java SimulatorKNN "C:\ccpdata\gpxdata\test"
     *            "C:\ccpdata\knnfolder"
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	if (args == null || args.length < 2) {
	    System.err.println("ERROR: Provide GPX and OPT");
	    System.exit(1);
	}
	
	String gpxFolder = args[0];
	String optFolder = args[1];
	
	File folder = new File(gpxFolder);
	List<File> gpxFiles;
	if (folder.isDirectory()) {
	    gpxFiles = getGPXFiles(folder);
	} else {
	    gpxFiles = new ArrayList<File>();
	    gpxFiles.add(folder);
	}
	
	System.out.println("Testing on " + gpxFiles.size() + " GPX files.");
	HybridSimResults res;
	
	
	OptPolicyHybrid op = new OptPolicyHybrid(optFolder, new int[] { 0,
		5000, 10000, 15000, 20000, 25000, 30000, 35000, 40000, 45000,
		50000 });
	op.loadState();
	op.setShortName("OptAll");
	
	res = simulateTrips(op, gpxFiles);
	
	System.out.println("Policy: " + op.getName());
	System.out.println("Total Costs: " + res.getTotalCost());
	
	NaivePolicyHybrid np = new NaivePolicyHybrid();
	
	res = simulateTrips(np, gpxFiles);
	System.out.println("Policy: " + np.getName());
	System.out.println("Total Costs: " + res.getTotalCost());
	
    }
      
    private static HybridSimResults simulateTrips(OptPolicyHybrid op,
	    List<File> tripFiles) throws IOException {
	HybridSimResults res = new HybridSimResults();
	List<Trip> tripsToTest = new ArrayList<Trip>();
	for (File tripFile : tripFiles) {
	    tripsToTest.addAll(parseTrips(tripFile));
	}
	
	for (Trip t : tripsToTest) {
	    if (t.getPoints().size() > 3600) continue;
	    System.out.print('.');
	    try {
		op.parseTrip(t);
		res.addTrip(simulateTrip(op, t));
		
	    } catch (PowerFlowException e) {
		e.printStackTrace();
	    }
	}
	
	System.out.println();
	System.out.println("Trips tested: " + tripsToTest.size());
	return res;
	
    }
    
    private static double simulateTrip(OptPolicyHybrid policy, Trip trip)
	    throws PowerFlowException {
	BatteryModel tripBattery = new SimpleBattery(batteryWhr, 0,
		systemVoltage);
	return simulate(policy, trip, tripBattery);
	
    }
    
    private static double simulate(OptPolicyHybrid policy, Trip trip,
	    BatteryModel battery) throws PowerFlowException {
	policy.beginTrip(trip.getFeatures(), battery.createClone());
	int i = 0;
	policy.parseTrip(trip);
	double cost = 0;
	for (PointFeatures point : trip.getPoints()) {
	    PowerControls pf = policy.calculatePowerFlows(point, i);
	    i++;
	    battery.drawPower(pf.getMotorWatts(), point.getPeriodMS());
	    cost += CostFunction.getCost(pf.getEngineWatts());
	}
	policy.endTrip(trip);
	return cost;
    }
    
    private static List<Trip> parseTrips(File gpxFile) throws IOException {
	List<Trip> trips = new ArrayList<Trip>();
	int i = 0;
	GPXTripParser gpxparser = new GPXTripParser();
	for (List<PointFeatures> tripPoints : gpxparser.read(gpxFile, civic)) {
	    String driverName = gpxFile.getParentFile().getName();
	    String fileName = driverName
		    + gpxFile.getName().substring(0,
			    gpxFile.getName().lastIndexOf('.')) + "_" + i;
	    TripFeatures tf = new TripFeatures(driverName, fileName, civic,
		    tripPoints.get(0));
	    trips.add(new Trip(tf, tripPoints));
	    gpxparser.clear();
	    i++;
	}
	return trips;
    }
    
    public static void writeResults(List<Double> i2sums) {
	FileWriter fstream;
	try {
	    
	    fstream = new FileWriter(
		    "C:/Users/astyler/Dropbox/experiments/whsensitivity/omnires.csv",
		    false);
	    BufferedWriter out = new BufferedWriter(fstream);

	    for (double d : i2sums) {
		out.write(d + ",");
	    }
	    
	    out.write("0.0\n");
	    out.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    static List<File> getGPXFiles(File gpxFolder) {
	List<File> gpxFiles = new ArrayList<File>();
	List<File> files = new ArrayList<File>();
	for (File f : gpxFolder.listFiles()) {
	    files.add(f);
	}
	Collections.sort(files);
	
	for (File f : files) {
	    if (f.isDirectory()) {
		gpxFiles.addAll(getGPXFiles(f));
	    } else if (f.isFile()
		    && (f.getAbsolutePath().endsWith("gpx") || f
			    .getAbsolutePath().endsWith("GPX"))) {
		gpxFiles.add(f);
	    }
	}
	
	return gpxFiles;
    }
}
