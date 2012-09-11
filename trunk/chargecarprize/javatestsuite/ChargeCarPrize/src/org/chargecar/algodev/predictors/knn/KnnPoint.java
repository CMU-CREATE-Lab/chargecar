package org.chargecar.algodev.predictors.knn;

import java.io.Serializable;

import org.chargecar.prize.util.PointFeatures;

public class KnnPoint implements Serializable{
    private final PointFeatures features;
    private final int truthIndex;
    private final int tripID;
    private double distance;
    
  
    public KnnPoint(PointFeatures features, int truthIndex, int tripID, double dist){
	this.features = features;
	this.truthIndex = truthIndex;
	this.tripID = tripID;
	this.distance = dist;	
    }
    
    public KnnPoint(PointFeatures features, int truthIndex, int tripID){
	this(features,truthIndex,tripID,Double.MAX_VALUE);
    }
    
    public double getDistance(){
	return distance;
    }
    public void setDistance(double dist){
	this.distance = dist;
    }
    
    public PointFeatures getFeatures(){
	return features;
    }
    
    public int getGroundTruthIndex(){
	return truthIndex;
    }
    
    public int getTripID(){
	return tripID;
    }
    
}
