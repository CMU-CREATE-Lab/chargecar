package org.chargecar.experiments.hybridBMW;

import java.util.List;
import java.util.Map;

import org.chargecar.algodev.controllers.Controller;
import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.experiments.hybridBMW.MDPValueGraphHybrid.ControlResult;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.util.Trip;

public class DPOptControllerHybrid {

    private final int[] U;
    private final Map<Integer, double[][]> tripMap;
    private final MDPValueGraphHybrid mmdpOpt;
    private final Map<Integer,Double> costFunction;
    
    public DPOptControllerHybrid(int[] controls, Map<Integer, Double> costFunction, Map<Integer, double[][]> tripMap, MDPValueGraphHybrid mvg){
	this.U = controls;
	this.tripMap = tripMap;	
	this.mmdpOpt = mvg;
	this.costFunction = costFunction;
    }
    public void addTrip(Trip t){	
	this.tripMap.put(t.hashCode(), mmdpOpt.getValues(t.getPoints()));
    }
    
    public int getControl(List<Prediction> predictedDuties,
	    BatteryModel batt, BatteryModel capnull, int periodMS, double powerDemand) {
	double[] uValues = new double[U.length];
	double percentCharge = batt.getWattHours() / batt.getMaxWattHours();
	
	ControlResult[] cResults = new ControlResult[U.length];
	
	 for(int i=0;i<U.length;i++){
		int control = U[i];
		cResults[i] = MDPValueGraphHybrid.testControl(batt, costFunction, powerDemand, control);
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
	int control = 0;
	
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

