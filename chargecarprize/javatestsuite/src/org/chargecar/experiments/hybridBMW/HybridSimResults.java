package org.chargecar.experiments.hybridBMW;

import java.util.ArrayList;
import java.util.List;

public class HybridSimResults {
    private List<Double> costs;
    private double totalCost;
    
    public HybridSimResults(){
	 costs = new ArrayList<Double>();
	 totalCost = 0;
    }
    
    public void addTrip(double cost){
	totalCost += cost;
	costs.add(cost);
    }
    
    public double getTotalCost(){
	return totalCost;
    }
    
    public List<Double> getCosts(){
	return costs;
    }
}
