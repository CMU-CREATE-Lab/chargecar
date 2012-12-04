package org.chargecar.algodev.controllers;

import java.util.List;
import java.util.Map;

import org.chargecar.algodev.controllers.MultipleModelDP.ControlResult;
import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.battery.BatteryModel;

public class DPOptController extends Controller {

    private final int[] U;
    private final Map<Integer, double[][]> tripMap;
    
    public DPOptController(int[] controls, Map<Integer, double[][]> tripMap){
	this.U = controls;
	this.tripMap = tripMap;
    }
    
    @Override
    public double getControl(List<Prediction> predictedDuties,
	    BatteryModel battery, BatteryModel cap, int periodMS, double powerDemand) {
	double[] uValues = new double[U.length];
	double percentCharge = cap.getWattHours() / cap.getMaxWattHours();
	
	for(int i = 0;i<uValues.length;i++){
	    uValues[i]=0;
	}
	
	for(Prediction p : predictedDuties){
	    double[][] valueFunction = tripMap.get(p.getTripID());
	    int times = valueFunction[0].length;
	    int X = valueFunction.length / times;
	    
	    int index = (int)(percentCharge*X);
	    if(index == X) index = X-1;		    
 
	    for(int i=0;i<U.length;i++){
		int control = U[i];
		ControlResult result = MultipleModelDP.testControl(cap, powerDemand, control, X);
		//doesnt know lambda for first step... OK
		double value = result.cost + valueFunction[result.index][1];		
		uValues[i] += p.getWeight()*value;
	    }
	    
	}
	
	double minValue = Double.MAX_VALUE;
	double control = 0;
	
	for(int i=0;i<uValues.length;i++){
	    double value = uValues[i];
	    if(value < minValue){
		minValue = value;
		control = U[i];
	    }
	}
	return control;
    }    
}
