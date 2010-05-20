/**
 * 
 */
package chargecar.visualization;

import java.util.List;

import chargecar.battery.BatteryModel;
import chargecar.util.SimulationResults;
import chargecar.util.Trip;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public interface Visualizer {
	public void visualizeTrip(Trip trip, BatteryModel battery, BatteryModel capacitor);
	public void visualizeTrips(SimulationResults simResults);
	public void visualizeSummary(List<SimulationResults> simResults);
}
