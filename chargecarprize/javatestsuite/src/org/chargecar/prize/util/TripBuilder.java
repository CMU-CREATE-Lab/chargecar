package org.chargecar.prize.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
public class TripBuilder {
    public static List<PointFeatures> calculateTrip(List<Calendar> times,
	    List<Double> lats, List<Double> lons, List<Double> eles,
	    Vehicle vehicle) {
	 List<PointFeatures> tripPoints;
		boolean broken;
		int fixes = 0;
		do{	    
		    broken = false;
		    //removeTunnels(times, lats, lons, eles);
		    //interpolatePoints(times, lats, lons, eles);
		    tripPoints = new ArrayList<PointFeatures>(times.size());
		    runPowerModel(tripPoints, times, lats, lons, eles, vehicle);
//		    for(int i=0; i < tripPoints.size();i++){
//				if(Math.abs(tripPoints.get(i).getAcceleration()) > 4.9){
//				    broken = true;
//				    fixes++;
//				    lats.remove(i);
//				    lons.remove(i);
//				    eles.remove(i);
//				    times.remove(i);
//				    tripPoints.remove(i);
//				    i=i+1;
//				}		
//		    }
		    if(times.size() <60)
			break;
		}while(broken);
		
		if((double)fixes / (tripPoints.size()+fixes) > 0.02){
		    System.out.println(fixes+"/"+(tripPoints.size()+fixes));
		    return null;
		}
		return tripPoints;		
    }
    
    private static void interpolatePoints(List<Calendar> times,
	    List<Double> lats, List<Double> lons, List<Double> eles) {
	// make sure there is a point no gaps of two seconds,
	// as this is all gps based without car scantool
	final int maxPeriodMS = 1000;
	
	for (int i = 1; i < times.size(); i++) {
	    long newTime = times.get(i).getTimeInMillis();
	    long oldTime = times.get(i - 1).getTimeInMillis();
	    if (newTime - oldTime >= 2*maxPeriodMS) {
		double latpms = (lats.get(i) - lats.get(i - 1))
		/ (newTime - oldTime);
		double lonpms = (lons.get(i) - lons.get(i - 1))
		/ (newTime - oldTime);
		double elepms = (eles.get(i) - eles.get(i - 1))
		/ (newTime - oldTime);
		Calendar interpTime = Calendar.getInstance();
		interpTime.setTimeInMillis(oldTime + maxPeriodMS);
		times.add(i, interpTime);
		
		lats.add(i, lats.get(i - 1) + maxPeriodMS * latpms);
		lons.add(i, lons.get(i - 1) + maxPeriodMS * lonpms);
		eles.add(i, eles.get(i - 1) + maxPeriodMS * elepms);
	    }
	}
    }
    
    private static void runPowerModel(List<PointFeatures> tripPoints,
	    List<Calendar> times, List<Double> lats, List<Double> lons,
	    List<Double> eles, Vehicle vehicle) {
	List<Double> bearings = new ArrayList<Double>();
	List<Double> planarDistances = new ArrayList<Double>();
	List<Double> adjustedDistances = new ArrayList<Double>();
	List<Double> speeds = new ArrayList<Double>();
	List<Double> accelerations = new ArrayList<Double>();
	List<Double> powerDemands = new ArrayList<Double>();
	
	
	double vInit = (Math.pow(Haversine(lats.get(0), lons.get(0), lats.get(1), lons.get(1)),2.0) + Math.pow(eles.get(1)-eles.get(0), 2.0))
	/ ((times.get(1).getTimeInMillis() - times.get(0).getTimeInMillis())/1000.0);
	planarDistances.add(0.0);
	adjustedDistances.add(0.0);
	speeds.add(vInit);
	accelerations.add(0.0);
	
	for (int i = 1; i < times.size(); i++) {
	    long msDiff = (times.get(i).getTimeInMillis() - times.get(i - 1)
		    .getTimeInMillis());
	    if (msDiff == 0) {
		break;
	    }
	    double sDiff = msDiff / 1000.0;	    
	    double eleDiff = eles.get(i) - eles.get(i - 1);
	    double tempDist = Haversine(lats.get(i - 1), lons.get(i - 1), lats.get(i), lons.get(i));
	    
	    bearings.add(getBearing(lats.get(i - 1), lons.get(i - 1), lats.get(i), lons.get(i)));
	    planarDistances.add(tempDist);
	    tempDist = Math.sqrt((tempDist * tempDist) + (eleDiff * eleDiff));
	    adjustedDistances.add(tempDist);	    
	    
	    double vFinal = (2*tempDist / sDiff) - vInit;	    
	    if (tempDist < 1E-6) {
		vFinal = 0.0;		
	    } 
	    speeds.add(vFinal);	    
	    accelerations.add((vFinal - vInit) / sDiff);
	    
	    vInit = vFinal;
	    
	}
	bearings.add(bearings.get(bearings.size()-1));
	
	final double carMassKg = vehicle.getMass();
	final double aGravity = 9.81;
	final double offset = -0.35;
	final double ineff = 1 / 0.85;
	final double regenEff = 0.35;
	
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
	    
//	    if (speed > 12.0) {
//		pwr = ((pwr - (0.056 * (speed * speed))) + (0.68 * speed));
//	    }
	    
	    powerDemands.add(pwr * 1000.0);// convert back to watts
	    
	}
	double powerSum = 0;
	for (int i = 1; i < times.size(); i++) {
	    int periodMS = (int) (times.get(i).getTimeInMillis() - times.get(
		    i - 1).getTimeInMillis());
	    double power = powerDemands.get(i);
	    tripPoints.add(new PointFeatures(lats.get(i - 1), lons.get(i - 1),
		    eles.get(i - 1), bearings.get(i - 1),planarDistances.get(i), accelerations
		    .get(i), speeds.get(i), power, powerSum,
		    periodMS, times.get(i - 1)));
	    powerSum += power;
	}
	PointFeatures endPoint = new PointFeatures(lats.get(lats.size() - 1),
		lons.get(lons.size() - 1), eles.get(eles.size() - 1), bearings.get(bearings.size() - 1), 0.0, 0.0,
		0.0, 0.0, powerSum, 1000, times.get(times.size() - 1));
	tripPoints.add(endPoint);
	
    }    
    
    
    private static void removeTunnels(List<Calendar> times, List<Double> lats,
	    List<Double> lons, List<Double> eles) {
	// removes tunnel points, tunnels will be fixed later by interpolation
	int consecutiveCounter = 0;
	for (int i = 1; i < times.size(); i++) {
	    if (lats.get(i).compareTo(lats.get(i - 1)) == 0
		    && lons.get(i).compareTo(lons.get(i - 1)) == 0) {
		// consecutive readings at the same position
		consecutiveCounter++;
	    } else if (consecutiveCounter > 0) {
		// position has changed, after consectuive readings at same
		// position
		// can be tunnel, red light, etc...
		if (Haversine(lats.get(i - 1), lons.get(i - 1), lats.get(i),
			lons.get(i)) > 50) {
		    // if traveled at least 50 metres, assume tunnel
		    times.subList(i - consecutiveCounter, i).clear();
		    lats.subList(i - consecutiveCounter, i).clear();
		    lons.subList(i - consecutiveCounter, i).clear();
		    eles.subList(i - consecutiveCounter, i).clear();
		    i = i - consecutiveCounter;
		}
		consecutiveCounter = 0;
	    }
	}
	
    }
    
    public static double Haversine(double lat1, double lon1, double lat2,
	    double lon2) {
	double R = 6371000; // earth radius, metres
	double dLat = Math.toRadians(lat2 - lat1);
	double dLon = Math.toRadians(lon2 - lon1);
	double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
	+ Math.cos(Math.toRadians(lat1))
	* Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
	* Math.sin(dLon / 2);
	double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	double d = R * c;
	return d;
    }
    
    public static double getBearing(double lat1, double lon1, double lat2,
	    double lon2) {
	double y = Math.sin(lat2-lat1) * Math.cos(lat2);
	double x = Math.cos(lat1)*Math.sin(lat2) -
	        Math.sin(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1);
	return (Math.toDegrees(Math.atan2(y, x))+360)%360;
    }
    
}
