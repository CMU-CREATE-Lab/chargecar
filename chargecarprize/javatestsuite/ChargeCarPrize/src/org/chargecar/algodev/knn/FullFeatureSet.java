package org.chargecar.algodev.knn;

import java.util.List;

import org.chargecar.algodev.ExtendedPointFeatures;
import org.chargecar.algodev.KnnPoint;

public class FullFeatureSet extends KdTreeFeatureSet {    
    private final int featureCount = 5;
    
    public int getFeatureCount(){
	return featureCount;
    }
    
    public double getValue(ExtendedPointFeatures point, int splitType) {
	switch(splitType)
	{
	case 0: return point.getSpeed();
	case 1: return point.getAcceleration();
	case 2: //return point.getAccelHistVar();
	case 3: //return point.getSpeedHistVar();
	case 4: return point.getPowerDemand();
	
	default: return 0.0;
	}	
    }
    
    public double distance(ExtendedPointFeatures point1, ExtendedPointFeatures point2){
	double dist = 0.0;
	for(int i =0;i<getFeatureCount();i++){
	    double temp = getValue(point1, i) - getValue(point2,i);
	    dist += temp*temp;
	}
	return dist;
    }
    
    public double estimate(ExtendedPointFeatures epf, List<KnnPoint> neighbors) {
	double distSum = 0;
	double estimate = 0;
	for(int i=0;i<neighbors.size();i++){
	    double dist = distance(epf, neighbors.get(i).getFeatures());
	    //dist=dist*dist;
	    if(dist==0.0) return neighbors.get(i).getGroundTruth();
	    estimate += neighbors.get(i).getGroundTruth() * 1.0/dist;
	    distSum += 1.0/dist;
	}
	return estimate / distSum;	
    }    
}
