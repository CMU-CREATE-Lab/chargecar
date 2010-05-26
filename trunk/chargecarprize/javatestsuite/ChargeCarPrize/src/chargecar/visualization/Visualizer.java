/**
 * 
 */
package chargecar.visualization;

import java.util.List;

import chargecar.battery.BatteryModel;
import chargecar.util.SimulationResults;
import chargecar.util.Trip;

/**
 * DO NOT EDIT
 * 
 * An interface for visualizers. A visualizer displays the results of
 * simulation, either a detailed view for each trip, that can show temperature,
 * charge, current, etc.. over time (before the memory gets freed) or one that
 * simply displays the summary of simulation at the very end.
 * 
 * @author Alex Styler
 */
public interface Visualizer {
    public void visualizeTrip(Trip trip, BatteryModel battery,
	    BatteryModel capacitor);
    
    public void visualizeTrips(SimulationResults simResults);
    
    public void visualizeSummary(List<SimulationResults> simResults);
}
