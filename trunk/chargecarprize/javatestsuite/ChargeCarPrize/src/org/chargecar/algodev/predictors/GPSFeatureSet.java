package org.chargecar.algodev.predictors;

import java.util.Collection;
import java.util.List;

import org.chargecar.algodev.predictors.knn.KnnPoint;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.TripBuilder;

public class GPSFeatureSet extends KdTreeFeatureSet {
  
    private final int featureCount = 2;
    private final FullFeatureSet ffs = new FullFeatureSet(); 
    public int getFeatureCount(){
	return featureCount;
    }
   
    public double getValue(PointFeatures point, int splitType) {
	switch(splitType)
	{
	case 0: return point.getLatitude();
	case 1: return point.getLongitude();
	default: return 0.0;
	}
    }
    @Override
/*    public double estimate(PointFeatures pf, List<KnnPoint> neighbors) {
	
	double distSum = 0;
	double estimate = 0;
	for(int i=0;i<neighbors.size();i++){
	    double dist = distance(epf, neighbors.get(i).getFeatures());
	    double bear = normalize(epf.getBearing() - neighbors.get(i).getFeatures().getBearing());
	    dist = dist + bear*bear;
	    if(dist==0.0) return neighbors.get(i).getGroundTruth();
	    estimate += neighbors.get(i).getGroundTruth() * 1.0/dist;
	    distSum += 1.0/dist;
	}
	return ffs.estimate(epf, neighbors);
    }*/
    
    public double distance(PointFeatures point1, PointFeatures point2){
	double dist = 0.0;
	for(int i =0;i<getFeatureCount();i++){
	    double temp = getValue(point1, i) - getValue(point2,i);
	    dist += temp*temp;
	}
	return dist;
    }
    
    private double normalize(double bear){
	while(bear > 180){
	    bear -= 360;
	}
	while(bear <= -180){
	    bear =+ 360;
	}
	return bear;
    }

    @Override
    public List<Double> estimate(PointFeatures epf,
	    Collection<KnnPoint> neighbors, List<Double> powers, int lookahead) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public double getWeight(int splitType) {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public double axialDistance(PointFeatures point1, PointFeatures point2,
	    int splitType) {
	// TODO Auto-generated method stub
	return 0;
    }
        
}

