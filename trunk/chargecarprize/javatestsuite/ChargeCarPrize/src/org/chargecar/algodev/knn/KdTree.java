package org.chargecar.algodev.knn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.chargecar.prize.util.PointFeatures;

public class KdTree {
    private final List<Double> powers;
    private final KdTreeNode root;
    private final Random randomGenerator = new Random();
    private final KdTreeFeatureSet featureSet;
    private double bestDist = Double.MAX_VALUE;
    
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
    
    public List<Double> getBestEstimate(PointFeatures point, int k, int lookahead){
	List<KnnPoint> neighbors = new ArrayList<KnnPoint>();
	for(int i=0;i<k;i++){
	    bestDist = Double.MAX_VALUE;
	    neighbors.add(searchTree(root, point, null, neighbors));
	}
	return featureSet.estimate(point, neighbors, powers, lookahead);
    }
    
   
    private KnnPoint searchTree(KdTreeNode node, PointFeatures point, KnnPoint best, List<KnnPoint> exclusions){	 
	
	if(node == null) return best;
	else if(exclusions.contains(node.getValue())==false){
	    double dist = distance(node.getValue().getFeatures(),point);
	    if(best == null || dist < bestDist){
		best = node.getValue();
		bestDist = dist;
	    }
	}
	    
	double pointAxisValue = getValue(point, node.getSplitType());
	double nodeAxisValue = getValue(node.getValue().getFeatures(), node.getSplitType());
	boolean leftBranch = pointAxisValue < nodeAxisValue;
	KdTreeNode branch = leftBranch ? node.getLeftSubtree() : node.getRightSubtree();
	best = searchTree(branch, point, best, exclusions);
	
//	double axialdist = nodeAxisValue - pointAxisValue;
//        double axialWeight = getWeight(node.getSplitType());
//        axialdist = axialdist*axialdist*axialWeight;
	double axialdist = featureSet.axialDistance(node.getValue().getFeatures(),point, node.getSplitType());
	
	if(best == null || axialdist <= bestDist){
	    branch = leftBranch ?  node.getRightSubtree() : node.getLeftSubtree();
	    best = searchTree(branch, point, best, exclusions);
	}
	
	return best;
	}
    
    private int select(List<KnnPoint> points, int left, int right, int k, int splitType) {
	while(true){	    
	    int pivotIndex = randomGenerator.nextInt(right-left+1)+left;
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
