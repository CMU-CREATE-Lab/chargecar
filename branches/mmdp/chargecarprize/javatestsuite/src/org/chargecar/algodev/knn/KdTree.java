package org.chargecar.algodev.knn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.util.PointFeatures;

public class KdTree {
    private KdTreeNode root;
    private final Random randomGenerator = new Random();
    private final KdTreeFeatureSet featureSet;
    private double kBestDist = Double.MAX_VALUE;
    private int treeSize;
    private int balancedSize;
    private List<KnnPoint> pointList;
    
    public KdTree(List<KnnPoint> points, KdTreeFeatureSet featureSet){
	this.featureSet = featureSet;
	
	pointList = new ArrayList<KnnPoint>();
	if(points != null) pointList.addAll(points);
	points = null;
	this.root = buildTree(pointList, 0);
	if(root == null) treeSize = 0;
	else treeSize = root.countNodes();
	balancedSize = treeSize;
    }
    
    public void addNode(KnnPoint point){
	if(this.root == null){	    
	    this.root = new KdTreeNode(point, null, null, 0);	    
	}
	else{
	    this.root.addChild(point, featureSet);
	}
	pointList.add(point);
	if(pointList.size() > (balancedSize+2000)){// && pointList.size() > 1000){
	    System.out.println("Rebalancing tree of size "+pointList.size());
	//    clearTree(root);
	    this.root = null;
	    this.root = buildTree(pointList, 0);
	    this.balancedSize = pointList.size();
	    System.out.println("Complete.");
	}
    }    
    
    /*private void clearTree(KdTreeNode node){
	if(node == null) return;
	else{
	    clearTree(node.getLeftSubtree());
	    clearTree(node.getRightSubtree());
	    node.
	}
    }*/
    
    private KdTreeNode buildTree(List<KnnPoint> points, int splitType){
	splitType = splitType % featureSet.getFeatureCount();
	KdTreeNode node;
	KnnPoint point;
	KdTreeNode leftSubtree = null;
	KdTreeNode rightSubtree = null;
	if(points == null || points.size() == 0) return null;
	else if(points.size() == 1){	    
	    point = points.get(0);
	    //points.set(0, null);
	}
	else{ 
	    int pivot = select(points, 0, points.size()-1, (int)(points.size()/2), splitType);
	    point = points.get(pivot);
	    //points.set(pivot, null);
	    leftSubtree = buildTree(new ArrayList<KnnPoint>(points.subList(0, pivot)), splitType+1);
	    rightSubtree = buildTree(new ArrayList<KnnPoint>(points.subList(pivot+1, points.size())), splitType+1);
	    
	}
	point.setDistance(-1);
	node = new KdTreeNode(point, leftSubtree, rightSubtree, splitType);
	return node;
    }
    
    public int countNodes(){
	return root.countNodes();
    }
    
    public Prediction getNeighbor(PointFeatures searchPoint){
	Comparator<KnnPoint> comp = new KnnComparator(searchPoint,featureSet);
	PriorityQueue<KnnPoint> neighbors = new PriorityQueue<KnnPoint>(1,comp);
	kBestDist = Double.MAX_VALUE;

	searchTree(root, searchPoint, neighbors, 1, new double[featureSet.getFeatureCount()]);
	
	KnnPoint kp = neighbors.poll();
	
	if(kp != null)
	 return new Prediction(1/(kp.getDistance()+0.01),kp.getTripID(),kp.getTimeIndex(),kp);
	else return null;	
    }
    
    public List<Prediction> getNeighbors(PointFeatures searchPoint, int k, List<Prediction> previousNeighbors, boolean trained){//, int lookahead){
	Comparator<KnnPoint> comp = new KnnComparator(searchPoint,featureSet);
	PriorityQueue<KnnPoint> neighbors = new PriorityQueue<KnnPoint>(k+2,comp);
	kBestDist = Double.MAX_VALUE;
	
	/*if(previousNeighbors != null){
	    for(Prediction p: previousNeighbors){
		KnnPoint kp = p.getPoint();
		double dist = featureSet.distance(kp.getFeatures(),searchPoint);
		kp.setDistance(dist);
		if(dist < kBestDist){    
		    addNeighbor(neighbors, kp, k);
		    if(neighbors.size() == k )
			kBestDist = neighbors.peek().getDistance();
		}		 
	    }
	}*/
	
	searchTree(root, searchPoint, neighbors, k+1, new double[featureSet.getFeatureCount()]);

	List<Prediction> predictions = new ArrayList<Prediction>();
	
	KnnPoint kp = neighbors.poll();
	
	while(kp != null){
	    Prediction p = new Prediction(1/(kp.getDistance()+0.01),kp.getTripID(),kp.getTimeIndex(),kp);
	    predictions.add(p);
	    kp = neighbors.poll();
	}
	
	if(predictions.size() == 0) return predictions;
	
	double weight = predictions.get(predictions.size()-1).getWeight();
	
	if(trained && weight > 99) //Remove best match -- perfect match of the search point
	    return predictions.subList(0, predictions.size() - 1);
	else //remove worst match, only want K predictions
	    return predictions.subList(1, predictions.size());
	
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
	
	searchTree(branch, point, bestKNeighbors, k, distSoFar.clone());
	
	double axialDist = featureSet.axialDistance(node.getValue().getFeatures(),point, node.getSplitType());
	distSoFar[node.getSplitType()] = axialDist;
	
	double distToSpace = 0;
	for(int i=0;i<distSoFar.length;i++)
	    distToSpace+=distSoFar[i];
	
	if(distToSpace <= kBestDist){

	    branch = leftBranch ?  node.getRightSubtree() : node.getLeftSubtree();
	    searchTree(branch, point, bestKNeighbors, k, distSoFar.clone());
	}
}
    
    private int select(List<KnnPoint> points, int left, int right, int k, int splitType) {
	while(true){	    
	    //int pivotIndex = (left+right)/2;
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

class KnnComparator implements Comparator<KnnPoint>{
    private final PointFeatures point;
    private final KdTreeFeatureSet featureSet;
    
    public KnnComparator(PointFeatures p, KdTreeFeatureSet fs){
	point = p;
	featureSet = fs;
    }
    @Override
    public int compare(KnnPoint p1, KnnPoint p2) {
	double d1 = p1.getDistance();//featureSet.distance(point, p1.getFeatures());
	double d2 = p2.getDistance();//featureSet.distance(point, p2.getFeatures());
	
	return Double.compare(d2, d1);
    }
    
}

