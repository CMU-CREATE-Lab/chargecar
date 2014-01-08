package org.chargecar.algodev;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.chargecar.algodev.knn.FullFeatureSet;
import org.chargecar.algodev.knn.KdTree;
import org.chargecar.algodev.knn.KnnPoint;
import org.chargecar.prize.util.PointFeatures;

public class KdTreeTester {
    private final static int N = 250000; //knn History Size
    private final static int M = 20000; //Testing set size
    private final static int k = 7; //number of neighbors to search
    public static void main(String[] args) {
	List<KnnPoint> points = new ArrayList<KnnPoint>();
	System.out.print("Creating "+N+" random Points...");
	for(int i=0;i<N;i++){
	    points.add(new KnnPoint(getRandomPoint(), 0,0));
	}
	System.out.print("complete.\n");
	System.out.print("Building tree...");
	KdTree tree = new KdTree(points, new FullFeatureSet());
	System.out.print("complete.\n");
	System.out.println("Matching "+M+" points with "+k+" neighbors each...");
	for(int i=0;i<M;i++){
	    tree.getNeighbors(getRandomPoint(), k, null, false);
	    if(i%1000 == 0){
		System.out.println(i+"...");
	    }
	}
	System.out.println("Complete.");
    }
    
    private static PointFeatures getRandomPoint(){
	Calendar randTime = Calendar.getInstance();
	randTime.setTimeInMillis((long)(Math.random()*10e7));
	return new PointFeatures(Math.random(),Math.random(),Math.random(),Math.random(),Math.random(),Math.random(),Math.random(),Math.random(),Math.random(), 1000, randTime);
    }
    
}
