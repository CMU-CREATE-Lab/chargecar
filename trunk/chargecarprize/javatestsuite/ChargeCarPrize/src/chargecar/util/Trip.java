package chargecar.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public class Trip {
	private TripFeatures features;
	private List<PointFeatures> points;
	
	public Trip(TripFeatures features, List<PointFeatures> points)
	{
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
	
	public String toString(){
		String driver = this.getFeatures().getDriver();
		String time = this.getFeatures().getStartTime().getTime().toLocaleString();
		String lat = Double.toString(this.getFeatures().getStartLat());
		String lon = Double.toString(this.getFeatures().getStartLon());
		return "TRIP: "+driver + " at "+time+", from ("+lat+", "+lon+").";
	}
}
