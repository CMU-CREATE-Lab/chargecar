package org.chargecar.algodev.predictors.knn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.chargecar.algodev.predictors.KdTreeFeatureSet;
import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.algodev.predictors.Predictor;
import org.chargecar.prize.util.PointFeatures;

public class KnnMeanPredictor extends Predictor {
    private final KdTree featTree;
    private final int k;
    private final int lookahead;
    
    public KnnMeanPredictor(List<KnnPoint> points, List<Double> powers, KdTreeFeatureSet featureSet, int neighbors, int lookahead){
	this.featTree = new KdTree(points,powers,featureSet);
	this.k = neighbors;
	this.lookahead = lookahead;
    }
    @Override
    public List<Prediction> predictDuty(PointFeatures state) {
	List<Prediction> neighbors = featTree.getNeighbors(state, k, lookahead);
	Prediction meanPrediction = averagePredictions(neighbors);
	neighbors.clear();
	neighbors.add(meanPrediction);
	return neighbors;
    }
    private Prediction averagePredictions(List<Prediction> neighbors){	
	List<Double> powerSums = new ArrayList<Double>();
	List<Double> weightSums = new ArrayList<Double>();//needed for shorter prediction lists
	for (int i = 0; i < lookahead; i++) {
	    powerSums.add(0.0);
	    weightSums.add(0.0);
	}
	
	for(Prediction neighbor : neighbors){
	    double weight = 1.0;//neighbor.getWeight();
  
	    for (int j = 0; j < lookahead; j++) {
		Double powerD = neighbor.getPowers().get(j);
		if (powerD == null) {
		    break;
		}
		powerSums.set(j, powerSums.get(j) + powerD * weight);	    
		weightSums.set(j, weightSums.get(j) + weight);
	    }
	}
	
	for (int i = 0; i < lookahead; i++) {
	    if (weightSums.get(i) == 0.0)
		powerSums.set(i, 0.0);
	    else powerSums.set(i, powerSums.get(i) / weightSums.get(i));
	}
	return new Prediction(1,powerSums);
    }
    
}
