package chargecar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import chargecar.util.GPXTripParser;
import chargecar.util.PointFeatures;
import chargecar.util.PowerFlows;
import chargecar.util.SimulationResults;
import chargecar.util.Trip;
import chargecar.util.TripFeatures;
import chargecar.visualization.ConsoleWriter;
import chargecar.battery.BatteryModel;
import chargecar.battery.NaiveBattery;
import chargecar.capacitor.CapacitorModel;
import chargecar.capacitor.NaiveCapacitor;
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
		
		double carMass = 2500;
		
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
		BatteryModel judgingBattery = new NaiveBattery();
		CapacitorModel judgingCap = new NaiveCapacitor(50);
		//SimulationResults userResults = simulateTripsNaive(userPolicy, tripsToTest, judgingBattery, judgingCap);
		SimulationResults noCapResults = simulateTripsNaive(noCapBaseline, tripsToTest, judgingBattery, judgingCap);
		SimulationResults naiveResults = simulateTripsNaive(naiveBaseline, tripsToTest,judgingBattery, judgingCap);
		
		ConsoleWriter writer = new ConsoleWriter();
		System.out.println("NO CAP");
		writer.visualizeTrips(noCapResults);
		System.out.println("NAIVE");
		writer.visualizeTrips(naiveResults);
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
