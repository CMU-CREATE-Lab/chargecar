/**
 * 
 */
package chargecar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import chargecar.util.PointFeatures;
import chargecar.util.PowerFlows;
import chargecar.util.Trip;
import chargecar.battery.BatteryModel;
import chargecar.battery.NaiveBattery;
import chargecar.capacitor.CapacitorModel;
import chargecar.capacitor.NaiveCapacitor;
import chargecar.policies.Policy;
import chargecar.policies.PolicyFactory;

/**
 * @author Alex Styler
 *
 */
public class Simulator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BatteryModel battery;
		CapacitorModel capacitor;
		Policy noCapBaseline;
		Policy naiveBaseline;
		Policy userPolicy;
		
		List<Trip> tripsToTest = new ArrayList<Trip>();

		
		}
		
	List<BatteryModel> simulateTripsNaive(Policy policy, List<Trip> trips){
		List<BatteryModel> tripBatteries = new ArrayList<BatteryModel>();
		List<CapacitorModel> tripCapacitors = new ArrayList<CapacitorModel>();
			for(Trip trip : trips){
				BatteryModel battery = new NaiveBattery();
				CapacitorModel cap = new NaiveCapacitor();
				policy.beginTrip(trip.getFeatures());
				
				for(PointFeatures point : trip.getPoints()){
					PowerFlows pf = policy.calculatePowerFlows(point);
					//point.validate(pf);
					battery.drawCurrent(0.0, 1.0);//btom+btoc
					//cap.drawCurrent(0.0,1.0);//ctom-btoc
				}
				tripBatteries.add(battery);
				tripCapacitors.add(cap);
		}
			return tripBatteries;//return both for visualizer
		
		
	}

}
