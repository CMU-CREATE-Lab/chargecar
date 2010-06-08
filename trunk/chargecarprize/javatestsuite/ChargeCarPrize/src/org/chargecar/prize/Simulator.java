package org.chargecar.prize;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.battery.SimpleBattery;
import org.chargecar.prize.battery.SimpleCapacitor;
import org.chargecar.prize.policies.NaiveBufferPolicy;
import org.chargecar.prize.policies.NoCapPolicy;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.GPXTripParser;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.SimulationResults;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripFeatures;
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
public class Simulator {
    static Visualizer visualizer = new ConsoleWriter();
    static int carMass = 1200;
    
    /**
     * @param args
     *            A pathname to a GPX file or folder containing GPX files (will
     *            be recursively traversed)
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	if (args == null || args.length < 1) {
	    System.err.println("ERROR: No GPX directory path provided.");
	    System.exit(1);
	}
	
	String gpxFolder = args[0];
	File folder = new File(gpxFolder);
	List<File> gpxFiles = getGPXFiles(folder);
	System.out.println("Testing on "+gpxFiles.size()+" files.");
	List<Policy> policies = new ArrayList<Policy>();
	policies.add(new NoCapPolicy());
	policies.add(new NaiveBufferPolicy());
	
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
	BatteryModel tripBattery = new SimpleBattery(50000, 50000);
	BatteryModel tripCap = new SimpleCapacitor(50, 0);
	simulate(policy, trip, tripBattery, tripCap);
	results.addTrip(trip, tripBattery, tripCap);
    }
    
    private static void simulate(Policy policy, Trip trip,
	    BatteryModel battery, BatteryModel cap) throws PowerFlowException {
	policy.beginTrip(trip.getFeatures(), battery.createClone(), cap
		.createClone());
	for (PointFeatures point : trip.getPoints()) {
	    PowerFlows pf = policy.calculatePowerFlows(point);
	    pf.adjust(point.getPowerDemand());
	    battery.drawCurrent(pf.getBatteryToCapacitor()
		    + pf.getBatteryToMotor(), point);
	    cap.drawCurrent(pf.getCapacitorToMotor()
		    - pf.getBatteryToCapacitor(), point);
	}
	policy.endTrip();
    }
    
    private static List<Trip> parseTrips(File gpxFile) throws IOException {
	List<Trip> trips = new ArrayList<Trip>();
	GPXTripParser gpxparser = new GPXTripParser();
	for (List<PointFeatures> tripPoints : gpxparser.read(gpxFile, carMass)) {
	    String driverName = gpxFile.getParentFile().getName();
	    TripFeatures tf = new TripFeatures(driverName, carMass, tripPoints
		    .get(0));
	    trips.add(new Trip(tf, tripPoints));
	    gpxparser.clear();
	}
	return trips;
    }
    
    static List<File> getGPXFiles(File gpxFolder) {
	List<File> gpxFiles = new ArrayList<File>();
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
	return gpxFiles;
    }
    
    private static Policy instantiatePolicy(String policyClassName) {
	try {
	    Class clazz;
	    try{
		clazz = Class.forName("org.chargecar.prize.policies."+policyClassName);
		policyClassName = "org.chargecar.prize.policies."+policyClassName;
	    }
	    catch(ClassNotFoundException e){}
	    clazz = Class.forName(policyClassName);
	    final Constructor constructor = clazz.getConstructor();
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