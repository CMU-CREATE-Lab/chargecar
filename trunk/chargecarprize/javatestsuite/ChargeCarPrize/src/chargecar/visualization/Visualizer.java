/**
 * 
 */
package chargecar.visualization;

import java.util.List;

import chargecar.battery.BatteryModel;
import chargecar.capacitor.CapacitorModel;
import chargecar.util.Trip;

/**
 * @author astyler
 *
 */
public interface Visualizer {
	public void visualizeTrip(Trip trip, BatteryModel battery, CapacitorModel capacitor);
	public void visualizeTrips(List<Trip> trips, List<BatteryModel> batteries, List<CapacitorModel> capacitors);
	public void visualizeSummary(List<Trip> trips, List<BatteryModel> batteries, List<CapacitorModel> capacitors);
}
