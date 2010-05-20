package chargecar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import chargecar.util.GPXTripParser;
import chargecar.util.PointFeatures;
import chargecar.util.PowerFlowException;
import chargecar.util.PowerFlows;
import chargecar.util.SimulationResults;
import chargecar.util.Trip;
import chargecar.util.TripFeatures;
import chargecar.visualization.ConsoleWriter;
import chargecar.visualization.Visualizer;
import chargecar.battery.BatteryModel;
import chargecar.battery.SimpleBattery;
import chargecar.battery.SimpleCapacitor;
import chargecar.policies.*;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public class Simulator {
	static Visualizer visualizer = new ConsoleWriter();
	static int carMass = 1200;
	/**
	 * @param args
	 * @throws IOException 
	 */
	
	public static void main(String[] args) throws IOException 
	{		
		String gpxFolder = args[0];
		File folder = new File(gpxFolder);
		List<File> gpxFiles = getGPXFiles(folder);
		
		List<Policy> policies = new ArrayList<Policy>();
		policies.add(new NoCapPolicy());
		policies.add(new NaiveBufferPolicy());
		policies.add(new SpeedPolicy());		
		
		for(Policy p:policies)
		{
			p.loadState();
		}
		
		List<SimulationResults> results = simulateTrips(policies, gpxFiles);
		
		visualizer.visualizeSummary(results);
	}

	private static List<SimulationResults> simulateTrips(List<Policy> policies, List<File> tripFiles) throws IOException
	{
		List<SimulationResults> results = new ArrayList<SimulationResults>();
		for(Policy p:policies){
			results.add(new SimulationResults(p.getName()));
		}
		for(File tripFile : tripFiles)
		{
				List<Trip> tripsToTest = parseTrips(tripFile);
				for(Trip t: tripsToTest){
					for(int i=0;i<policies.size();i++){
						try {
							simulateTrip(policies.get(i),t,results.get(i));
						} catch (PowerFlowException e) {
							e.printStackTrace();
						}
					}
				}
						
		}
		return results;		
	}

	private static void simulateTrip(Policy policy, Trip trip, SimulationResults results) throws PowerFlowException
	{
		BatteryModel tripBattery = new SimpleBattery(Double.MAX_VALUE, Double.MAX_VALUE);
		BatteryModel tripCap = new SimpleCapacitor(50, 0);	
		simulate(policy, trip, tripBattery, tripCap);
		results.addTrip(trip, tripBattery, tripCap);
	}
	private static void simulate(Policy policy, Trip trip, BatteryModel battery, BatteryModel cap) throws PowerFlowException 
	{
		policy.beginTrip(trip.getFeatures(),battery.createClone(),cap.createClone());
		for(PointFeatures point : trip.getPoints())
		{
			PowerFlows pf = policy.calculatePowerFlows(point);
			pf.adjust(point.getPowerDemand());
			battery.drawCurrent(pf.getBatteryToCapacitor() + pf.getBatteryToMotor(), point);
			cap.drawCurrent(pf.getCapacitorToMotor() - pf.getBatteryToCapacitor(), point);
		}
		policy.endTrip();
	}
	
	private static List<Trip> parseTrips(File gpxFile) throws IOException 
	{		
		List<Trip> trips = new ArrayList<Trip>();
		GPXTripParser gpxparser = new GPXTripParser();		
		for(List<PointFeatures> tripPoints : gpxparser.read(gpxFile, carMass)){
			String driverName = gpxFile.getParentFile().getName();
			TripFeatures tf = new TripFeatures(driverName,carMass,tripPoints.get(0));
			trips.add(new Trip(tf, tripPoints));
			gpxparser.clear();
		}
		return trips;

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
}
