package org.chargecar.algodev.knn;

import java.util.ArrayList;
import java.util.List;

import org.chargecar.prize.util.PointFeatures;

public class FullFeatureSet extends KdTreeFeatureSet {    
    private final int featureCount = 7;
    private final double[] weights = new double[]{10,10,5,1,1,1,4};
    
    public int getFeatureCount(){
	return featureCount;
    }
    
    public double getValue(PointFeatures point, int splitType) {
	switch(splitType)
	{
	case 0: return point.getLatitude();
	case 1: return point.getLongitude();
	case 2: return point.getSpeed();
	//case 3: return point.getAcceleration();
	case 3: return point.getElevation();
	case 4: return point.getBearing();	
	case 5: return point.getPowerDemand();
	case 6: return point.getTotalPowerUsed();
	
	default: return 0.0;
	}	
    }
    
    public double distance(PointFeatures point1, PointFeatures point2){
	double dist = 0.0;
	for(int i =0;i<getFeatureCount();i++){
	    double temp = getValue(point1, i) - getValue(point2,i);
	    dist += temp*temp*weights[i];
	}
	return dist;
    }
    
    public List<Double> estimate(PointFeatures pf, List<KnnPoint> neighbors, List<Double> powers, int lookahead) {
	List<Double> powerSums = new ArrayList<Double>();
	List<Double> pointScales = new ArrayList<Double>();
	for(int i=0;i<lookahead;i++){
	    powerSums.add(0.0);
	    pointScales.add(0.0);
	}
	for(int i=0;i<neighbors.size();i++){
	    double distScaler = 1.0/(distance(pf, neighbors.get(i).getFeatures())+0.01);	    
	    int powerInd = neighbors.get(i).getGroundTruthIndex();
	    for(int j=0;j<lookahead;j++){
		Double powerD = powers.get(powerInd + j);
		if(powerD == null) {
		    break;
		}
		powerSums.set(j, powerSums.get(j)+powerD*distScaler);
		pointScales.set(j, pointScales.get(j)+distScaler);		
	    }
	}
	for(int i=0;i<lookahead;i++){
	    if(pointScales.get(i) == 0) powerSums.set(i, 0.0);
	    else powerSums.set(i, powerSums.get(i) / pointScales.get(i));
	}
		
	return powerSums;	
    }
    
    public double getWeight(int splitType) {	
	return weights[splitType];
    }    
}
