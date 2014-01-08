package org.chargecar.algodev.predictors.knn;

import java.util.List;

import org.chargecar.algodev.knn.KdTree;
import org.chargecar.algodev.knn.KdTreeFeatureSet;
import org.chargecar.algodev.knn.KnnPoint;
import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.algodev.predictors.Predictor;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.Trip;

public class KnnDistPredictor implements Predictor {
    private final KdTree featTree;
    private final int k;
    private List<Prediction> neighbors;
    private final boolean trained;
    
    public KnnDistPredictor(List<KnnPoint> points, KdTreeFeatureSet featureSet, int neighbors, boolean trainedOnTestData){//, int lookahead){
	this.featTree = new KdTree(points, featureSet);
	this.k = neighbors;
	this.neighbors = null;
	this.trained = trainedOnTestData;
	//this.lookahead = lookahead;
    }
    @Override
    public List<Prediction> predictDuty(PointFeatures state) {
	this.neighbors = featTree.getNeighbors(state, k, this.neighbors, this.trained);
	return this.neighbors;
    }
    
    public void addTrip(Trip t){
	for(int i = 0; i<t.getPoints().size();i++){
		PointFeatures pf = t.getPoints().get(i);
		KnnPoint kp = new KnnPoint(pf,i, t.hashCode());
		featTree.addNode(kp);
	    }
	
    }
    @Override
    public void endTrip() {
	this.neighbors = null;
    }
}
