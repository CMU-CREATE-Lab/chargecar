package org.chargecar.algodev;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.chargecar.prize.util.PointFeatures;

public class KdTree {
    private final KdTreeNode root;
    private final Random randomGenerator = new Random();
    
    public KdTree(List<KnnPoint> points){
	root = buildTree(points, true);
    }
    
    private KdTreeNode buildTree(List<KnnPoint> points, boolean splitType){
	KdTreeNode node;
	System.out.print('*');
	if(points.size() == 1){	    
	    KnnPoint point = points.get(0);
	    node = new KdTreeNode(point, null, null, splitType);	    
	}
	else{	
 
	    //int pivot = select(points, 0, points.size()-1, (int)((points.size()-1)/2), splitType);
	    //int pivot = select2(points, 0, points.size()-1, 0.35, true);
	    int pivot = select3(points, 0, points.size()-1,true);
	    KdTreeNode leftSubtree = buildTree(points.subList(0, pivot), !splitType);
	    KdTreeNode rightSubtree = buildTree(points.subList(pivot+1, points.size()), !splitType);
	    node = new KdTreeNode(points.get(pivot), leftSubtree, rightSubtree, splitType);
	    leftSubtree.setParent(node);
	    rightSubtree.setParent(node);
	}
	return node;
    }
    
    public double getBestEstimate(PointFeatures point){
	return 0;
    }
    
    public static double computeDistance(PointFeatures pf, KnnPoint kp){
	double lat = pf.getLatitude() - kp.getFeatures().getLatitude();
	double lon = pf.getLongitude() - kp.getFeatures().getLongitude();
	return lat*lat + lon*lon;
	//return TripBuilder.Haversine(pf.getLatitude(), pf.getLongitude(), kp.getFeatures().getLatitude(), kp.getFeatures().getLongitude());
    }
    private int select(List<KnnPoint> points, int left, int right, int k, boolean splitType) {
	while(true){
	    int pivotIndex = randomGenerator.nextInt(right-left)+left;
	    int pivotNewIndex = partition(points, left, right, pivotIndex, splitType);
	    if (k == pivotNewIndex)
		return k;
	    else if (k < pivotNewIndex)
		right = pivotNewIndex-1;
	    else
		left = pivotNewIndex+1;
	}
    }
    
    private int select2(List<KnnPoint> points, int left, int right, double alpha, boolean splitType) {
	try{
	while(true){
	    int pivotIndex = randomGenerator.nextInt(right-left)+left;
	    int pivotNewIndex = partition(points, left, right, pivotIndex, splitType);
	    if (pivotNewIndex / points.size() > alpha && pivotNewIndex / points.size() < (1-alpha))
		return pivotNewIndex;
	    else if (pivotNewIndex / points.size() > alpha)
		right = pivotNewIndex-1;
	    else
		left = pivotNewIndex+1;
	}}
	catch(NullPointerException x){
	    x.printStackTrace();
	    return 0;
	}
    }
    
    private int select3(List<KnnPoint> points, int left, int right, boolean splitType){
	List<KnnPoint> medians = new ArrayList<KnnPoint>();
	int sublistSize = Math.max(11, (int)(points.size()/10)+1);
	if(sublistSize % 2 == 0) sublistSize++;
	for(int i = 0; i < 5;i++){
	    List<KnnPoint> sublist = new ArrayList<KnnPoint>();
	    for(int j = 0; j < sublistSize; j++){
		int index = randomGenerator.nextInt(points.size());
		sublist.add(points.get(index));
	    }
	    int med = select(sublist, 0, sublistSize-1, (sublistSize-1)/2, splitType);
	    medians.add(points.get(med));
	}
	int pivot = select(medians, 0, 10, 5, splitType);
	return partition(points, 0, points.size()-1, pivot,splitType);
    }
    
    
    private int partition(List<KnnPoint> points, int left, int right, int pivot, boolean splitType){
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
    
    private double getValue(KnnPoint point, boolean splitType){
	if(splitType) return point.getFeatures().getLatitude();
	else return point.getFeatures().getLongitude();
    }
}
