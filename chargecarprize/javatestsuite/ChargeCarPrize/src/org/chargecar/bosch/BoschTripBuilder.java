package org.chargecar.bosch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.Vehicle;

/**
 * DO NOT EDIT
 * 
 * Used by GPX parsers to build the trips using the power+physics model for the
 * 2006 Accord.
 * 
 * @author Alex Styler
 * @author Matt Deuscher
 * @author Paul Dille
 * @author Jacob Katz
 * 
 */
public class BoschTripBuilder {
    public static List<PointFeatures> calculateTrip(List<Calendar> times,
	    List<Double> vels, Vehicle vehicle) throws IOException {
	List<PointFeatures> tripPoints = new ArrayList<PointFeatures>(times
		.size());
	runPowerModel(tripPoints, times, vels, vehicle);
	return tripPoints;	
    }
    
    private static void runPowerModel(List<PointFeatures> tripPoints,
	    List<Calendar> times, List<Double> vels, Vehicle vehicle) throws IOException {
	List<Double> speeds = new ArrayList<Double>();
	List<Double> accelerations = new ArrayList<Double>();
	List<Double> powerDemands = new ArrayList<Double>();
	
	speeds.add(0.0);
	accelerations.add(0.0);
	
	for (int i = 1; i < times.size(); i++) {
	    long msDiff = (times.get(i).getTimeInMillis() - times.get(i - 1).getTimeInMillis());
	    if (msDiff == 0) {
		break;
	    }

	    speeds.add(vels.get(i));
	    
	    accelerations.add(1000.0 * (speeds.get(i) - speeds.get(i - 1))
		    / msDiff);
	}
	speeds.set(0, speeds.get(1));
	accelerations.set(1, 0.0);
	
	final double carMassKg = vehicle.getMass();
	final double aGravity = 9.81;
	final double offset = -0.35;
	final double ineff = 1 / 0.85;
	final double regenEff = 0.35;
	final double def_elevation = 0;//sealevel assumption 
	
	final double outsideTemp = ((60 + 459.67) * 5 / 9);// 60F to kelvin
	
	for (int i = 0; i < accelerations.size(); i++) {
	    double pressure = 101325 * Math.pow(
		    (1 - ((0.0065 * def_elevation) / 288.15)),
		    ((aGravity * 0.0289) / (8.314 * 0.0065)));
	    double rho = (pressure * 0.0289) / (8.314 * outsideTemp);
	    double airResCoeff = 0.5 * rho * vehicle.getCarCrossArea() * vehicle.getCarDragCoeff();
	    
	    double theta = 0;
	    
	    double mgsintheta = carMassKg*aGravity*Math.sin(theta);
	    double mgcostheta = carMassKg*aGravity*Math.cos(theta);
	    
	    //Rolling resistance dependent on the normal force
	    double rollingRes = vehicle.getRollingResCoeff() * mgcostheta;
	    
	    double airRes = airResCoeff * speeds.get(i) * speeds.get(i);
	    //Total force on the car fNet = ma = fMotor - airRes - rollingres - mgsintheta
	    double fNet = carMassKg * accelerations.get(i);  
	    //Define fR to be sum of other forces
	    double fRes = airRes + rollingRes + mgsintheta;
	    double fMotor = fNet + fRes;
	    double pwr = 0.0;
	    double speed = speeds.get(i);
	    if (fMotor > 0){ //Motor is applying power to the vehicle
		pwr = fMotor * speed * ineff;
	    } else { //Assume regen when motor force is negative
		pwr = regenEff*fMotor*speed;		
	    }
	    
	    pwr = ((pwr / -1000.0) + offset);
	    
	    if (speed > 12.0) {
		pwr = ((pwr - (0.056 * (speed * speed))) + (0.68 * speed));
	    }
	    
	    powerDemands.add(pwr * 1000.0);// convert back to watts
	    
	}
	//System.out.println("Times: "+times.size()+" Powers: "+powerDemands.size()+" Speeds: "+speeds.size()+" Accelerations: "+accelerations.size());
	for (int i = 1; i < times.size(); i++) {
	    int periodMS = (int) (times.get(i).getTimeInMillis() - times.get(i - 1).getTimeInMillis());
	    tripPoints.add(new PointFeatures(0, 0, 0, 0.0, 0, accelerations.get(i), speeds.get(i), powerDemands.get(i), periodMS, times.get(i - 1)));
	}
	PointFeatures endPoint = new PointFeatures(0.0, 0.0, 0.0, 0.0,0.0, 0.0, 0.0, 0.0, 1000, times.get(times.size() - 1));
	tripPoints.add(endPoint);
	
    }   
}
