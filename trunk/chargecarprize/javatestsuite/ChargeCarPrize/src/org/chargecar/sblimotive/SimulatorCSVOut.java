package org.chargecar.sblimotive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.chargecar.algodev.*;
import org.chargecar.algodev.knn.*;
import org.chargecar.algodev.policies.KnnKdTreePolicy;
import org.chargecar.prize.battery.*;
import org.chargecar.prize.policies.*;
import org.chargecar.prize.util.*;
import org.chargecar.prize.visualization.*;

/**
 * DO NOT EDIT Runs the simulation of an electric car running over a commute
 * defined by GPX file from real world commutes. Uses a compound energy storage
 * Policy to decide whether to get/store power in either the capacitor or
 * battery inside the car.
 * 
 * Competitors need only modify UserPolicy with their algorithm.
 * 
 * @author Alex Styler
 * 
 */
public class SimulatorCSVOut {    
    static Vehicle civic = new Vehicle(1250, 1.988, 0.31, 0.015);
    static Visualizer visualizer = new ConsoleWriter();
    static double systemVoltage = 400;
    static double batteryWhr = 50000;
    static double capWhr = 100;
    static String csvFolder;
    /**
     * @param args
     *            A pathname to a GPX file or folder containing GPX files (will
     *            be recursively traversed)
     *            Alternate policies to test, either in a referenced JAR file or 
     *            within the project
     *        	  e.g. java Simulator "C:\testdata\may" org.chargecar.policies.SpeedPolicy
     *        	       java Simulator "C:\testdata" NaiveBufferPolicy SpeedPolicy
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	if (args == null || args.length < 3) {
	    System.err.println("ERROR: Provide: GPX directory, KNN directory, and CSV output directory");
	    System.exit(1);
	}
	
	String gpxFolder = args[0];
	String knnFolder = args[1];
	csvFolder = args[2];
	File folder = new File(gpxFolder);
	List<File> gpxFiles = getGPXFiles(folder);
	System.out.println("Testing on "+gpxFiles.size()+" GPX files.");
	List<Policy> policies = new ArrayList<Policy>();
	
	policies.add(new NoCapPolicy());
	policies.add(new KnnKdTreePolicy(knnFolder,7,240));
	policies.add(new OmniscientPolicy(1000000));
	
	for (Policy p : policies) {
	    p.loadState();
	}
	
	List<SimulationResults> results = simulateTrips(policies, gpxFiles);
	visualizer.visualizeSummary(results);
    }
    
    private static List<SimulationResults> simulateTrips(List<Policy> policies,
	    List<File> tripFiles) throws IOException {
	List<SimulationResults> results = new ArrayList<SimulationResults>();
	for (Policy p : policies) {
	    results.add(new SimulationResults(p.getName()));
	}
	for (File tripFile : tripFiles) {
	    List<Trip> tripsToTest = parseTrips(tripFile);
	    for (Trip t : tripsToTest) {
		for (int i = 0; i < policies.size(); i++) {
		    try {
			simulateTrip(policies.get(i), t, results.get(i));
			System.out.print('.');
		    } catch (PowerFlowException e) {
			e.printStackTrace();
		    }
		}
	    }
	}
	System.out.println();
	return results;
    }
    
    private static void simulateTrip(Policy policy, Trip trip,
	    SimulationResults results) throws PowerFlowException {
	BatteryModel tripBattery = new SimpleBattery(batteryWhr, batteryWhr, systemVoltage);
	BatteryModel tripCap = new SimpleCapacitor(capWhr, 0, systemVoltage);
	simulate(policy, trip, tripBattery, tripCap);
	results.addTrip(trip, tripBattery, tripCap);
	CSVWriter writer = new CSVWriter(csvFolder+"\\"+policy.getShortName()+"\\"+trip.getFeatures().getFileName()+".csv");
	writer.writeBatteryPowers(tripBattery);
    }
    
    private static void simulate(Policy policy, Trip trip,
	    BatteryModel battery, BatteryModel cap) throws PowerFlowException {
	policy.beginTrip(trip.getFeatures(), battery.createClone(), cap
		.createClone());
	if(policy.getName().equals("Omniscient Policy")){
	    ((OmniscientPolicy)policy).parseTrip(trip);
	}
	for (PointFeatures point : trip.getPoints()) {
	    PowerFlows pf = policy.calculatePowerFlows(point);
	    pf.adjust(point.getPowerDemand());
	    battery.drawPower(pf.getBatteryToCapacitor()
		    + pf.getBatteryToMotor(), point);
	    cap.drawPower(pf.getCapacitorToMotor()
		    - pf.getBatteryToCapacitor(), point);
	}
	policy.endTrip();
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
    
    static List<File> getGPXFiles(File gpxFolder) {
	List<File> gpxFiles = new ArrayList<File>();
	if(gpxFolder.isDirectory()){
	    File[] files = gpxFolder.listFiles();	    
	for (File f : files) {
	    if (f.isDirectory()) {
		gpxFiles.addAll(getGPXFiles(f));
	    } else if (f.isFile()
		    && (f.getAbsolutePath().endsWith("gpx") || f
			    .getAbsolutePath().endsWith("GPX"))) {
		gpxFiles.add(f);
	    }
	}
	}
	else{
	    gpxFiles.add(gpxFolder);
	}
	
	return gpxFiles;
    }
}
