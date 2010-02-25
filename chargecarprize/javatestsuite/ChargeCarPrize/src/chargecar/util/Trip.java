package chargecar.util;

import java.util.ArrayList;
import java.util.List;

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
}
