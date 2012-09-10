package org.chargecar.algodev.predictors.knn;

import java.io.Serializable;

import org.chargecar.prize.util.PointFeatures;

public class KnnPoint implements Serializable{
    private final PointFeatures features;
    private final int truthIndex;
    
    public KnnPoint(PointFeatures features, int truthIndex){
	this.features = features;
	this.truthIndex = truthIndex;
    }
    
    public PointFeatures getFeatures(){
	return features;
    }
    
    public int getGroundTruthIndex(){
	return truthIndex;
    }
    
}
