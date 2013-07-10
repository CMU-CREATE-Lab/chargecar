package org.chargecar.experiments.thermal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.chargecar.algodev.controllers.MDPPolyTrainer;
import org.chargecar.algodev.controllers.MDPTrainer;
import org.chargecar.algodev.predictors.knn.KnnPoint;
import org.chargecar.algodev.predictors.knn.KnnTableTrainer;
import org.chargecar.prize.battery.SimpleCapacitor;
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
public class ThermGraphTrainer {
    static Vehicle civic = new Vehicle(1200, 1.988, 0.31, 0.015);
    static double systemVoltage = 120;
    
    static double[] temps = new double[]{30,31,32,33,34,35,36,37,38,39,40,41}; 
    static double[] powers = new double[51];;
    static double[] massFlows = new double[]{0,0.001,0.0015,0.002,0.0025,0.003,0.0035,0.0042};
    
    /**
     * @param args
     *            A pathname to a GPX file or folder containing GPX files (will
     *            be recursively traversed)
     *            Alternate policies to test, either in a referenced JAR file or 
     *            within the project
     *        	  e.g. java Simulator "C:\testdata\may" org.chargecar.policies.SpeedPolicy
     *        	       java Simulator "C:\testdata" NaiveBufferPolicy SpeedPolicy
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
	if (args == null || args.length < 1) {
	    System.err.println("ERROR: No GPX directory path provided.");
	    System.exit(1);
	}
	
	powers = new double[51];
	double pInit = -45078;
	double pDiff = 3563.6;
	for(int i = 0;i<51;i++){
	    if(i == 13){
		powers[i] = 0;
	    }
	    else if(i > 13){
		powers[i] = pInit + pDiff*(i-1);
	    }
	    else{
		powers[i] = pInit + pDiff*i;
	    }
	    
	}
	
	String dynFilePath = args[2];
	FileInputStream fis = new FileInputStream(new File(dynFilePath));
	ObjectInputStream ois = new ObjectInputStream(fis);
	double[][][] dynamics = (double[][][])ois.readObject(); 
	ois.close();
	ThermalBattery theBatt = new ThermalBattery(30, temps,powers,massFlows, dynamics);
	
	String gpxFolder = args[0];	
	String optFolder = args[1];
	File folder = new File(gpxFolder);
	List<File> gpxFilesT = getGPXFiles(folder);
	List<File> gpxFiles = new ArrayList<File>(gpxFilesT.size());
	for(int i = gpxFilesT.size() - 1; i >= 0; i--){
	    gpxFiles.add(gpxFilesT.get(i));
	}
	
	System.out.println("Training on "+gpxFiles.size()+" GPX files.");
	
	
	int count = 0;
	for (File tripFile : gpxFiles) {
	    List<Trip> tripsToTest = parseTrips(tripFile);
	    for (Trip t : tripsToTest) {
		ThermalValueGraph tvg = new ThermalValueGraph(temps, massFlows, 0.99, theBatt); 		
		double[][] values = tvg.getValues(t.getPoints());		
		writeTrip(t,values,optFolder,count);
		count++;
	    }
	}
	
	System.out.println("Complete. Trips trained on: "+count);
    }    
    
    public static void writeTrip(Trip t, double[][] vg, String optFolder, int num){  	
	File powerFile = new File(optFolder+"/trip"+num+".pow");
	File vgFile = new File(optFolder+"/trip"+num+".tvg");
	writeTripPowers(t.getPoints(),powerFile);
	writeValueGraph(vg,vgFile);	
      }
      
    public static void writeTripPowers(List<PointFeatures> pfs, File powerFile) {
   	FileWriter fstream;
   	try {
   	    powerFile.getParentFile().mkdirs();
   	    //powerFile.createNewFile();
   	    fstream = new FileWriter(powerFile);
   	    BufferedWriter out = new BufferedWriter(fstream);
   	    for(PointFeatures pf : pfs){
   		out.write(pf.getTime().getTimeInMillis()/1000+",");   		
   		out.write(pf.getPowerDemand()+"\n");
   	    }
   	    out.close();
   	} catch (IOException e) {
   	    // TODO Auto-generated catch block
   	    e.printStackTrace();
   	}
    }
    
    public static void writeValueGraph(double[][] vg, File vgFile) {
	FileWriter fstream;
   	try {
   	    vgFile.getParentFile().mkdirs();
   	    //vgFile.createNewFile();
   	    fstream = new FileWriter(vgFile);
   	    BufferedWriter out = new BufferedWriter(fstream);
   	    for(int x = 0; x < vg.length ; x++){
   		out.write(vg[x][0]+"");
   		for(int y=1;y < vg[x].length;y++){
   		    out.write(","+vg[x][y]); 
   		}
   		out.write("\n");   		
   	    }
   	    out.close();
   	} catch (IOException e) {
   	    // TODO Auto-generated catch block
   	    e.printStackTrace();
   	}
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
