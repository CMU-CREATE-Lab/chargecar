package org.chargecar.algodev.controllers;

import java.util.List;

import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.battery.SimpleCapacitor;
import org.chargecar.prize.util.PowerFlowException;

//TODO TABULATE DYNAMICS!!!!!

public class MultipleModelDP extends Controller {
    
    final int[] U;// = new int[]{0,64,256,512,768,1024,1536,2048,2516,3072,3524,4096,5122,5500,6134,6600,7124,7600,8192,9122,10020,12000};
    
    final int X; //how many charge states 
    final double lambda;//discount factor
    
    public MultipleModelDP(int[] controls, int stateBuckets, double discountRate){
	this.U = controls;
	this.X = stateBuckets;
	this.lambda = discountRate;
    }
    
    
    
    public double getControl(List<Prediction> duties, BatteryModel battery, BatteryModel cap, int periodMS){
	double[] uValues = new double[U.length];
	double percentCharge = cap.getWattHours() / cap.getMaxWattHours();
	int index = (int)(percentCharge*X);
	if(index == X) index = X-1;
	
	for(int i = 0;i<uValues.length;i++){
	    uValues[i]=0;
	}
	
	for(Prediction p : duties){
	    //may need to normalize due to T
	    double[][] valueFunction = getValues(p,cap);
	    for(int i=0;i<U.length;i++){
		int control = U[i];
		ControlResult result = testControl(cap, p.getPowers().get(0), control);
		double value = result.cost + lambda*valueFunction[result.index][1];
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
    
    public double[][] getValues(Prediction p, BatteryModel cap){
	final List<Double> powers = p.getPowers();
	//TODO
	int T = powers.size()+1; //how many Time States we have
	
	//look for null in case data overlaps new trip
	//there will be a null in the power set to signify a trip
	//break
	for(int t=0;t<powers.size();t++){
	    if(powers.get(t) == null){
		T = t+1;
		break;
	    }
	}
	
	//can decrease time resolution as t -> T
	
	
	final double[][] valueFunction = new double[X][T];
	//final int[][] controls = new int[X][T];
	final BatteryModel[] xstates = new BatteryModel[X];
	
	
	for(int x=0;x<X;x++){
	    xstates[x]=new SimpleCapacitor(cap.getMaxWattHours(),(double)x*cap.getMaxWattHours()/X,cap.getVoltage());
	    for(int t=0;t<T-1;t++){
		valueFunction[x][t] = Double.MAX_VALUE;
	    }
	    valueFunction[x][T-1] = 0;
	}
	
	//djikstra shortest path search
	//TODO change to A*?
	for(int t=T-2;t>=1;t--){
	    double power = 0;
	    try{
	    power = powers.get(t);
	    }catch(NullPointerException n){
		System.out.println(powers.size());
	    }
	    for(int x=0;x<X;x++){
		BatteryModel state = xstates[x];
		double minValue = Double.MAX_VALUE;
		for(int u=0;u<U.length;u++){
		    int control = U[u];
		    ControlResult result = testControl(state, power, control);
		    double value = result.cost + lambda*valueFunction[result.index][t+1];
		    if(value < minValue){
			minValue = value;
			//controls[x][t] = control;
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
    
    public ControlResult testControl(BatteryModel capacitorState, double powerDraw, double control){
	BatteryModel modelCap = capacitorState.createClone();
	
	double cost = 0.0;
	
	double wattsDemanded = powerDraw;
	int periodMS = 1000;
	double minCapPower = modelCap.getMinPowerDrawable(periodMS);
	double maxCapPower = modelCap.getMaxPowerDrawable(periodMS);	
	
	double capToMotorWatts = wattsDemanded > maxCapPower ? maxCapPower : wattsDemanded;
	capToMotorWatts = capToMotorWatts < minCapPower ? minCapPower : capToMotorWatts;
	double batteryToMotorWatts = wattsDemanded - capToMotorWatts;
	double batteryToCapWatts = control;// - batteryToMotorWatts;	
	batteryToCapWatts = batteryToCapWatts  < 0 ? 0 : batteryToCapWatts;	
	
	if (capToMotorWatts - batteryToCapWatts < minCapPower) {
		batteryToCapWatts = capToMotorWatts - minCapPower;
	    } else if(capToMotorWatts - batteryToCapWatts > maxCapPower){
		batteryToCapWatts = capToMotorWatts - maxCapPower;
	    }

	try {
	    modelCap.drawPower(capToMotorWatts - batteryToCapWatts, periodMS);
	   
	} catch (PowerFlowException e) {
	    System.out.print('x');
	}
    
	double percentCharge = modelCap.getWattHours() / modelCap.getMaxWattHours();
	int index = (int)(percentCharge*X);
	if(index == X) index = X-1;
	
	cost = (batteryToCapWatts + batteryToMotorWatts);
	cost = cost*cost;
	
	return new ControlResult(index,cost);
    }
    
    static class ControlResult {
	public final int index;
	public final double cost;
	public ControlResult(int i,double c){
	    index=i;
	    cost=c;
	}
    }

}
