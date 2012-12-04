package org.chargecar.prize.util;

import java.io.Serializable;
import java.util.Calendar;

/**
 * DO NOT EDIT
 * 
 * Contains the features for an individual point in a trip, such as GPS
 * coordinates, power demand, speed, etc...
 * 
 * @author Alex Styler
 */
public class PointFeatures implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    final private double latitude;
    final private double longitude;
    final private double elevation;
    final private double bearing;
    final private double planarDist;
    final private double acceleration;
    final private double speed;
    final private double powerDemand;    
    final private double totalPowerUsed;
    final private int periodMS;
    final private Calendar time;
    
    public PointFeatures(double latitude, double longitude, double elevation, double bearing,
	    double planarDist, double acceleration, double speed,
	    double powerDemand, double totalPowerUsed, int periodMS, Calendar time) {
	this.latitude = latitude;
	this.longitude = longitude;
	this.elevation = elevation;
	this.bearing = bearing;
	this.acceleration = acceleration;
	this.speed = speed;
	this.powerDemand = powerDemand;
	this.totalPowerUsed = totalPowerUsed;
	this.periodMS = periodMS;
	this.planarDist = planarDist;
	this.time = time;
    }
    
    public PointFeatures clone() {
	return new PointFeatures(this.latitude, this.longitude, this.elevation, 
		this.bearing, this.planarDist, this.acceleration, this.speed,
		this.powerDemand, this.totalPowerUsed, this.periodMS, (Calendar) this.time.clone());
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
    
    public double getBearing(){
	return this.bearing;
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
    
    public double getTotalPowerUsed(){
	return this.totalPowerUsed;
    }
    
    public int getPeriodMS() {
	return this.periodMS;
    }
    
    public Calendar getTime() {
	return this.time;
    }
    
}
