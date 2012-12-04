package org.chargecar.algodev.predictors.knn;

import java.util.List;

import org.chargecar.algodev.predictors.KdTreeFeatureSet;
import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.algodev.predictors.Predictor;
import org.chargecar.prize.util.PointFeatures;

public class KnnDistPredictor extends Predictor {
    private final KdTree featTree;
    private final int k;
//    private final int lookahead;
    
    public KnnDistPredictor(List<KnnPoint> points, KdTreeFeatureSet featureSet, int neighbors){//, int lookahead){
	this.featTree = new KdTree(points, featureSet);
	this.k = neighbors;
	//this.lookahead = lookahead;
    }
    @Override
    public List<Prediction> predictDuty(PointFeatures state) {
	List<Prediction> neighbors = featTree.getNeighbors(state, k);//, lookahead);
	return neighbors;
    }
}
