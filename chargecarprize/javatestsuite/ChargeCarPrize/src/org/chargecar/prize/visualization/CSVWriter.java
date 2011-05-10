package org.chargecar.prize.visualization;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.util.DriverResults;
import org.chargecar.prize.util.SimulationResults;
import org.chargecar.prize.util.Trip;

/**
 * DO NOT EDIT
 * 
 * A simple visualizer that outputs simulation details to the console screen.
 * 
 * @author Alex Styler
 */
public class CSVWriter implements Visualizer {
    private final DecimalFormat d = new DecimalFormat("0.000E0");
    private final DecimalFormat p = new DecimalFormat("0.000%");
    private final String filename;
    
    public CSVWriter(String filename){
	this.filename = filename;
    }
    
    public void visualizeSummary(List<SimulationResults> results) {
	FileWriter fstream;
	try {
	    fstream = new FileWriter(filename);
	    BufferedWriter out = new BufferedWriter(fstream);
	    for (SimulationResults r : results) {
		double currentSquaredSum = 0.0;
		for (Double d : r.getBatteryCurrentSquaredIntegrals()) {
		    currentSquaredSum += d;
		    out.write(currentSquaredSum+",");
		}
		out.write("0.0\n");
		double chargeSpentSum = 0.0;
		for (Double d : r.getChargeSpent()) {
		    chargeSpentSum += d;
		    out.write(chargeSpentSum+",");
		}
		out.write("0.0\n");
		double ahServedSum = 0.0;
		for (Double d : r.getAmpHoursServedSums()) {
		    ahServedSum += d;
		    out.write(ahServedSum+",");
		}
		out.write("0.0\n");
	    }
	    out.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    public void visualizeTrip(Trip trip, BatteryModel battery,
	    BatteryModel capacitor) {
	System.out.println(trip);
	System.out.println("Integral of current squared: "
		+ d.format(battery.getCurrentSquaredIntegral()));
    }
    
    public void visualizeTrips(SimulationResults simResults) {

    }

    @Override
    public void visualizeDrivers(List<DriverResults> driverResults) {
	// TODO Auto-generated method stub
	
    }
}
