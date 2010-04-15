package chargecar.util;

import java.util.Date;

/**
 * @author Alex Styler
 * DO NOT EDIT
 * Contains the features for an individual point in a trip,
 * such as GPS coordinates, power demand, speed, etc...
 */
public class PointFeatures {
	private Double latitude;
	private Double longitude;
	private Double elevation;
	private Double acceleration;
	private Double speed;
	private Double powerDemand;
	private Double period;
	public PointFeatures(){
		
	}
	public double getPowerDemand(){
		return 0.0;
	}
	public double getPeriod() {
		return 1.0;
	}
}
