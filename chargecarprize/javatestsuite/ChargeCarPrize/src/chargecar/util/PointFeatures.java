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
	final private Double latitude;
	final private Double longitude;
	final private Double elevation;
	final private Double acceleration;
	final private Double speed;
	final private Double powerDemand;
	final private Double period;
	final private Calendar time;

	public PointFeatures(Double latitude, Double longitude, Double elevation,
			Double acceleration, Double speed, Double powerDemand,
			Double period, Calendar time) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.elevation = elevation;
		this.acceleration = acceleration;
		this.speed = speed;
		this.powerDemand = powerDemand;
		this.period = period;
		this.time = time;
	}
	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public Double getElevation() {
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

	public Double getPeriod() {
		return period;
	}

	public Calendar getTime() {
		return time;
	}

}
