package org.chargecar.algodev;

import java.io.Serializable;

import org.chargecar.prize.util.PointFeatures;

public class KnnPoint implements Serializable{
    private final ExtendedPointFeatures features;
    private final double truth;
    
    public KnnPoint(ExtendedPointFeatures features, double truth){
	this.features = features;
	this.truth = truth;
    }
    
    public ExtendedPointFeatures getFeatures(){
	return features;
    }
    
    public double getGroundTruth(){
	return truth;
    }
    
}
