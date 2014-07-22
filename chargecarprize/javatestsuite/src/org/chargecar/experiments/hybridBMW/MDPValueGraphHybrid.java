package org.chargecar.experiments.hybridBMW;

import java.util.List;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.battery.SimpleBattery;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;

//TODO TABULATE DYNAMICS!!!!!

public class MDPValueGraphHybrid{
    
    final int[] U;
    final int X; //how many charge states 
    final double lambda;//discount factor
    final BatteryModel batt;
    
    public MDPValueGraphHybrid(int[] controls, int stateBuckets, double discountRate, BatteryModel batt){
	this.U = controls;
	this.X = stateBuckets;
	this.lambda = discountRate;
	this.batt = batt;
    }
    
    public double[][] getValues(List<PointFeatures> points){
	int T = points.size()+1; 
	
	final double[][] valueFunction = new double[X][T];
	final BatteryModel[] xstates = new BatteryModel[X];
	
	
	for(int x=0;x<X;x++){
	    xstates[x]=new SimpleBattery(batt.getMaxWattHours(),(double)x*batt.getMaxWattHours()/X,batt.getVoltage());
	    for(int t=0;t<T-1;t++){
		valueFunction[x][t] = Double.MAX_VALUE;
	    }
	    valueFunction[x][T-1] = 0;
	}
	
	//djikstra shortest path search
	//TODO change to A*?
	for(int t=T-2;t>=0;t--){
	    double power = 0;
	    power = points.get(t).getPowerDemand();
	    for(int x=0;x<X;x++){
		BatteryModel state = xstates[x];
		double minValue = Double.MAX_VALUE;
		for(int u=0;u<U.length;u++){
		    int control = U[u];
		    ControlResult result = testControl(state, power, control);
		    
		    double value = result.cost;

                    double chargeState = result.pCharge*X;
                    int floor = (int)Math.floor(chargeState);
                    floor = Math.min(Math.max(floor,0),X-1);
                    int ceil = (int)Math.ceil(chargeState);
                    ceil = Math.max(Math.min(ceil,X-1),0);

                    double fVal = valueFunction[floor][t+1];


                    if(floor==ceil){
                        value += lambda*fVal;
                    }
                    else{
                        double cVal = valueFunction[ceil][t+1];
                        value += lambda*(fVal + ((cVal - fVal)*(chargeState - floor)/(ceil-floor) ));
                    }		    
		    
		    if(value < minValue){
			minValue = value;
		    }
		}
		valueFunction[x][t] = minValue;
	    }
	}
	
	return valueFunction;
	/*
	double percentCharge = cap.getWattHours() / cap.getMaxWattHours();
	int index = (int)(percentCharge*X);
	if(index == X) index = X-1;
	
	return controls[index][0];
*/
	}
    
    public static ControlResult testControl(BatteryModel batteryState, double powerDraw, int control){
	BatteryModel modelBatt = batteryState.createClone();
	
	double cost = 0.0;
	
	double wattsDemanded = powerDraw;
	int periodMS = 1000;

	double motorWatts = wattsDemanded-control;
		
	try {
	    modelBatt.drawPower(motorWatts, periodMS);
	} catch (PowerFlowException e) {
	    return new ControlResult(0,Double.MAX_VALUE);
	}

	double percentCharge = modelBatt.getWattHours() / modelBatt.getMaxWattHours();
	
	cost = CostFunction.getCost(control);
		
	return new ControlResult(percentCharge, cost);
    }
    
    public static class ControlResult {
	public final double pCharge;
	public final double cost;
	public ControlResult(double pc,double c){
	    pCharge = pc;
	    cost=c;
	}
    }

}
