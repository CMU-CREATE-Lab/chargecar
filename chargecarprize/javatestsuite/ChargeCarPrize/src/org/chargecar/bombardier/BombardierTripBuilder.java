package org.chargecar.bombardier;

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
public class BombardierTripBuilder {
    public static List<PointFeatures> calculateTrip(List<Calendar> times,
	    List<Double> xpos, List<Double> ypos, List<Double> eles,
	    Vehicle vehicle) throws IOException {
	List<PointFeatures> tripPoints = new ArrayList<PointFeatures>(times
		.size());
	runPowerModel(tripPoints, times, xpos, ypos, eles, vehicle);
	return tripPoints;	
    }
    
    private static void runPowerModel(List<PointFeatures> tripPoints,
	    List<Calendar> times, List<Double> xpos, List<Double> ypos,
	    List<Double> eles, Vehicle vehicle) throws IOException {
	FileWriter fstream = new FileWriter("C:/out.csv");
	BufferedWriter out = new BufferedWriter(fstream);
	
	List<Double> planarDistances = new ArrayList<Double>();
	List<Double> adjustedDistances = new ArrayList<Double>();
	List<Double> speeds = new ArrayList<Double>();
	List<Double> accelerations = new ArrayList<Double>();
	List<Double> powerDemands = new ArrayList<Double>();
	
	planarDistances.add(0.0);
	adjustedDistances.add(0.0);
	speeds.add(0.0);
	accelerations.add(0.0);
	
	for (int i = 1; i < times.size(); i++) {
	    long msDiff = (times.get(i).getTimeInMillis() - times.get(i - 1).getTimeInMillis());
	    if (msDiff == 0) {
		break;
	    }
	    double eleDiff = eles.get(i) - eles.get(i - 1);
	    double tempDist = Math.sqrt(Math.pow(xpos.get(i-1)-xpos.get(i),2)+Math.pow(ypos.get(i)-ypos.get(i-1), 2));
	    
	    planarDistances.add(tempDist);
	    tempDist = Math.sqrt((tempDist * tempDist) + (eleDiff * eleDiff));
	    adjustedDistances.add(tempDist);
	    double tempSpeed = 1000.0 * tempDist / msDiff;
	    
	    if (tempDist < 1E-6) {
		speeds.add(0.0);
	    } else {
		speeds.add(tempSpeed);
	    }
	    accelerations.add(1000.0 * (speeds.get(i) - speeds.get(i - 1))
		    / msDiff);
	}
	speeds.set(0, speeds.get(1));
	accelerations.set(1, 0.0);
	
	final double carMassKg = vehicle.getMass();
	final double aGravity = 9.81;
	final double offset = -3.5;
	final double ineff = 1 / 1;
	final double regenEff = 0.55;
	
	final double outsideTemp = ((60 + 459.67) * 5 / 9);// 60F to kelvin
	
	for (int i = 0; i < accelerations.size(); i++) {
	    double pressure = 101325 * Math.pow(
		    (1 - ((0.0065 * eles.get(i)) / 288.15)),
		    ((aGravity * 0.0289) / (8.314 * 0.0065)));
	    double rho = (pressure * 0.0289) / (8.314 * outsideTemp);
	    double airResCoeff = 0.5 * rho * vehicle.getCarCrossArea() * vehicle.getCarDragCoeff();
	    
	    double theta = 0;
	    if (i > 0) {
		final double eleDiff = eles.get(i) - eles.get(i - 1);
		
		if (planarDistances.get(i) < 1E-6) {  //Small distance (want to avoid infinite slope)
		    theta = 0;
		} else if (Math.abs(speeds.get(i)) < 0.50) { //Slow speed
		    theta = 0;
		} else if (eleDiff != 0) { //All other cases, except no elevation change
		    theta = Math.atan(eleDiff / planarDistances.get(i));		   
		}
	    }
	    
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
	
	for (int i = 1; i < times.size(); i++) {
	    out.write(times.get(i-1).getTimeInMillis()+","+xpos.get(i-1)+","+speeds.get(i)+","+accelerations.get(i)+","+powerDemands.get(i)/-100.0+"\n");
	    int periodMS = (int) (times.get(i).getTimeInMillis() - times.get(i - 1).getTimeInMillis());
	    tripPoints.add(new PointFeatures(xpos.get(i - 1), ypos.get(i - 1),
		    eles.get(i - 1), planarDistances.get(i), accelerations
		    .get(i), speeds.get(i), powerDemands.get(i),
		    periodMS, times.get(i - 1)));
	}
	PointFeatures endPoint = new PointFeatures(xpos.get(xpos.size() - 1),
		ypos.get(ypos.size() - 1), eles.get(eles.size() - 1), 0.0, 0.0,
		0.0, 0.0, 1000, times.get(times.size() - 1));
	tripPoints.add(endPoint);
	out.close();
	
    }   
    private static void runPowerModel2(List<PointFeatures> tripPoints,
	    List<Calendar> times, List<Double> xpos, List<Double> ypos,
	    List<Double> eles, Vehicle vehicle) throws IOException {
	FileWriter fstream = new FileWriter("C:/out.csv");
	BufferedWriter out = new BufferedWriter(fstream);
	
	List<Double> planarDistances = new ArrayList<Double>();
	List<Double> adjustedDistances = new ArrayList<Double>();
	List<Double> speeds = new ArrayList<Double>();
	List<Double> accelerations = new ArrayList<Double>();
	List<Double> powerDemands = new ArrayList<Double>();
	
	planarDistances.add(0.0);
	adjustedDistances.add(0.0);
	speeds.add(0.0);
	accelerations.add(0.0);
	
	for (int i = 1; i < times.size(); i++) {
	    long msDiff = (times.get(i).getTimeInMillis() - times.get(i - 1).getTimeInMillis());
	    if (msDiff == 0) {
		break;
	    }
	    double eleDiff = eles.get(i) - eles.get(i - 1);
	    double tempDist = Math.sqrt(Math.pow(xpos.get(i-1)-xpos.get(i),2)+Math.pow(ypos.get(i)-ypos.get(i-1), 2));
	    
	    planarDistances.add(tempDist);
	    tempDist = Math.sqrt((tempDist * tempDist) + (eleDiff * eleDiff));
	    adjustedDistances.add(tempDist);
	    double tempSpeed = 1000.0 * tempDist / msDiff;
	    
	    if (tempDist < 1E-6) {
		speeds.add(0.0);
	    } else {
		speeds.add(tempSpeed);
	    }
	    accelerations.add(1000.0 * (speeds.get(i) - speeds.get(i - 1))
		    / msDiff);
	}
	speeds.set(0, speeds.get(1));
	accelerations.set(1, 0.0);
	
	final double carMassKg = vehicle.getMass();
	final double offset = -3.5;
	final double ineff = 1;
	final double regenEff = 0.55;
	
	for (int i = 0; i < accelerations.size(); i++){
	    //Total force on the car fNet = ma = fMotor - airRes - rollingres - mgsintheta
	    double fNet = carMassKg * accelerations.get(i);  
	    //Define fR to be sum of other forces
	    double speed = speeds.get(i);
	    double fRes = 17.25*(vehicle.getMass()/907.18474)+30.94 + (5.482*speed*2.23693629) + (vehicle.getCarCrossArea()*10.7639104)*0.0015*Math.pow(speed*2.23693629,2);
	    fRes = 4.448 * fRes;
	    double fMotor = fNet + fRes;
	    double pwr = 0.0;
	   
	    if (fMotor > 0){ //Motor is applying power to the vehicle
		pwr = fMotor * speed * ineff;
	    } else { //Assume regen when motor force is negative
		pwr = regenEff*fMotor*speed;		
	    }
	    
	    pwr = ((pwr / -1000.0) + offset);
	    
	    powerDemands.add(pwr * 1000.0);// convert back to watts
	    
	}
	
	for (int i = 1; i < times.size(); i++) {
	    out.write(times.get(i-1).getTimeInMillis()+","+xpos.get(i-1)+","+speeds.get(i)+","+accelerations.get(i)+","+powerDemands.get(i)/-100.0+"\n");
	    int periodMS = (int) (times.get(i).getTimeInMillis() - times.get(i - 1).getTimeInMillis());
	    tripPoints.add(new PointFeatures(xpos.get(i - 1), ypos.get(i - 1),
		    eles.get(i - 1), planarDistances.get(i), accelerations
		    .get(i), speeds.get(i), powerDemands.get(i),
		    periodMS, times.get(i - 1)));
	}
	PointFeatures endPoint = new PointFeatures(xpos.get(xpos.size() - 1),
		ypos.get(ypos.size() - 1), eles.get(eles.size() - 1), 0.0, 0.0,
		0.0, 0.0, 1000, times.get(times.size() - 1));
	tripPoints.add(endPoint);
	out.close();
	
    }   
}
