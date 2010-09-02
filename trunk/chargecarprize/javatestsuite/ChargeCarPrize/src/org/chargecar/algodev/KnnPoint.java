package org.chargecar.algodev;

import java.io.Serializable;

import org.chargecar.prize.util.PointFeatures;

public class KnnPoint implements Serializable{
    private final PointFeatures features;
    private final double truth;
    
    public KnnPoint(PointFeatures features, double truth){
	this.features = features;
	this.truth = truth;
    }
    
    public PointFeatures getFeatures(){
	return features;
    }
    
    public double getGroundTruth(){
	return truth;
    }
    
}
