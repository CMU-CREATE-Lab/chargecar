package org.chargecar.algodev.knn;

import org.chargecar.prize.util.PointFeatures;

public abstract class KdTreeFeatureSet {
 
    public abstract int getFeatureCount();
    public abstract double getValue(PointFeatures point, int splitType);
    public abstract double getWeight(int splitType);
    public double getValue(KnnPoint point, int splitType){
	return getValue(point.getFeatures(), splitType);
    }
    
    public abstract double distance(PointFeatures point1, PointFeatures point2);
    
    public abstract double axialDistance(PointFeatures point1, PointFeatures point2, int splitType);
    
}
