package org.chargecar.prize.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * DO NOT EDIT
 * 
 * Java class for storing a commute. GPX files are parsed into trips. Trips
 * contain overarching trip features, then a list of points during the trip,
 * each with it's own features.
 * 
 * @author Alex Styler
 */
public class Trip {
    private TripFeatures features;
    private List<PointFeatures> points;
    
    public Trip(TripFeatures features, List<PointFeatures> points) {
	this.features = features;
	this.points = new ArrayList<PointFeatures>(points);
    }
    
    /**
     * @return the features
     */
    public TripFeatures getFeatures() {
	return features;
    }
    
    /**
     * @return the points
     */
    public List<PointFeatures> getPoints() {
	return points;
    }
    
    public String toLongString() {
	StringBuilder tripString = new StringBuilder();
	tripString.append("Driver: " + this.getFeatures().getDriver() + "\n");
	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
	List<PointFeatures> pfs = getPoints();
	tripString.append("Point count:" + pfs.size() + "\n");
	tripString.append("Start time: "
		+ sdf.format(pfs.get(0).getTime().getTime()) + "\n");
	tripString.append("End time: "
		+ sdf.format(pfs.get(pfs.size() - 1).getTime().getTime())
		+ "\n");
	int periodMax = pfs.get(0).getPeriodMS();
	int periodMin = periodMax;
	double speedMax = pfs.get(0).getSpeed();
	double speedMin = speedMax;
	double powerMax = pfs.get(0).getPowerDemand();
	double powerMin = 0;
	
	for (PointFeatures pf : pfs) {
	    if (pf.getPeriodMS() > periodMax) {
		periodMax = pf.getPeriodMS();
	    }
	    if (pf.getPeriodMS() < periodMin) {
		periodMin = pf.getPeriodMS();
	    }
	    if (pf.getSpeed() > speedMax) {
		speedMax = pf.getSpeed();
	    }
	    if (pf.getSpeed() < speedMin) {
		speedMin = pf.getSpeed();
	    }
	    if (pf.getPowerDemand() > powerMax) {
		powerMax = pf.getPowerDemand();
	    }
	    if (pf.getPowerDemand() < powerMin) {
		powerMin = pf.getPowerDemand();
	    }
	}
	
	tripString.append("Period range: " + periodMin + " to " + periodMax
		+ "\n");
	tripString
		.append("Speed range: " + speedMin + " to " + speedMax + "\n");
	tripString.append("Power range: " + powerMin + " to " + powerMax);
	
	return tripString.toString();
    }
    
    public String toString() {
	List<PointFeatures> pfs = getPoints();
	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
	return "Driver:" + this.getFeatures().getDriver() + " Time:"
		+ sdf.format(pfs.get(0).getTime().getTime());
    }
    
    public int hashCode(){
	return this.toString().hashCode();
	
    }
}
