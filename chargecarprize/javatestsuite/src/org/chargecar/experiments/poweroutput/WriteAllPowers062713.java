package org.chargecar.experiments.poweroutput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.chargecar.prize.util.GPXTripParser;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripFeatures;
import org.chargecar.prize.util.Vehicle;
import org.chargecar.prize.visualization.CSVWriter;


/**
 * Experiment for MATLAB needs all possible power draws.
 * This reads and parses all files, then steps through them to
 * record all powers before writing to a CSV.
 * @author Alex Styler
 * 
 */
public class WriteAllPowers062713 {
    static Vehicle civic = new Vehicle(1200, 1.988, 0.31, 0.015);
    static CSVWriter csvWriter = new CSVWriter("/home/astyler/Dropbox/experiments/poweroutput/062713.csv");

    /**
     * @param args
     *            A pathname to a folder containing *.knn files for each driver
     *            to be tested
     *        	  e.g. java WriteAllPowers062713 "C:\ccpdata\gpxdata\test" "C:\ccpdata\knnfolder" 
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	if (args == null || args.length < 1) {
	    System.err.println("ERROR: Provide GPX directory");
	    //System.err.println("ERROR: Provide GPX directory");
	    System.exit(1);
	}
	
	String gpxFolder = args[0];

	File folder = new File(gpxFolder);
	List<File> gpxFiles;
	if(folder.isDirectory()){
	    gpxFiles = getGPXFiles(folder);
    	}else{
    	    gpxFiles = new ArrayList<File>();
    	    gpxFiles.add(folder);
    	}
    	    
	System.out.println("Testing on "+gpxFiles.size()+" GPX files.");
	
	List<PointFeatures> points = parseTrips(gpxFiles);
	csvWriter.writeTripPowers(points);
    }    
    
    private static List<PointFeatures> parseTrips(List<File> tripFiles) throws IOException {

	List<Trip> tripsToTest = new ArrayList<Trip>();
	for (File tripFile : tripFiles) {
	    tripsToTest.addAll(parseTrips(tripFile));
	}	
	
	List<PointFeatures> tripPoints = new ArrayList<PointFeatures>();
	
	for (Trip t : tripsToTest) {
	    if(t.getPoints().size() > 3600) continue;
	    System.out.println("Trip "+t.getPoints().size()+"points.");		
	    tripPoints.addAll(t.getPoints());
	}	    
	   
	
	return tripPoints;
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
