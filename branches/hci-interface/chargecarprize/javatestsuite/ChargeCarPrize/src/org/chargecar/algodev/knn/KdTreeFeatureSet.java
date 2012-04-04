package org.chargecar.algodev.knn;

import java.util.Collection;
import java.util.List;

import org.chargecar.prize.util.PointFeatures;

public abstract class KdTreeFeatureSet {
    public abstract List<Double> estimate(PointFeatures epf, Collection<KnnPoint> neighbors, List<Double> powers, int lookahead);
    public abstract int getFeatureCount();
    public abstract double getValue(PointFeatures point, int splitType);
    public abstract double getWeight(int splitType);
    public double getValue(KnnPoint point, int splitType){
	return getValue(point.getFeatures(), splitType);
    }
    
    public abstract double distance(PointFeatures point1, PointFeatures point2);
    
    public abstract double axialDistance(PointFeatures point1, PointFeatures point2, int splitType);
    
}
