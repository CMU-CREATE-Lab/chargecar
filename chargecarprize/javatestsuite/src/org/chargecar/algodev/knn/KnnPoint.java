package org.chargecar.algodev.knn;

import java.io.Serializable;

import org.chargecar.prize.util.PointFeatures;

public class KnnPoint implements Serializable{
    private PointFeatures features;
    private final int timeIndex;
    private final int tripID;
    private double distance;
  
    public KnnPoint(PointFeatures features, int timeIndex, int tripID, double dist){
	this.features = features;
	this.timeIndex = timeIndex;
	this.tripID = tripID;
	this.distance = dist;	
    }
    
    public KnnPoint(PointFeatures features, int truthIndex, int tripID){
	this(features,truthIndex,tripID, Double.MAX_VALUE);
    }
    
    public void setFeatures(PointFeatures features) {
        this.features = features;
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
    
    public int getTimeIndex(){
	return timeIndex;
    }
    
    public int getTripID(){
	return tripID;
    }
    
}
