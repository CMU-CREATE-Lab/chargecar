package org.chargecar.algodev.controllers;

import java.util.ArrayList;
import java.util.List;

import org.chargecar.algodev.predictors.Prediction;

public class ApproximateAnalytic extends Controller {
    public double getControl(List<Prediction> duties, double capCharge){
	List<Double> powers = duties.get(0).getPowers();
	
	List<Double> cumulativeSum = new ArrayList<Double>();
	List<Integer> timeStamps = new ArrayList<Integer>();
	List<Double> rates = new ArrayList<Double>();
	
	//double sum = -modelCap.getMinPowerDrawable(pf.getPeriodMS());
	double sum = -capCharge;
	int timesum = 0;

	for(int i=0;i<powers.size();i++){	    
	    sum += powers.get(i);
	    timesum += 1000;
	    cumulativeSum.add(sum);
	    timeStamps.add(timesum);
	    rates.add(1000*sum/timesum);
	}
	
	double maxRate = Double.NEGATIVE_INFINITY;
	for(int i = 0;i<rates.size();i++){
	    if(rates.get(i) > maxRate){
		maxRate = rates.get(i);
	    }
	}
	
	return maxRate;
	
    }
}
