package org.chargecar.algodev.predictors;

import java.util.ArrayList;
import java.util.List;

public class Prediction {
    private final double weight;
 //   private final String driver;
    private final int tripID;
    private final int timeIndex;
    
   
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
    

    public Prediction(double weight, int tripID, int timeIndex) {
	super();
	this.weight = weight;
//	this.driver = driver;
	this.tripID = tripID;
	this.timeIndex = timeIndex;
    }
    
    
}
