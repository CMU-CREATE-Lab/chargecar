package chargecar.util;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Alex Styler
 * DO NOT EDIT
 * Contains the features for an individual point in a trip,
 * such as GPS coordinates, power demand, speed, etc...
 */
public class PointFeatures {
	final private BigDecimal latitude;
	final private BigDecimal longitude;
	final private BigDecimal elevation;
	final private BigDecimal acceleration;
	final private BigDecimal speed;
	final private BigDecimal powerDemand;
	final private BigDecimal planarDist;
	final private int periodMS;
	final private Calendar time;

	public PointFeatures(BigDecimal latitude, BigDecimal longitude, BigDecimal elevation, BigDecimal planarDist,
			BigDecimal acceleration, BigDecimal speed, BigDecimal powerDemand,
			int periodMS, Calendar time) {
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
	
	public PointFeatures clone(){
		return new PointFeatures(this.latitude,this.longitude,this.elevation,this.planarDist,this.acceleration,this.speed,this.powerDemand,this.periodMS,(Calendar) this.time.clone());
	}
	
	public BigDecimal getLatitude() {
		return this.latitude;
	}

	public BigDecimal getLongitude() {
		return this.longitude;
	}

	public BigDecimal getElevation() {
		return this.elevation;
	}

	public BigDecimal getPlanarDist(){
		return this.planarDist;
	}
	public BigDecimal getAcceleration() {
		return this.acceleration;
	}

	public BigDecimal getSpeed() {
		return this.speed;
	}

	public BigDecimal getPowerDemand() {
		return this.powerDemand;
	}

	public int getPeriodMS() {
		return this.periodMS;
	}

	public Calendar getTime() {
		return this.time;
	}

}
