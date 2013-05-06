package org.chargecar.algodev.controllers;

import java.util.List;
import java.util.Map;

import org.chargecar.algodev.controllers.MDPPolynomial.ControlResult;
import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.util.Trip;

public class DPPolyController implements Controller {

    private final int[] U;
    private final Map<Integer, double[][]> tripMap;
    
    public DPPolyController(int[] controls, Map<Integer, double[][]> tripMap){
	this.U = controls;
	this.tripMap = tripMap;
    }
    
    @Override
    public double getControl(List<Prediction> predictedDuties,
	    BatteryModel battery, BatteryModel cap, int periodMS, double powerDemand) {
	double[] uValues = new double[U.length];
	double percentCharge = cap.getWattHours() / cap.getMaxWattHours();
	
	ControlResult[] cResults = new ControlResult[U.length];
	
	 for(int i=0;i<U.length;i++){
		int control = U[i];
		cResults[i] = MDPPolynomial.testControl(cap, powerDemand, control);
	 }
	
	for(int i = 0;i<uValues.length;i++){
	    uValues[i]=0;
	}
	
	for(Prediction p : predictedDuties){
	    double[][] coefficients = tripMap.get(p.getTripID());
	    
	    for(int i=0;i<U.length;i++){
		//doesnt know lambda for first step... OK
		double value = cResults[i].cost + MDPPolynomial.calculateValue(coefficients[p.getTimeIndex()+1], cResults[i].pCharge);		
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

    @Override
    public void addTrip(Trip t) {
	// TODO Auto-generated method stub
	
    }    
}
