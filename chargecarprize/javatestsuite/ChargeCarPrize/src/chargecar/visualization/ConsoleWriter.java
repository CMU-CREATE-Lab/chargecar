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
		String driver = trip.getFeatures().getDriver();
		String time = trip.getFeatures().getStartTime().getTime().toLocaleString();
		String lat = Double.toString(trip.getFeatures().getStartLat());
		String lon = Double.toString(trip.getFeatures().getStartLon());
		System.out.print("TRIP: "+driver + " at "+time+", from ("+lat+", "+lon+").");
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
