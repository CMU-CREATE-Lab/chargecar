package org.chargecar.algodev.predictors;

import java.util.ArrayList;
import java.util.List;

import org.chargecar.algodev.knn.KnnPoint;

public class Prediction {
    private double weight;
 //   private final String driver;
    private final int tripID;
    private final int timeIndex;
    private final KnnPoint knnPoint;
    
   
//    public String getDriver() {
//        return driver;
//    }

    public int getTripID() {
        return tripID;
    }

    public int getTimeIndex() {
        return timeIndex;
    }

    public double getWeight(){
	return weight;
    }
    
    public KnnPoint getPoint(){
	return this.knnPoint;
    }
    
    public void setWeight(double weight){
	this.weight = weight;
    }
    

    public Prediction(double weight, int tripID, int timeIndex, KnnPoint point) {
	super();
	this.weight = weight;
//	this.driver = driver;
	this.tripID = tripID;
	this.timeIndex = timeIndex;
	this.knnPoint = point;
    }
    
    
}
