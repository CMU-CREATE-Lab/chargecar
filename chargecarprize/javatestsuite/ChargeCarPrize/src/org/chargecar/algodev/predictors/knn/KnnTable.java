package org.chargecar.algodev.predictors.knn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.chargecar.prize.util.PointFeatures;

public class KnnTable implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final List<KnnPoint> points;
    private final List<Double> powers;
    
    public KnnTable(){
	points = new ArrayList<KnnPoint>();
	powers = new ArrayList<Double>();
    }
    
    public KnnTable(List<KnnPoint> inpPoints, List<Double> inpPowers){
	this.points = new ArrayList<KnnPoint>(inpPoints);
	this.powers = new ArrayList<Double>(inpPowers);	
    }
    
    public void addPoint(PointFeatures pf, double powerDemand){
	powers.add(powerDemand);
	int index = powers.size()-1;
	points.add(new KnnPoint(pf,index));	
    }
    
    public List<KnnPoint> getKnnPoints(){
	return points;
    }
    
    public List<Double> getPowers(){
	return powers;
    }
    
    public void endTrip(){
	powers.add(null);
    }
    
    
    
}
