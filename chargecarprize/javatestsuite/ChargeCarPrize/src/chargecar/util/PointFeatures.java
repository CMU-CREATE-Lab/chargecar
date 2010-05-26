package chargecar.util;

import java.util.Calendar;

/**
 * DO NOT EDIT
 * 
 * Contains the features for an individual point in a trip, such as GPS
 * coordinates, power demand, speed, etc...
 * 
 * @author Alex Styler
 */
public class PointFeatures {
    final private double latitude;
    final private double longitude;
    final private double elevation;
    final private double acceleration;
    final private double speed;
    final private double powerDemand;
    final private double planarDist;
    final private int periodMS;
    final private Calendar time;
    
    public PointFeatures(double latitude, double longitude, double elevation,
	    double planarDist, double acceleration, double speed,
	    double powerDemand, int periodMS, Calendar time) {
	this.latitude = latitude;
	this.longitude = longitude;
	this.elevation = elevation;
	this.acceleration = acceleration;
	this.speed = speed;
	this.powerDemand = powerDemand;
	this.periodMS = periodMS;
	this.planarDist = planarDist;
	this.time = time;
    }
    
    public PointFeatures clone() {
	return new PointFeatures(this.latitude, this.longitude, this.elevation,
		this.planarDist, this.acceleration, this.speed,
		this.powerDemand, this.periodMS, (Calendar) this.time.clone());
    }
    
    public double getLatitude() {
	return this.latitude;
    }
    
    public double getLongitude() {
	return this.longitude;
    }
    
    public double getElevation() {
	return this.elevation;
    }
    
    public double getPlanarDist() {
	return this.planarDist;
    }
    
    public double getAcceleration() {
	return this.acceleration;
    }
    
    public double getSpeed() {
	return this.speed;
    }
    
    public double getPowerDemand() {
	return this.powerDemand;
    }
    
    public int getPeriodMS() {
	return this.periodMS;
    }
    
    public Calendar getTime() {
	return this.time;
    }
    
}
