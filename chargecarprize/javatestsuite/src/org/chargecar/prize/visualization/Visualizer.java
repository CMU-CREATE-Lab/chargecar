/**
 * 
 */
package org.chargecar.prize.visualization;

import java.util.List;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.util.DriverResults;
import org.chargecar.prize.util.SimulationResults;
import org.chargecar.prize.util.Trip;


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
    
    public void visualizeDrivers(List<DriverResults> driverResults);
    
    public void visualizeTrips(List<SimulationResults> results);
}
