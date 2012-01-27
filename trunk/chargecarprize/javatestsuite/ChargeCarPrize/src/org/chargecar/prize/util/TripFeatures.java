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
    private String fileName;
    private Vehicle vehicle;
    private Calendar startTime;
    private double startLat;
    private double startLon;
    
    public TripFeatures(String driver, String fileName, Vehicle vehicle, PointFeatures startPoint) {
	super();
	this.fileName = fileName;
	this.driver = driver;
	this.vehicle = vehicle;
	this.startTime = (Calendar) startPoint.getTime().clone();
	this.startLat = startPoint.getLatitude();
	this.startLon = startPoint.getLongitude();
    }
    
    public String getDriver() {
	return driver;
    }
    
    public Vehicle getVehicle() {
	return vehicle;
    }
    
    public String getFileName(){
	return fileName;
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
