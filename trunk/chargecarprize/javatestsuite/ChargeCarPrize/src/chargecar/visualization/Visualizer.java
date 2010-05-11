/**
 * 
 */
package chargecar.visualization;

import java.util.List;

import chargecar.battery.BatteryModel;
import chargecar.capacitor.CapacitorModel;
import chargecar.util.SimulationResults;
import chargecar.util.Trip;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public interface Visualizer {
	public void visualizeTrip(Trip trip, BatteryModel battery, CapacitorModel capacitor);
	public void visualizeTrips(SimulationResults simResults);
	public void visualizeSummary(SimulationResults simResults, SimulationResults baseline);
}
