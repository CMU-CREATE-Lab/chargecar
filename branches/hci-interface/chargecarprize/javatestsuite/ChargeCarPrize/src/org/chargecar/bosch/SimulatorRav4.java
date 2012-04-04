package org.chargecar.bosch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.battery.LeadAcidBattery;
import org.chargecar.prize.battery.LiFePo4;
import org.chargecar.prize.battery.SimpleBattery;
import org.chargecar.prize.battery.SimpleCapacitor;
import org.chargecar.prize.policies.NoCapPolicy;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.DriverResults;
import org.chargecar.prize.util.GPXTripParser;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.SimulationResults;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripBuilder2;
import org.chargecar.prize.util.TripFeatures;
import org.chargecar.prize.util.Vehicle;
import org.chargecar.prize.visualization.CSVWriter;
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
public class SimulatorRav4 {
   // static Visualizer visualizer = new CSVWriter("C:/final.csv");
    static Visualizer visualizer = new ConsoleWriter();
    static Vehicle rav4 = new Vehicle(1690, 2.3, 0.28, 0.010);
    
    static double systemVoltage = 320;
    static double batteryWhr = 50000;
    static double capWhr = 0;
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
	    System.err.println("ERROR: No Rav4 File path provided.");
	    System.exit(1);
	}
	
	String rav4File = args[0];
	File file = new File(rav4File);
	BufferedReader reader = new BufferedReader(new FileReader(rav4File));
	String line = null;
	List<Double> lats = new ArrayList<Double>();
	List<Double> lons = new ArrayList<Double>();
	List<Double> eles = new ArrayList<Double>();
	List<Calendar> times = new ArrayList<Calendar>();
	System.out.println("Reading data file...");
	int index = 0;
	while((line = reader.readLine()) != null){
	    if(index % 100 == 0)
		System.out.println("Processing Point: "+index);
	    StringTokenizer st = new StringTokenizer(line,",");
	    times.add(getCalTime(st.nextToken()));
	    lats.add(convertLat(st.nextToken()));
	    lons.add(convertLon(st.nextToken()));
	    eles.add(Double.parseDouble(st.nextToken())/3.28);	  
	    index++;
	}
	System.out.println("Building Trip...");
	List<PointFeatures> pfs = TripBuilder2.calculateTrip(times, lats, lons, eles, rav4);
	System.out.println("Writing File...");
	CSVWriter w = new CSVWriter("C:/rav4out.csv");
	w.writeTripPowers(pfs);
	System.out.println("Complete");
	
    }
    
    private static double convertLat(String lat){
	double deg = Integer.parseInt(lat.substring(0, 2));
	double minutes = Double.parseDouble(lat.substring(2));
	
	return deg + minutes/60;
    }
    private static double convertLon(String lon){
	double deg = Integer.parseInt(lon.substring(0, 3));
	double minutes = Double.parseDouble(lon.substring(3));
	
	return deg + minutes/60;
    }
    
    private static Calendar getCalTime(String incTime){
	int hour = Integer.parseInt(incTime.substring(0, 2));
	int minute = Integer.parseInt(incTime.substring(2,4));
	int second = Integer.parseInt(incTime.substring(4,6));
	int year = 2011;
	int month = 7;
	int day = 22;
	Calendar calTime = Calendar.getInstance();
	calTime.setTimeInMillis(0);
	calTime.set(year, month, day, hour, minute, second);
	return calTime;	
    }
     
}
