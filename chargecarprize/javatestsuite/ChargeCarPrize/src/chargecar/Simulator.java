package chargecar;

import java.util.ArrayList;
import java.util.List;

import chargecar.util.PointFeatures;
import chargecar.util.PowerFlows;
import chargecar.util.Trip;
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
		
		BatteryModel judgingBattery = new NaiveBattery();
		CapacitorModel judgingCap = new NaiveCapacitor(50);
		List<BatteryModel> userBatteries = simulateTripsNaive(userPolicy, tripsToTest, judgingBattery, judgingCap);
		List<BatteryModel> noCapBatteries = simulateTripsNaive(noCapBaseline, tripsToTest, judgingBattery, judgingCap);
		List<BatteryModel> naiveBatteries = simulateTripsNaive(naiveBaseline, tripsToTest,judgingBattery, judgingCap);
		//TODO add capacitors
		//grab Results, visualize
		}
		
	private static List<BatteryModel> simulateTripsNaive(Policy policy, List<Trip> trips, BatteryModel battery, CapacitorModel cap){
		List<BatteryModel> tripBatteries = new ArrayList<BatteryModel>();
		List<CapacitorModel> tripCapacitors = new ArrayList<CapacitorModel>();		
		for(Trip trip : trips){
				BatteryModel tripBattery = battery.createClone();
				CapacitorModel tripCap = cap.createClone();				
				simulateTrip(policy, trip, tripBattery, tripCap);				
				tripBatteries.add(tripBattery);
				tripCapacitors.add(tripCap);
		}
		return tripBatteries;//return both for visualizer		
	}

	private static void simulateTrip(Policy policy, Trip trip, BatteryModel battery, CapacitorModel cap) {
		policy.beginTrip(trip.getFeatures(),battery.createClone(),cap.createClone());
		for(PointFeatures point : trip.getPoints()){
			PowerFlows pf = policy.calculatePowerFlows(point);
			battery.drawCurrent(pf.getBatteryToCapacitor() + pf.getBatteryToMotor(), point.getPeriodMS());
			cap.drawCurrent(pf.getCapacitorToMotor() - pf.getBatteryToCapacitor(), point.getPeriodMS());
		}
		policy.endTrip();
	}

}
