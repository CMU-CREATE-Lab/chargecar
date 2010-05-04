package chargecar.util;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Alex Styler
 * DO NOT EDIT
 * Contains the features for an individual point in a trip,
 * such as GPS coordinates, power demand, speed, etc...
 */
public class PointFeatures {
	final private double latitude;
	final private double longitude;
	final private double elevation;
	final private double acceleration;
	final private double speed;
	final private double powerDemand;
	final private int periodMS;
	final private Calendar time;

	public PointFeatures(Double latitude, Double longitude, Double elevation,
			Double acceleration, Double speed, Double powerDemand,
			int periodMS, Calendar time) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.elevation = elevation;
		this.acceleration = acceleration;
		this.speed = speed;
		this.powerDemand = powerDemand;
		this.periodMS = periodMS;
		this.time = time;
	}
	
	public PointFeatures clone(){
		return new PointFeatures(this.latitude,this.longitude,this.elevation,this.acceleration,this.speed,this.powerDemand,this.periodMS,(Calendar) this.time.clone());
	}
	
	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getElevation() {
		return elevation;
	}

	public Double getAcceleration() {
		return acceleration;
	}

	public Double getSpeed() {
		return speed;
	}

	public Double getPowerDemand() {
		return powerDemand;
	}

	public int getPeriodMS() {
		return periodMS;
	}

	public Calendar getTime() {
		return time;
	}

}
