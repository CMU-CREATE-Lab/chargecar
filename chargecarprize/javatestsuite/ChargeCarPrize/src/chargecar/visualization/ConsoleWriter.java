package chargecar.visualization;

import java.util.List;

import chargecar.battery.BatteryModel;
import chargecar.capacitor.CapacitorModel;
import chargecar.util.Trip;

public class ConsoleWriter implements Visualizer {

	@Override
	public void visualizeSummary(List<Trip> trips,
			List<BatteryModel> batteries, List<CapacitorModel> capacitors) {
		// TODO Auto-generated method stub

	}

	@Override
	public void visualizeTrip(Trip trip, BatteryModel battery,
			CapacitorModel capacitor) {

	}

	@Override
	public void visualizeTrips(List<Trip> trips, List<BatteryModel> batteries,
			List<CapacitorModel> capacitors) {
		// TODO Auto-generated method stub

	}
	
	private Double currentSquaredIntegral(BatteryModel battery){
		return null;

	}

}
