package chargecar;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import chargecar.util.GPXTripParser;
import chargecar.util.PointFeatures;
import chargecar.util.PowerFlows;
import chargecar.util.SimulationResults;
import chargecar.util.Trip;
import chargecar.util.TripFeatures;
import chargecar.visualization.ConsoleWriter;
import chargecar.visualization.Visualizer;
import chargecar.battery.BatteryModel;
import chargecar.battery.SimpleBattery;
import chargecar.capacitor.CapacitorModel;
import chargecar.capacitor.SimpleCapacitor;
import chargecar.policies.*;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public class Simulator {
	static Visualizer visualizer = new ConsoleWriter();
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	
	public static void main(String[] args) throws IOException 
	{		
		String gpxFolder = args[0];
		double carMass = 1200;
		
		List<Trip> tripsToTest = new ArrayList<Trip>();
		
		Policy noCapBaseline = new NoCapPolicy();
		Policy userPolicy = new NaiveBufferPolicy();		
		userPolicy.loadState();		
		
		parseTrips(gpxFolder, carMass, tripsToTest);

		System.out.println("Testing "+tripsToTest.size()+" trips.");
		
		SimulationResults baselineResults = simulateTripsNaive(noCapBaseline, tripsToTest);
		SimulationResults userResults = simulateTripsNaive(userPolicy, tripsToTest);
		
		visualizer.visualizeSummary(userResults, baselineResults);
	}

	private static void parseTrips(String gpxFolder, double carMass, List<Trip> tripsToTest) throws IOException 
	{
		File folder = new File(gpxFolder);
		List<File> gpxFiles = getGPXFiles(folder);		
		GPXTripParser gpxparser = new GPXTripParser();	

		for(File gpxFile:gpxFiles)
		{			
			for(List<PointFeatures> tripPoints : gpxparser.read(gpxFile, carMass)){
				String driverName = gpxFile.getParentFile().getName();
				TripFeatures tf = new TripFeatures(driverName,carMass,tripPoints.get(0));
				tripsToTest.add(new Trip(tf, tripPoints));
			}
		}
	}
		
	private static List<File> getGPXFiles(File gpxFolder) 
	{		
		List<File> gpxFiles = new ArrayList<File>();
		File[] files = gpxFolder.listFiles();
		for(File f: files)
		{
			if(f.isDirectory())
			{
				gpxFiles.addAll(getGPXFiles(f));
			}
			else if(f.isFile() && (f.getAbsolutePath().endsWith("gpx") || f.getAbsolutePath().endsWith("GPX")) )
			{
				gpxFiles.add(f);
			}		
		}
		return gpxFiles;	
	}

	private static SimulationResults simulateTripsNaive(Policy policy, List<Trip> trips)
	{
		List<BatteryModel> tripBatteries = new ArrayList<BatteryModel>();
		List<CapacitorModel> tripCapacitors = new ArrayList<CapacitorModel>();
		SimulationResults results = new SimulationResults();
		for(Trip trip : trips)
		{
				BatteryModel tripBattery = new SimpleBattery();
				CapacitorModel tripCap = new SimpleCapacitor(50);				
				simulateTrip(policy, trip, tripBattery, tripCap);				
				results.addTrip(trip, tripBattery, tripCap);
		}
		return results;		
	}

	private static void simulateTrip(Policy policy, Trip trip, BatteryModel battery, CapacitorModel cap) 
	{
		policy.beginTrip(trip.getFeatures(),battery.createClone(),cap.createClone());
		for(PointFeatures point : trip.getPoints())
		{
			PowerFlows pf = policy.calculatePowerFlows(point);
			battery.drawCurrent(pf.getBatteryToCapacitor() + pf.getBatteryToMotor(), point);
			cap.drawCurrent(pf.getCapacitorToMotor() - pf.getBatteryToCapacitor(), point);
		}
		policy.endTrip();
	}
}
