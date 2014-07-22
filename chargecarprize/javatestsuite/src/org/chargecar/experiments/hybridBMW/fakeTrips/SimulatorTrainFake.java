package org.chargecar.experiments.hybridBMW.fakeTrips;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.chargecar.experiments.hybridBMW.MDPTrainerHybrid;
import org.chargecar.prize.battery.SimpleBattery;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.Vehicle;

/**
 * DO NOT EDIT Runs the simulation of an electric car running over a commute
 * defined by GPX file from real world commutes. Uses a compound energy storage
 * Policy to decide whether to get/store power in either the capacitor or
 * battery inside the car.
 * 
 * Competitors need only modify UserPolicy with their algorithm.
 * 
 * @author Alex Styler
 * 
 */
public class SimulatorTrainFake {
    static Vehicle civic = new Vehicle(1200, 1.988, 0.31, 0.015);
    static double systemVoltage = 120;
    static int battWhr = 5000;

    /**
     * @param args
     *            A pathname to a GPX file or folder containing GPX files (will
     *            be recursively traversed)
     *            Alternate policies to test, either in a referenced JAR file or 
     *            within the project
     *        	  e.g. java Simulator "C:\testdata\may" org.chargecar.policies.SpeedPolicy
     *        	       java Simulator "C:\testdata" NaiveBufferPolicy SpeedPolicy
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	if (args == null || args.length < 1) {
	    System.err.println("ERROR: No Opt directory path provided.");
	    System.exit(1);
	}
	
	String optFolder = args[0];
	
	MDPTrainerHybrid trainer = new MDPTrainerHybrid(optFolder, new SimpleBattery(battWhr, 0, systemVoltage), 100);

	List<Trip> trips = getTrips();
	
	for(Trip t : trips){
	    trainer.parseTrip(t, true);
	}	
		
	trainer.finishTraining();
	System.out.println("Finished Training on "+trips.size()+" fake trip(s)");
    }

    private static List<Trip> getTrips() {
	List<Trip> trips = new ArrayList<Trip>();
	trips.add(FakeTripMaker.getTrip(civic,3));
	trips.add(FakeTripMaker.getTrip(civic,4));
	return trips;
    }    
}
