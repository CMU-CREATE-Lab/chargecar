package org.chargecar.experiments.hybridBMW;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.battery.LiFePo4;
import org.chargecar.prize.battery.SimpleBattery;
import org.chargecar.prize.battery.SimpleCapacitor;
import org.chargecar.prize.policies.NoCapPolicy;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.GPXTripParser;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.SimulationResults;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripFeatures;
import org.chargecar.prize.util.Vehicle;
import org.chargecar.prize.visualization.CSVWriter;
import org.chargecar.prize.visualization.ConsoleWriter;
import org.chargecar.prize.visualization.Visualizer;

/**
 * DO NOT EDIT 
 * Simulates the car over trips, making estimates for demand based on
 * a KNN lookup over past history of driving.  History is defined by
 * the points stored in the *.knn files created using SimulatorTrainer
 * @author Alex Styler
 * 
 */
public class Simulator {
    
    static Vehicle civic = new Vehicle(1200, 1.988, 0.31, 0.015);
    static Visualizer visualizer = new ConsoleWriter();
    static Visualizer visualizer2 = new CSVWriter("/home/astyler/Dropbox/bmwHybridOmni.csv");
    static double systemVoltage = 120;
    static double batteryWhr = 5000;
    //5 kWh battery
    
    /**
     * @param args
     *            A pathname to a folder containing *.knn files for each driver
     *            to be tested
     *        	  e.g. java SimulatorKNN "C:\ccpdata\gpxdata\test" "C:\ccpdata\knnfolder" 
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	if (args == null || args.length < 3) {
	    System.err.println("ERROR: Provide GPX, KNN, and OPT");
	    //System.err.println("ERROR: Provide GPX directory");
	    System.exit(1);
	}
	
	String gpxFolder = args[0];
	String knnFolder = args[1];
	String optFolder = args[2];

	File folder = new File(gpxFolder);
	List<File> gpxFiles;
	if(folder.isDirectory()){
	    gpxFiles = getGPXFiles(folder);
    	}else{
    	    gpxFiles = new ArrayList<File>();
    	    gpxFiles.add(folder);
    	}
    	 
	System.out.println("Testing on "+gpxFiles.size()+" GPX files.");
	
	OptPolicyHybrid op = new OptPolicyHybrid(optFolder);
	op.loadState();

	simulateTrips(op, gpxFiles);
	    
	}
	    
    
	private static double getCurrentSquaredSum(SimulationResults r){
	    double currentSquaredSum = 0.0;
	    for (Double d : r.getBatteryCurrentSquaredIntegrals()) {
		currentSquaredSum += d;
	    }
	    return currentSquaredSum;
	}
	
	
    private static void simulateTrips(OptPolicyHybrid op, List<File> tripFiles) throws IOException {
	int count = 0;
	List<Trip> tripsToTest = new ArrayList<Trip>();
	for (File tripFile : tripFiles) {
	    tripsToTest.addAll(parseTrips(tripFile));
	}	
	   
	    for (Trip t : tripsToTest) {
		if(t.getPoints().size() > 3600) continue;
		System.out.print('.');
		try {
		    op.parseTrip(t);
		    double cost = simulateTrip(op, t);
		    System.out.println("Trip cost: "+cost);
		} catch (PowerFlowException e) {
		    e.printStackTrace();
	    }
	}	    

	System.out.println();
	System.out.println("Trips tested: "+tripsToTest.size());
	
    }
    
    private static double simulateTrip(OptPolicyHybrid policy, Trip trip) throws PowerFlowException {
	BatteryModel tripBattery = new SimpleBattery(batteryWhr, batteryWhr/2, systemVoltage);
	return simulate(policy, trip, tripBattery);

    }
    
    private static double simulate(OptPolicyHybrid policy, Trip trip,
	    BatteryModel battery) throws PowerFlowException {
	int[] controlsSet = new int[]{0,5000,10000,15000,20000,25000,30000,35000,40000,45000,50000};
	double[] controlsCost = new double[]{0,1,2,3,4,5,6,7,8,9,10};
	Map<Integer,Double> costFunction = new HashMap<Integer,Double>(11);
	for(int i=0;i<controlsSet.length;i++){
	    costFunction.put(controlsSet[i], controlsCost[i]);
	}
	
	policy.beginTrip(trip.getFeatures(), battery.createClone());
	int i = 0;
	policy.parseTrip(trip);
	double cost = 0;
	for (PointFeatures point : trip.getPoints()) {
	    PowerControls pf = policy.calculatePowerFlows(point, i);
	    i++;
	    battery.drawPower(pf.getMotorWatts(), point.getPeriodMS());
	    cost += pf.getCost();
	}
	policy.endTrip(trip);
	return cost;
    }
    
    private static List<Trip> parseTrips(File gpxFile) throws IOException {
	List<Trip> trips = new ArrayList<Trip>();
	int i=0;
	GPXTripParser gpxparser = new GPXTripParser();
	for (List<PointFeatures> tripPoints : gpxparser.read(gpxFile, civic)) {
	    String driverName = gpxFile.getParentFile().getName();
	    String fileName = driverName+gpxFile.getName().substring(0, gpxFile.getName().lastIndexOf('.'))+"_"+i;
	    TripFeatures tf = new TripFeatures(driverName, fileName, civic, tripPoints
		    .get(0));
	    trips.add(new Trip(tf, tripPoints));
	    gpxparser.clear();
	    i++;
	}
	return trips;
    }
    
    public static void writeResults(List<Double> i2sums){
	FileWriter fstream;
	try {
	    
	    fstream = new FileWriter("C:/Users/astyler/Dropbox/experiments/whsensitivity/omnires.csv",false);
	    BufferedWriter out = new BufferedWriter(fstream);
	    boolean end = false;
	    for(double d : i2sums){		
		out.write(d+",");
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
	for(File f  : gpxFolder.listFiles()){
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
