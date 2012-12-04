package org.chargecar.algodev.predictors.knn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
//import java.util.Random;

import org.chargecar.algodev.predictors.KdTreeFeatureSet;
import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.util.PointFeatures;

public class KdTree {
    private final List<Double> powers;
    private final KdTreeNode root;
    //private final Random randomGenerator = new Random();
    private final KdTreeFeatureSet featureSet;
    private double kBestDist = Double.MAX_VALUE;
    
    public KdTree(List<KnnPoint> points, List<Double> powers, KdTreeFeatureSet featureSet){
	this.featureSet = featureSet;
	this.root = buildTree(points, 0);
	this.powers = powers;
    }
    
    private KdTreeNode buildTree(List<KnnPoint> points, int splitType){
	splitType = splitType % featureSet.getFeatureCount();
	KdTreeNode node;
	if(points.size() == 0) return null;
	else if(points.size() == 1){	    
	    KnnPoint point = points.get(0);
	    node = new KdTreeNode(point, null, null, splitType);	    
	}
	else{ 
	    int pivot = select(points, 0, points.size()-1, (int)(points.size()/2), splitType);
	    KdTreeNode leftSubtree = buildTree(new ArrayList<KnnPoint>(points.subList(0, pivot)), splitType+1);
	    KdTreeNode rightSubtree = buildTree(new ArrayList<KnnPoint>(points.subList(pivot+1, points.size())), splitType+1);
	    node = new KdTreeNode(points.get(pivot), leftSubtree, rightSubtree, splitType);
	}
	return node;
    }
    
    public int countNodes(){
	return root.countNodes();
    }
    
    public List<Prediction> getNeighbors(PointFeatures point, int k, int lookahead){
	Comparator<KnnPoint> comp = new KnnComparator(point,featureSet);
	PriorityQueue<KnnPoint> neighbors = new PriorityQueue<KnnPoint>(k+1,comp);
	kBestDist = Double.MAX_VALUE;
	searchTree(root, point, neighbors, k, new double[featureSet.getFeatureCount()]);
	List<Prediction> predictions = new ArrayList<Prediction>();
	for(KnnPoint kp : neighbors){
	    List<Double> pointPowers = this.powers.subList(kp.getGroundTruthIndex(), Math.min(kp.getGroundTruthIndex()+lookahead, this.powers.size()));
	    if(pointPowers != null & pointPowers.size() > 0){
		predictions.add(new Prediction(1/(kp.getDistance()+1e-12),pointPowers));
	    }
	}
	return predictions;
    }
    
   
    
    private void addNeighbor(PriorityQueue<KnnPoint> bestKNeighbors, KnnPoint neighbor, int k){
	int tripID = neighbor.getTripID();	
	
	for(KnnPoint kp:bestKNeighbors){
	    if(kp.getTripID() == tripID){
		if(kp.getDistance() > neighbor.getDistance()){
		    bestKNeighbors.remove(kp);
		    bestKNeighbors.add(neighbor);
		    return;
		}		
		else
		    return;
	    }
	}
	    	
	bestKNeighbors.add(neighbor);
	
	while(bestKNeighbors.size() > k)
	    bestKNeighbors.poll();    
    }
   
    private void searchTree(KdTreeNode node, PointFeatures point, PriorityQueue<KnnPoint> bestKNeighbors, int k, double[] distSoFar){	 
	if(node == null) return;
	
	double dist = featureSet.distance(node.getValue().getFeatures(),point);
	node.getValue().setDistance(dist);
	
	if(dist < kBestDist){    
	    addNeighbor(bestKNeighbors, node.getValue(),k);
	    if(bestKNeighbors.size() == k )
		kBestDist = bestKNeighbors.peek().getDistance();
	}
	    
	double pointAxisValue = getValue(point, node.getSplitType());
	double nodeAxisValue = getValue(node.getValue().getFeatures(), node.getSplitType());
	boolean leftBranch = pointAxisValue < nodeAxisValue;
	KdTreeNode branch = leftBranch ? node.getLeftSubtree() : node.getRightSubtree();	
	searchTree(branch, point, bestKNeighbors, k, distSoFar);
	
	double axialDist = featureSet.axialDistance(node.getValue().getFeatures(),point, node.getSplitType());
	distSoFar[node.getSplitType()] = axialDist;
	double distToSpace = distSoFar[0];
	for(int i=1;i<distSoFar.length;i++)
	    distToSpace+=distSoFar[i];
	
	if(distToSpace <= kBestDist){
	    branch = leftBranch ?  node.getRightSubtree() : node.getLeftSubtree();
	    searchTree(branch, point, bestKNeighbors, k, distSoFar.clone());
	}
}
    
    private int select(List<KnnPoint> points, int left, int right, int k, int splitType) {
	while(true){	    
	    int pivotIndex = (left+right)/2;//randomGenerator.nextInt(right-left+1)+left;
	    int pivotNewIndex = partition(points, left, right, pivotIndex, splitType);
	    if (k == pivotNewIndex)
		return k;
	    else if (k < pivotNewIndex)
		right = pivotNewIndex-1;
	    else
		left = pivotNewIndex+1;
	}
    }
        
    private int partition(List<KnnPoint> points, int left, int right, int pivot, int splitType){
	double pivotValue = getValue(points.get(pivot).getFeatures(),splitType);
	swap(points, pivot, right);
	int storeIndex = left;
	for(int i = left; i <= right-1;i++){
	    if(getValue(points.get(i).getFeatures(),splitType) < pivotValue){
		swap(points, storeIndex, i);
		storeIndex++;
	    }
	}
	swap(points, right, storeIndex);
	return storeIndex;
    }
    
    private void swap(List<KnnPoint> points, int x, int y){
	KnnPoint temp = points.get(x);
	points.set(x, points.get(y));
	points.set(y, temp);
    }
    
    private double distance(PointFeatures one, PointFeatures two){
	return featureSet.distance(one, two);
    }
    private double getValue(PointFeatures pf, int split){
	return featureSet.getValue(pf, split);
    }
    
    private double getWeight(int split){
	return featureSet.getWeight(split);
    }
}

class KnnComparator implements Comparator<KnnPoint>{
    private final PointFeatures point;
    private final KdTreeFeatureSet featureSet;
    
    public KnnComparator(PointFeatures p, KdTreeFeatureSet fs){
	point = p;
	featureSet = fs;
    }
    @Override
    public int compare(KnnPoint p1, KnnPoint p2) {
	double d1 = featureSet.distance(point, p1.getFeatures());
	double d2 = featureSet.distance(point, p2.getFeatures());
	
	return Double.compare(d2, d1);
    }
    
}

