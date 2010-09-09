package org.chargecar.algodev.knn;

import java.util.List;

import org.chargecar.algodev.ExtendedPointFeatures;
import org.chargecar.algodev.KnnPoint;
import org.chargecar.prize.util.TripBuilder;

public class GPSFeatureSet extends KdTreeFeatureSet {
  
    private final int featureCount = 2;
    private final FullFeatureSet ffs = new FullFeatureSet(); 
    public int getFeatureCount(){
	return featureCount;
    }
   
    public double getValue(ExtendedPointFeatures point, int splitType) {
	switch(splitType)
	{
	case 0: return point.getLatitude();
	case 1: return point.getLongitude();
	default: return 0.0;
	}
    }
    @Override
    public double estimate(ExtendedPointFeatures epf, List<KnnPoint> neighbors) {
	return ffs.estimate(epf, neighbors);
    }
    
    public double distance(ExtendedPointFeatures point1, ExtendedPointFeatures point2){
	double dist = 0.0;
	for(int i =0;i<getFeatureCount();i++){
	    double temp = getValue(point1, i) - getValue(point2,i);
	    dist += temp*temp;
	}
	return dist;
    }
    
    private double getHaversineDist(ExtendedPointFeatures epf, KnnPoint kp){
	return TripBuilder.Haversine(epf.getLatitude(), epf.getLongitude(), kp.getFeatures().getLatitude(), kp.getFeatures().getLongitude());
    }
    
}

