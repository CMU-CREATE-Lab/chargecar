package org.chargecar.algodev.knn;

import java.util.ArrayList;
import java.util.List;

import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.util.PointFeatures;



public class KdForest {
  //private List<Double> expertWeights;
    private List<Expert> experts;
    private KdTreeFeatureSet featureSet;
    
    public class Expert{
	private final KdTree tree;
	private double weight;
	public Expert(KdTree tree, double weight){
	    this.tree = tree;
	    this.weight = weight;
	}
	
	public void scaleWeight(double factor){
	    this.weight *= factor;
	}
	
	public void setWeight(double weight){
	    this.weight = weight;
	}
	
	public double getWeight(){
	    return this.weight;
	}
	public KdTree getTree(){
	    return this.tree;
	}
	
    }    
    
    public KdForest(KdTreeFeatureSet featureSet){
	this.featureSet = featureSet;
	experts = new ArrayList<Expert>();
    }
    
    public void addExpert(KdTree tree){	
	experts.add(new Expert(tree, 1D));	
    }
    
    public void addExpert(List<KnnPoint> points){
	KdTree tree = new KdTree(points, featureSet);
	addExpert(tree);	
    }
    
    public int getNumExperts(){
	return experts.size();
    }    

    public void resetWeights(){
	for(Expert e : experts){
	    e.setWeight(1D);
	}
	
    }
    
    public List<Prediction> getNeighbors(PointFeatures searchPoint, boolean trained){	
	List<Prediction> predictions = new ArrayList<Prediction>();
	
	for(Expert expert : experts){
	    //TODO Test single searchNeighbor vs searchNeighbors k=1
	    Prediction p = expert.getTree().getNeighbor(searchPoint);
	    if(trained && p.getWeight() > 99){
		//We have trained on the data in the set, so skip the perfect match trip as it is the search trip
		//We use the rest of the dataset trips to achieve LOOCV
		continue;
	    }
	    else{
		p.setWeight(p.getWeight()*expert.getWeight());		
	    }
	    
	    predictions.add(p);
	}
	
	return predictions;
    }
}
