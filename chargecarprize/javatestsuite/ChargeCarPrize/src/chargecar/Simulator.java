package chargecar;

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

	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		List<Trip> tripsToTest = new ArrayList<Trip>();
		Policy noCapBaseline = new NoCapPolicy();
		Policy naiveBaseline = new NaiveBufferPolicy();
		Policy userPolicy = new UserPolicy();
		userPolicy.loadState();
		
		double carMass = 1200;
		
		GPXTripParser gpxparser = new GPXTripParser();
		
		try {
			for(List<PointFeatures> tripPoints : gpxparser.read(args[0], carMass)){
				TripFeatures tf = new TripFeatures(args[1],carMass,tripPoints.get(0));
				tripsToTest.add(new Trip(tf, tripPoints));
			}
		} catch (IOException e) {			
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Testing "+tripsToTest.size()+" trips.");
		
		for(Trip t:tripsToTest){
			debugTrip(t);
		}		
		
		BatteryModel judgingBattery = new SimpleBattery();
		CapacitorModel judgingCap = new SimpleCapacitor(50);
		//SimulationResults userResults = simulateTripsNaive(userPolicy, tripsToTest, judgingBattery, judgingCap);
		SimulationResults noCapResults = simulateTripsNaive(noCapBaseline, tripsToTest, judgingBattery, judgingCap);
		SimulationResults naiveResults = simulateTripsNaive(naiveBaseline, tripsToTest,judgingBattery, judgingCap);
		
		ConsoleWriter writer = new ConsoleWriter();
		System.out.println("NO CAP");
		writer.visualizeTrips(noCapResults);
		System.out.println("NAIVE");
		writer.visualizeTrips(naiveResults);
		}
		
	private static void debugTrip(Trip trip) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
		System.out.println("==================");
		System.out.println("Debug: "+trip);
		List<PointFeatures> pfs = trip.getPoints();
		System.out.println("Point count:" + pfs.size());
		System.out.println("Start time: " + sdf.format(pfs.get(0).getTime().getTime()));
		System.out.println("End time: " + sdf.format(pfs.get(pfs.size()-1).getTime().getTime()));
		int periodMax = pfs.get(0).getPeriodMS();
		int periodMin = periodMax;
		double speedMax = pfs.get(0).getSpeed();
		double speedMin = speedMax;
		double powerMax = pfs.get(0).getPowerDemand();
		double powerMin = powerMax;
		
		for(PointFeatures pf : pfs){
			if(pf.getPeriodMS() > periodMax)
				periodMax = pf.getPeriodMS();
			if(pf.getPeriodMS() < periodMin)
				periodMin = pf.getPeriodMS();
			if(pf.getSpeed() > speedMax)
				speedMax = pf.getSpeed();
			if(pf.getSpeed() < speedMin)
				speedMin = pf.getSpeed();
			if(pf.getPowerDemand() > powerMax)
				powerMax = pf.getPowerDemand();
			if(pf.getPowerDemand() < powerMin)
				powerMin = pf.getPowerDemand();
		}
		System.out.println("Period range: "+periodMin+" to "+periodMax);
		System.out.println("Speed range: "+speedMin+" to "+speedMax);
		System.out.println("Power range: "+powerMin+" to "+powerMax);
		System.out.println("==================");
		
		
	}

	private static SimulationResults simulateTripsNaive(Policy policy, List<Trip> trips, BatteryModel battery, CapacitorModel cap){
		List<BatteryModel> tripBatteries = new ArrayList<BatteryModel>();
		List<CapacitorModel> tripCapacitors = new ArrayList<CapacitorModel>();		
		for(Trip trip : trips){
				BatteryModel tripBattery = battery.createClone();
				CapacitorModel tripCap = cap.createClone();				
				simulateTrip(policy, trip, tripBattery, tripCap);				
				tripBatteries.add(tripBattery);
				tripCapacitors.add(tripCap);
		}
		return new SimulationResults(trips,tripBatteries,tripCapacitors);//return both for visualizer		
	}

	private static void simulateTrip(Policy policy, Trip trip, BatteryModel battery, CapacitorModel cap) {
		policy.beginTrip(trip.getFeatures(),battery.createClone(),cap.createClone());
		for(PointFeatures point : trip.getPoints()){
			PowerFlows pf = policy.calculatePowerFlows(point);
			battery.drawCurrent(pf.getBatteryToCapacitor() + pf.getBatteryToMotor(), point);
			cap.drawCurrent(pf.getCapacitorToMotor() - pf.getBatteryToCapacitor(), point);
		}
		policy.endTrip();
	}

}
