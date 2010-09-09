package org.chargecar.algodev.knn;

import java.util.List;

import org.chargecar.algodev.ExtendedPointFeatures;
import org.chargecar.algodev.KnnPoint;

public abstract class KdTreeFeatureSet {
    public abstract double estimate(ExtendedPointFeatures epf, List<KnnPoint> neighbors);
    public abstract int getFeatureCount();
    public abstract double getValue(ExtendedPointFeatures point, int splitType);
    
    public double getValue(KnnPoint point, int splitType){
	return getValue(point.getFeatures(), splitType);
    }
    
    public abstract double distance(ExtendedPointFeatures point1, ExtendedPointFeatures point2);
}
