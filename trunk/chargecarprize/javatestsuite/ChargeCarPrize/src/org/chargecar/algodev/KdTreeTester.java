package org.chargecar.algodev;

import java.util.ArrayList;
import java.util.List;

import org.chargecar.algodev.knn.FullFeatureSet;
import org.chargecar.algodev.knn.KdTree;
import org.chargecar.algodev.knn.KnnPoint;
import org.chargecar.prize.util.PointFeatures;

public class KdTreeTester {
    public static void main(String[] args) {
	List<KnnPoint> points = new ArrayList<KnnPoint>();
	for(int i=0;i<5;i++){
	    points.add(new KnnPoint(new PointFeatures(Math.random(),0,0,0,0,0,0,0,0, 0, null), 0));
	}
	KdTree tree = new KdTree(points, null, new FullFeatureSet());	
	for(int i=0;i<points.size();i++)
	    tree.getBestEstimate(points.get(i).getFeatures(), 1, 0);
	System.out.println(tree.countNodes());
    }
    
}
