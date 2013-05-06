package org.chargecar.algodev.controllers;

import java.util.List;
import java.util.Map;

import org.chargecar.algodev.controllers.MDPValueGraph.ControlResult;
import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.util.Trip;

public class DPOptController implements Controller {

    private final int[] U;
    private final Map<Integer, double[][]> tripMap;
    private final MDPValueGraph mmdpOpt;
    
    public DPOptController(int[] controls, Map<Integer, double[][]> tripMap, MDPValueGraph mvg){
	this.U = controls;
	this.tripMap = tripMap;	
	this.mmdpOpt = mvg;
    }
    public void addTrip(Trip t){	
	this.tripMap.put(t.hashCode(), mmdpOpt.getValues(t.getPoints()));
    }
    
    public double getControl(List<Prediction> predictedDuties,
	    BatteryModel battery, BatteryModel cap, int periodMS, double powerDemand) {
	double[] uValues = new double[U.length];
	double percentCharge = cap.getWattHours() / cap.getMaxWattHours();
	
	ControlResult[] cResults = new ControlResult[U.length];
	
	 for(int i=0;i<U.length;i++){
		int control = U[i];
		cResults[i] = MDPValueGraph.testControl(cap, powerDemand, control);
	 }
	
	for(int i = 0;i<uValues.length;i++){
	    uValues[i]=0;
	}
	
	for(Prediction p : predictedDuties){
	    double[][] valueFunction = tripMap.get(p.getTripID());
	    
	    //notes, 2D array.  .length gives only first dimension (chargeStates in this case).
	    int X = valueFunction.length;
	    	    
	    int index = (int)(percentCharge*X);
	    if(index == X) index = X-1;		    
 
	    for(int i=0;i<U.length;i++){
		//doesnt know lambda for first step... OK
		double value = cResults[i].cost;
                double chargeState = cResults[i].pCharge*X;
                
                int floor = (int)Math.floor(chargeState);
                floor = Math.min(Math.max(floor,0),X-1);
                int ceil = (int)Math.ceil(chargeState);
                ceil = Math.max(Math.min(ceil,X-1),0);

                double fVal = valueFunction[floor][p.getTimeIndex()+1];

                if(floor==ceil){
                    value += fVal;
                }
                else{
                    double cVal = valueFunction[ceil][p.getTimeIndex()+1];
                    value += (fVal + ((cVal - fVal)*(chargeState - floor)/(ceil-floor) ));
                }
		
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
