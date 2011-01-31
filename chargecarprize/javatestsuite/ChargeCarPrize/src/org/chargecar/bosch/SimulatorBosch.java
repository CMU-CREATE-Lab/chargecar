package org.chargecar.bosch;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.chargecar.algodev.OmniscientPolicy;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.battery.LiFePo4;
import org.chargecar.prize.battery.SimpleCapacitor;
import org.chargecar.prize.policies.NoCapPolicy;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.SimulationResults;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripFeatures;
import org.chargecar.prize.util.Vehicle;
import org.chargecar.prize.visualization.ConsoleWriter;
import org.chargecar.prize.visualization.Visualizer;

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
public class SimulatorBosch {
    static Visualizer visualizer = new ConsoleWriter();
    static Vehicle civic = new Vehicle(1200, 1.988, 0.31, 0.015);
    static double systemVoltage = 96;
    static double batteryWhr = 50000;
    static double capWhr = 1e10;
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
	if (args == null || args.length < 1) {
	    System.err.println("ERROR: No GPX directory path provided.");
	    System.exit(1);
	}
	
	String csvFolder = args[0];
	File folder = new File(csvFolder);
	List<File> csvFiles = getCSVFiles(folder);
	List<Policy> policies = new ArrayList<Policy>();
	policies.add(new NoCapPolicy());
	policies.add(new OmniscientPolicy());
	
	// load policies specified on the command-line, if any
	if (args.length > 1) {
	    for (int i = 1; i < args.length; i++) {
		System.out.println("Loading Policy: " + args[i]);
		final Policy policy = instantiatePolicy(args[i]);
		if (policy != null) {
		    policies.add(policy);
		}
	    }
	}
	
	for (Policy p : policies) {
	    p.loadState();
	}
	
	List<SimulationResults> results = simulateTrips(policies, csvFiles);
	visualizer.visualizeSummary(results);
    }
    
    private static List<SimulationResults> simulateTrips(List<Policy> policies,
	    List<File> csvFiles) throws IOException {
	List<SimulationResults> results = new ArrayList<SimulationResults>();
	for (Policy p : policies) {
	    results.add(new SimulationResults(p.getName()));
	}
	for (File tripFile : csvFiles) {
	    Trip tripToTest = parseTrip(tripFile);
	    
		for (int i = 0; i < policies.size(); i++) {
		    try {
			simulateTrip(policies.get(i), tripToTest, results.get(i));
			System.out.print('.');
		    } catch (PowerFlowException e) {
			e.printStackTrace();
		    }
		}
	    
	}
	System.out.println();
	return results;
    }
    
    static List<File> getCSVFiles(File gpxFolder) {
	List<File> csvFiles = new ArrayList<File>();
	File[] files = gpxFolder.listFiles();
	for (File f : files) {
	    if (f.isDirectory()) {
		csvFiles.addAll(getCSVFiles(f));
	    } else if (f.isFile()
		    && (f.getAbsolutePath().endsWith("csv") || f
			    .getAbsolutePath().endsWith("CSV"))) {
		csvFiles.add(f);
	    }
	}
	return csvFiles;
    }
    
    private static void simulateTrip(Policy policy, Trip trip,
	    SimulationResults results) throws PowerFlowException {
	BatteryModel tripBattery = new LiFePo4(batteryWhr, batteryWhr, systemVoltage);
	BatteryModel tripCap = new SimpleCapacitor(capWhr, 0, systemVoltage);
	simulate(policy, trip, tripBattery, tripCap);
	results.addTrip(trip, tripBattery, tripCap);
    }
    
    private static void simulate(Policy policy, Trip trip,
	    BatteryModel battery, BatteryModel cap) throws PowerFlowException {
	policy.beginTrip(trip.getFeatures(), battery.createClone(), cap
		.createClone());
	if(policy.getName().equals("Omnipotent Policy")){
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
    
    private static Trip parseTrip(File csvFile) throws IOException {
	List<PointFeatures> tripPoints = CSVTripParser.parseTrips(csvFile, civic);
	String driverName = "Bosch ACC";
	TripFeatures tf = new TripFeatures(driverName, civic, tripPoints.get(0));
	return new Trip(tf, tripPoints);
    }
    
    private static Policy instantiatePolicy(String policyClassName) {
	try {
	    Class<?> clazz;
	    try{
		clazz = Class.forName("org.chargecar.prize.policies."+policyClassName);
		policyClassName = "org.chargecar.prize.policies."+policyClassName;
	    }
	    catch(ClassNotFoundException e){}
	    clazz = Class.forName(policyClassName);
	    final Constructor<?> constructor = clazz.getConstructor();
	    if (constructor != null) {
		final Policy policy = (Policy) constructor.newInstance();
		if (policy == null) {
		    System.err
			    .println("Instantiation of Policy implementation [\" + policyClassName + \"] returned null.  Weird.");
		} else {
		    return policy;
		}
	    }
	} catch (ClassNotFoundException e) {
	    System.err
		    .println("ClassNotFoundException while trying to find Policy implementation ["
			    + policyClassName + "]: " + e);
	} catch (NoSuchMethodException e) {
	    System.err
		    .println("NoSuchMethodException while trying to find no-arg constructor for Policy implementation ["
			    + policyClassName + "]: " + e);
	} catch (IllegalAccessException e) {
	    System.err
		    .println("IllegalAccessException while trying to instantiate Policy implementation ["
			    + policyClassName + "]: " + e);
	} catch (InvocationTargetException e) {
	    System.err
		    .println("InvocationTargetException while trying to instantiate Policy implementation ["
			    + policyClassName + "]: " + e);
	} catch (InstantiationException e) {
	    System.err
		    .println("InstantiationException while trying to instantiate Policy implementation ["
			    + policyClassName + "]: " + e);
	}
	
	return null;
    }
    
}

