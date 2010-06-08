/**
 * 
 */
package org.chargecar.prize.util;

import java.util.Calendar;

/**
 * DO NOT EDIT
 * 
 * Contains the features for a new trip, including driver id, car weight,
 * starting GPS location, temperature, etc..
 * 
 * @author Alex Styler
 */
public class TripFeatures {
    private String driver;
    private double carMass;
    private Calendar startTime;
    private double startLat;
    private double startLon;
    
    public TripFeatures(String driver, double carMass, PointFeatures startPoint) {
	super();
	this.driver = driver;
	this.carMass = carMass;
	this.startTime = (Calendar) startPoint.getTime().clone();
	this.startLat = startPoint.getLatitude();
	this.startLon = startPoint.getLongitude();
    }
    
    public String getDriver() {
	return driver;
    }
    
    public double getCarMass() {
	return carMass;
    }
    
    public Calendar getStartTime() {
	return (Calendar) startTime.clone();
    }
    
    public double getStartLat() {
	return startLat;
    }
    
    public double getStartLon() {
	return startLon;
    }
    
}
