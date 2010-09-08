package org.chargecar.algodev;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.chargecar.prize.util.PointFeatures;

public class KdTree {
    private final KdTreeNode root;
    private final Random randomGenerator = new Random();
    private static int splitFeatures = 2;
    
    public KdTree(List<KnnPoint> points){
	root = buildTree(points, 0);
    }
    
    private KdTreeNode buildTree(List<KnnPoint> points, int splitType){
	splitType = splitType % splitFeatures;
	KdTreeNode node;
	if(points.size() == 0) return null;
	else if(points.size() == 1){	    
	    KnnPoint point = points.get(0);
	    node = new KdTreeNode(point, null, null, splitType);	    
	}
	else{	    
	    int pivot = select(points, 0, points.size()-1, (int)((points.size()+1)/2), splitType);
	    //int pivot = select2(points, 0, points.size()-1, 0.35, true);
	    //int pivot = select3(points, 0, points.size()-1,true);
	    KdTreeNode leftSubtree = buildTree(points.subList(0, pivot), splitType++);
	    KdTreeNode rightSubtree = buildTree(points.subList(pivot+1, points.size()), splitType++);
	    node = new KdTreeNode(points.get(pivot), leftSubtree, rightSubtree, splitType);
	}
	return node;
    }
    
    public double getBestEstimate(ExtendedPointFeatures point, int k){
	List<KnnPoint> neighbors = new ArrayList<KnnPoint>();
	return searchTree(root, point, null, neighbors).getGroundTruth();
//	double flowEst = 0.0;
//	for(int i=0;i<k;i++){
//	    neighbors.add(searchTree(root, point, null, neighbors));
//	    flowEst += neighbors.get(i).getGroundTruth();
//	}
//	return flowEst / k;
	
	//	double bestDist = Double.MAX_VALUE;
	//	double bestEst = 0.0;
	//	for(int i=0;i<k;i++){
	//	    double dist = featureDist(point,neighbors.get(i));
	//	    if(dist < bestDist){
	//		bestEst = neighbors.get(i).getGroundTruth();
	//		bestDist = dist;
	//	    }
	//	}
	//	return bestEst;
	
    }
    
    private double featureDist(ExtendedPointFeatures epf, KnnPoint point){
	double sdist = epf.getSpeedHistVar() - point.getFeatures().getSpeedHistVar();
	double adist = epf.getAccelHistVar() - point.getFeatures().getAccelHistVar();
	return sdist*sdist + adist*adist;
    }
    
    private KnnPoint searchTree(KdTreeNode node, ExtendedPointFeatures point, KnnPoint best, List<KnnPoint> exclusions){	 
	if(node == null) return best;
	else if(best == null || distance(node.getValue().getFeatures(),point) < distance(best.getFeatures(),point))
	    if(exclusions.contains(node.getValue())==false) best = node.getValue();
	double pointAxisValue = getValue(point, node.getSplitType());
	
	KdTreeNode branch = pointAxisValue < node.getSplitValue() ? node.getLeftSubtree() : node.getRightSubtree();
	best = searchTree(branch, point, best, exclusions);
	
	double axialdist = node.getSplitValue() - pointAxisValue;
	if(best == null || axialdist * axialdist <= distance(best.getFeatures(),point)){
	    branch =  pointAxisValue < node.getSplitValue() ?  node.getRightSubtree() : node.getLeftSubtree();
	    best = searchTree(branch, point, best, exclusions);
	}
	
	return best;
    }
    
    private double distance(ExtendedPointFeatures point1, ExtendedPointFeatures point2){
	double dist = 0.0;
	for(int i =0;i<splitFeatures;i++){
	    double temp = getValue(point1, i) - getValue(point2,i);
	    dist += temp*temp;
	}
	return dist;
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
    
    //    private int select2(List<KnnPoint> points, int left, int right, double alpha, boolean splitType) {
    //	try{
    //	    while(true){
    //		int pivotIndex = randomGenerator.nextInt(right-left+1)+left;
    //		int pivotNewIndex = partition(points, left, right, pivotIndex, splitType);
    //		if (pivotNewIndex / points.size() > alpha && pivotNewIndex / points.size() < (1-alpha))
    //		    return pivotNewIndex;
    //		else if (pivotNewIndex / points.size() > alpha)
    //		    right = pivotNewIndex-1;
    //		else
    //		    left = pivotNewIndex+1;
    //	    }}
    //	catch(NullPointerException x){
    //	    x.printStackTrace();
    //	    return 0;
    //	}
    //    }
    //    
    //    private int select3(List<KnnPoint> points, int left, int right, boolean splitType){
    //	List<KnnPoint> medians = new ArrayList<KnnPoint>();
    //	int sublistSize = 11;
    //	//if(sublistSize % 2 == 0) sublistSize++;
    //	for(int i = 0; i < 5;i++){
    //	    List<KnnPoint> sublist = new ArrayList<KnnPoint>();
    //	    for(int j = 0; j < sublistSize; j++){
    //		int index = randomGenerator.nextInt(points.size());
    //		sublist.add(points.get(index));
    //	    }
    //	    int med = select(sublist, 0, 10, 5, splitType);
    //	    medians.add(sublist.get(med));
    //	}
    //	KnnPoint pivot = medians.get(select(medians, 0, 4, 2, splitType));
    //	int pivotInd = points.indexOf(pivot);
    //	return partition(points, 0, points.size()-1, pivotInd,splitType);
    //    }
    
    
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
    
    public static double getValue(KnnPoint point, int splitType){
	return getValue(point.getFeatures(), splitType);
    }
    public static double getValue(ExtendedPointFeatures point, int splitType){
	switch(splitType)
	{
	case 0: return point.getSpeed();
	case 1: return point.getAcceleration();
	default: return 0.0;
	}
    }
}
