package org.chargecar.experiments.aamasresults;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import org.chargecar.algodev.SimulatorTrainer;
import org.chargecar.algodev.policies.KnnDistPolyPolicy;
import org.chargecar.algodev.policies.KnnDistributionPolicy;
import org.chargecar.algodev.policies.KnnMMDPLive;
import org.chargecar.algodev.policies.OptimalPolicy;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.battery.LiFePo4;
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
public class SimulatorWh {
    static Vehicle civic = new Vehicle(1200, 1.988, 0.31, 0.015);
    static Visualizer visualizer = new ConsoleWriter();
    static Visualizer visualizer2 = new CSVWriter("/home/astyler/Dropbox/illahasthor50whr.csv");
    static double systemVoltage = 120;
    static double batteryWhr = 50000;
    
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
	
//	KnnDistributionPolicy kp = new KnnDistributionPolicy(knnFolder,optFolder,1,false);
	OptimalPolicy op = new OptimalPolicy(optFolder);
	op.loadState();
	
	List<Double> i2sums = new ArrayList<Double>();
	 
	List<Integer> whToTest = new ArrayList<Integer>();
	whToTest.add(5);
	whToTest.add(10);
	whToTest.add(50);
	whToTest.add(100);
	whToTest.add(200);
	whToTest.add(1000);
	
	File optFolderParent = new File(optFolder);
	optFolderParent.mkdirs();
	
	for(int wh : whToTest){
	    String newOptFolder = optFolder + "/mmdp"+wh;
	   /* String[] tempArgs = new String[3];
	    tempArgs[0] = args[0];
	    tempArgs[1] = newOptFolder;
	    tempArgs[2] = Integer.toString(wh);
	    SimulatorTrainer.main(tempArgs);*/   
	    	    
	    op.setOptPath(newOptFolder);	    
	    SimulationResults results = simulateTrips(op, gpxFiles, wh);

	   double sum = getCurrentSquaredSum(results);
	   i2sums.add(sum);
	    
	    System.out.println("Wh = " + wh+ ": " + sum);    		
	}
	
	writeResults(i2sums);
//	visualizer.visualizeTrips(results);
//	visualizer.visualizeSummary(results);
//	visualizer2.visualizeTrips(results);
    }    
    
	private static double getCurrentSquaredSum(SimulationResults r){
	    double currentSquaredSum = 0.0;
	    for (Double d : r.getBatteryCurrentSquaredIntegrals()) {
		currentSquaredSum += d;
	    }
	    return currentSquaredSum;
	}
	
	
    private static SimulationResults simulateTrips(OptimalPolicy op,
	    List<File> tripFiles, int capWhr) throws IOException {
	SimulationResults results = new SimulationResults(op.getName());
	
	int count = 0;
	List<Trip> tripsToTest = new ArrayList<Trip>();
	for (File tripFile : tripFiles) {
	    tripsToTest.addAll(parseTrips(tripFile));
	}	
	   
	    for (Trip t : tripsToTest) {
		if(t.getPoints().size() > 3600) continue;
		//System.out.println("Trip "+t.getPoints().size()+"points.");
		System.out.print('.');
		try {
		 //   int tripID = t.hashCode();
		    op.parseTrip(t);
		    simulateTrip(op, t, results, capWhr);
		} catch (PowerFlowException e) {
		    e.printStackTrace();
	    }
	}	    

	System.out.println();
	//System.out.println("Trips tested: "+tripsToTest.size());
	
	return results;
    }
    
    private static void simulateTrip(OptimalPolicy policy, Trip trip,
	    SimulationResults results, int capWhr) throws PowerFlowException {
	BatteryModel tripBattery = new LiFePo4(batteryWhr, batteryWhr, systemVoltage);
	BatteryModel tripCap = new SimpleCapacitor(capWhr, 0, systemVoltage);
	simulate(policy, trip, tripBattery, tripCap);
	results.addTrip(trip, tripBattery, tripCap);
    }
    
    private static void simulate(OptimalPolicy policy, Trip trip,
	    BatteryModel battery, BatteryModel cap) throws PowerFlowException {
	policy.beginTrip(trip.getFeatures(), battery.createClone(), cap
		.createClone());
	int i = 0;
	policy.parseTrip(trip);
	for (PointFeatures point : trip.getPoints()) {
	    PowerFlows pf = policy.calculatePowerFlows(point, i);
	    i++;
	   // pf.adjust(point.getPowerDemand());
	    battery.drawPower(pf.getBatteryToCapacitor()
		    + pf.getBatteryToMotor(), point.getPeriodMS());
	    cap.drawPower(pf.getCapacitorToMotor()
		    - pf.getBatteryToCapacitor(), point.getPeriodMS());
	}
	policy.endTrip(trip);
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
