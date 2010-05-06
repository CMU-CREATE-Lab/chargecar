package chargecar.util;

import java.util.List;

import chargecar.battery.BatteryModel;
import chargecar.capacitor.CapacitorModel;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public class SimulationResults {
	private List<BatteryModel> batteries;
	private List<CapacitorModel> capacitors;
	private List<Trip> trips;
	public List<BatteryModel> getBatteries() {
		return batteries;
	}
	public List<CapacitorModel> getCapacitors() {
		return capacitors;
	}
	public List<Trip> getTrips() {
		return trips;
	}
	public SimulationResults(List<Trip> trips,
			List<BatteryModel> tripBatteries, List<CapacitorModel> tripCapacitors) {
		super();
		this.batteries = tripBatteries;
		this.capacitors = tripCapacitors;
		this.trips = trips;
	}
	
	
}
