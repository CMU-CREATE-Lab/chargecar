package org.chargecar.algodev;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.chargecar.algodev.knn.KnnTableTrainer;
import org.chargecar.prize.util.GPXTripParser;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripFeatures;
import org.chargecar.prize.util.Vehicle;

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
public class SimulatorTrainer {
    static Vehicle civic = new Vehicle(1200, 1.988, 0.31, 0.015);
    
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
	
	String gpxFolder = args[0];
	File folder = new File(gpxFolder);
	List<File> gpxFiles = getGPXFiles(folder);
	System.out.println("Training on "+gpxFiles.size()+" GPX files.");
	KnnTableTrainer policy = new KnnTableTrainer();
	
	for (File tripFile : gpxFiles) {
	    List<Trip> tripsToTest = parseTrips(tripFile);
	    for (Trip t : tripsToTest) {
		policy.parseTrip(t);
	    }
	}	
	policy.finishTraining();
	System.out.println("Complete.");
    }    
    
    private static List<Trip> parseTrips(File gpxFile) throws IOException {
	List<Trip> trips = new ArrayList<Trip>();
	GPXTripParser gpxparser = new GPXTripParser();
	for (List<PointFeatures> tripPoints : gpxparser.read(gpxFile, civic)) {
	    String driverName = gpxFile.getParentFile().getName();
	    TripFeatures tf = new TripFeatures(driverName, civic, tripPoints
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
}
