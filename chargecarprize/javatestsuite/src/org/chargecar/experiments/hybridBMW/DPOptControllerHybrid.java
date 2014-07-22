package org.chargecar.experiments.hybridBMW;

import java.util.List;
import java.util.Map;

import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.experiments.hybridBMW.MDPValueGraphHybrid.ControlResult;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.util.Trip;

public class DPOptControllerHybrid {
    
    private final int[] U;
    private final Map<Integer, double[][]> tripMap;
    private final MDPValueGraphHybrid mmdpOpt;
    
    public DPOptControllerHybrid(int[] controls,
	    Map<Integer, double[][]> tripMap, MDPValueGraphHybrid mvg) {
	this.U = controls;
	this.tripMap = tripMap;
	this.mmdpOpt = mvg;
    }
    
    public void addTrip(Trip t) {
	this.tripMap.put(t.hashCode(), mmdpOpt.getValues(t.getPoints()));
    }
    
    public int getControl(List<Prediction> predictedDuties, BatteryModel batt,
	    BatteryModel capnull, int periodMS, double powerDemand, int lastControl) {
	
	double[] uExpectedCostToGo = new double[U.length];
	double percentCharge = batt.getWattHours() / batt.getMaxWattHours();
	
	ControlResult[] cResults = new ControlResult[U.length];
	
	for (int i = 0; i < U.length; i++) {
	    //test each possible control and return the resulting controlResults for each
	    int control = U[i];
	    cResults[i] = MDPValueGraphHybrid.testControl(batt, powerDemand,
		    control);
	    
	    //initialize expected cost to go's to be 0
	    uExpectedCostToGo[i] = 0.00001*Math.abs(U[i]-lastControl);
	}
	
	for (Prediction p : predictedDuties) {
	    //get the trip map MDP for the prediction match
	    //System.out.println("Debug prediction match: "+p.getTripID()+" Index: "+p.getTimeIndex()+" Weight: "+p.getWeight());
	    double[][] valueFunction = tripMap.get(p.getTripID());
	    
	    // notes, 2D array. .length gives only first dimension (chargeStates
	    // in this case).
	    int X = valueFunction.length;
	   // System.out.println("Charge States: "+X);
	    //find closest index for starting charge in the MDP state
	    int index = (int) (percentCharge * X);
	    if (index == X) index = X - 1;
	   // System.out.println("Time index pred: "+p.getTimeIndex()+" Charge State: "+index+"//"+X+" From pct: "+ percentCharge);
	    
	    for (int i = 0; i < U.length; i++) {
		// doesnt know lambda for first step... OK
		
		//initialize total cost to the one step cost from the control
		double costToGo = cResults[i].cost;
		
		//find the resulting charge state (0 -> 1) in index space (0 -> X-1)
		//it will be a double, so interpolate between the closest indexes
		//to get a value estimate for that state (e.g. 1.5 is halfway between index 1 and 2)
		double chargeState = cResults[i].pCharge * X;
		
		int floor = (int) Math.floor(chargeState);
		floor = Math.min(Math.max(floor, 0), X - 1);
		int ceil = (int) Math.ceil(chargeState);
		ceil = Math.max(Math.min(ceil, X - 1), 0);
		
		double fVal = valueFunction[floor][p.getTimeIndex() + 1];
		
		//System.out.println("Control "+U[i]+" Result: "+pd.format(chargeState) + " between "+floor+"-"+ceil);
		
		
		//add interpolated CostToGo to the one step cost already stored in CTG
		if (floor == ceil) {
		    costToGo += fVal;
		} else {
		    double cVal = valueFunction[ceil][p.getTimeIndex() + 1];
		    costToGo += (fVal + ((cVal - fVal) * (chargeState - floor) / (ceil - floor)));
		}
		
		//update the expected CostToGo for this control (U[i]), weighted by the prediction weight
		//when all predictions results/weights combined, we will have an expectation of CostToGo
		uExpectedCostToGo[i] += p.getWeight() * costToGo;
	    }
	    
	   /* DecimalFormat pd =new DecimalFormat("0.000E0");
	    System.out.print("Index "+p.getTimeIndex()+" Costs: ");
	    for(int i=0;i<U.length;i++){
		System.out.print(pd.format(uExpectedCostToGo[i])+" ");
		
	    }
	    System.out.print("\n");*/
	    
	}
	
	double minCTG = Double.MAX_VALUE;
	int control = 0;
	
	//determine which control had the lowest costToGo and return it
	for (int i = 0; i < uExpectedCostToGo.length; i++) {
	    double costToGo = uExpectedCostToGo[i];
	    if (costToGo < minCTG) {
		minCTG = costToGo;
		control = U[i];
	    }
	}
	return control;
    }
}
