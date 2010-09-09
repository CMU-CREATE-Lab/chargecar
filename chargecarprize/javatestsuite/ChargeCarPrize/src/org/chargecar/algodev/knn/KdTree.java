package org.chargecar.algodev.knn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.chargecar.algodev.ExtendedPointFeatures;
import org.chargecar.prize.util.PointFeatures;

public class KdTree {
    private final KdTreeNode root;
    private final Random randomGenerator = new Random();
    private final KdTreeFeatureSet featureSet;
    
    public KdTree(List<KnnPoint> points, KdTreeFeatureSet featureSet){
	this.featureSet = featureSet;
	root = buildTree(points, 0);	
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
	    int pivot = select(points, 0, points.size()-1, (int)((points.size()+1)/2), splitType);
	    KdTreeNode leftSubtree = buildTree(points.subList(0, pivot), splitType+1);
	    KdTreeNode rightSubtree = buildTree(points.subList(pivot+1, points.size()), splitType+1);
	    node = new KdTreeNode(points.get(pivot), leftSubtree, rightSubtree, splitType);
	}
	return node;
    }
    
    public double getBestEstimate(ExtendedPointFeatures point, int k){
	List<KnnPoint> neighbors = new ArrayList<KnnPoint>();
	for(int i=0;i<k;i++){
	    neighbors.add(searchTree(root, point, null, neighbors));
	}
	return featureSet.estimate(point, neighbors);
    }
    
    private KnnPoint searchTree(KdTreeNode node, ExtendedPointFeatures point, KnnPoint best, List<KnnPoint> exclusions){	 
	if(node == null) return best;
	else if(best == null || distance(node.getValue().getFeatures(),point) < distance(best.getFeatures(),point))
	    if(exclusions.contains(node.getValue())==false) best = node.getValue();
	double pointAxisValue = getValue(point, node.getSplitType());
	
	KdTreeNode branch = pointAxisValue < getValue(node.getValue(), node.getSplitType()) ? node.getLeftSubtree() : node.getRightSubtree();
	best = searchTree(branch, point, best, exclusions);
	
	double axialdist = getValue(node.getValue(), node.getSplitType()) - pointAxisValue;
	if(best == null || axialdist * axialdist <= distance(best.getFeatures(),point)){
	    branch =  pointAxisValue < getValue(node.getValue(), node.getSplitType()) ?  node.getRightSubtree() : node.getLeftSubtree();
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
	double pivotValue = getValue(points.get(pivot),splitType);
	swap(points, pivot, right);
	int storeIndex = left;
	for(int i = left; i <right-1;i++){
	    if(getValue(points.get(i),splitType) < pivotValue){
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
    
    private double distance(ExtendedPointFeatures one, ExtendedPointFeatures two){
	return featureSet.distance(one, two);
    }
    private double getValue(ExtendedPointFeatures epf, int split){
	return featureSet.getValue(epf, split);
    }
    private double getValue(KnnPoint kp, int split){
	return featureSet.getValue(kp, split);
    }
}
