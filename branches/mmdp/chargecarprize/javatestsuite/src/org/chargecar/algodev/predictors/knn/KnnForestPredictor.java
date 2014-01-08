package org.chargecar.algodev.predictors.knn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chargecar.algodev.knn.KdForest;
import org.chargecar.algodev.knn.KdTree;
import org.chargecar.algodev.knn.KdTreeFeatureSet;
import org.chargecar.algodev.knn.KnnPoint;
import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.algodev.predictors.Predictor;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.Trip;

    public class KnnForestPredictor implements Predictor {
	private KdForest forest;
        private final boolean trained;
        private final KdTreeFeatureSet featureSet;
        
        public KnnForestPredictor(List<KnnPoint> points, KdTreeFeatureSet featureSet, boolean trainedOnTestData){//, int lookahead){
            this.forest = new KdForest(featureSet);    		
            this.trained = trainedOnTestData;
            this.featureSet = featureSet;
            addPoints(points);
    	}
        
        private void addPoints(List<KnnPoint> points){
            Map<Integer, KdTree> treeMap = new HashMap<Integer, KdTree>();
            
            for(KnnPoint kp : points){
        	int tripID = kp.getTripID();
        	KdTree tree = treeMap.get(tripID);
        	if(tree==null){
        	    tree = new KdTree(null,featureSet);
        	    tree.addNode(kp);
        	    treeMap.put(tripID, tree);
        	}
        	else{
        	    tree.addNode(kp);
        	}
            }
            
            for(KdTree tree : treeMap.values()){
        	forest.addExpert(tree);
            }   
        }
        
        public int expertCount(){
            return forest.getNumExperts();
        }
        
        
        @Override
        public List<Prediction> predictDuty(PointFeatures state) {
            return forest.getNeighbors(state, this.trained);
        }
        
        public void feedback(List<Double> scores){
            // TODO: Implement some sort of feedback scaling of expert weights based on scores returned by Policy.            
        }
        
        public void addTrip(Trip t){
            KdTree tree = new KdTree(null, this.featureSet);
            for(int i = 0; i<t.getPoints().size();i++){
    		PointFeatures pf = t.getPoints().get(i);
    		KnnPoint kp = new KnnPoint(pf,i, t.hashCode());
    		tree.addNode(kp);
    	    }    	
        }

	@Override
	public void endTrip() {
	    this.forest.resetWeights();	    
	}
        
        
    }

