package org.chargecar.algodev.controllers;

import java.util.List;

import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.battery.SimpleCapacitor;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import Jama.Matrix;

//TODO TABULATE DYNAMICS!!!!!

public class MDPPolynomial{
    
    final int[] U;
    final int X; //how many charge states 
    final int O; 
    final double lambda;//discount factor
    Matrix stateMatrix; 
    
    public MDPPolynomial(int[] controls, int stateBuckets, int order, double discountRate){
	this.U = controls;
	this.X = stateBuckets;
	this.O = order+1; //2nd order = [0,1,2]
	this.lambda = discountRate;

    }
    
    public double[][] getCoefficients(List<PointFeatures> points, BatteryModel cap){
	int T = points.size()+1; //how many Time States we have
	
	final double[][] coefficients = new double[T][O];
	
	final BatteryModel[] xstates = new BatteryModel[X];
	
	final double states[][] = new double[X][O];
	
	for(int x=0;x<X;x++){
	    xstates[x]=new SimpleCapacitor(cap.getMaxWattHours(),(double)x*cap.getMaxWattHours()/X,cap.getVoltage());
	    for(int o=0;o<O-1;o++){
		states[x][o] = Math.pow((double)x / X,o);
	    }
	    
	}
	
	stateMatrix = new Matrix(states);
	stateMatrix = stateMatrix.transpose().times(stateMatrix).inverse().times(stateMatrix.transpose());

	for(int o=0;o<O;o++){
	    coefficients[T-1][o] = 0; //0 value at final timestep
	}
	
	//djikstra shortest path search
	//TODO change to A*?
	for(int t=T-2;t>=1;t--){
	    double power = 0;
	    
	    double[] values = new double[X]; //values for each sampled state at this timestep
	    
	    power = points.get(t).getPowerDemand();
	    for(int x=0;x<X;x++){
		BatteryModel state = xstates[x];
		double minValue = Double.MAX_VALUE;
		for(int u=0;u<U.length;u++){
		    int control = U[u];
		    ControlResult result = testControl(state, power, control);
		    
		    double value = result.cost + calculateValue(coefficients[t+1],result.pCharge);                    
		    
		    if(value < minValue){
			minValue = value;
		    }
		}
		values[x] = minValue;
	    }
	    
	    coefficients[t] = regression(values);
	}
	
	return coefficients;
	/*
	double percentCharge = cap.getWattHours() / cap.getMaxWattHours();
	int index = (int)(percentCharge*X);
	if(index == X) index = X-1;
	
	return controls[index][0];
*/
	}
    
    private double[] regression(double[] values) {
	Matrix coeff = stateMatrix.times(new Matrix(values, values.length));
	return coeff.getColumnPackedCopy(); 
    }

    private double calculateValue(double[] coeffs, double pCharge) {
	double value = 0;
	for(int o=0;o<O;o++){
	    value += coeffs[o]*Math.pow(pCharge, o);
	}
	return value;
    }

    public static ControlResult testControl(BatteryModel capacitorState, double powerDraw, double control){
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

	cost = (batteryToCapWatts + batteryToMotorWatts);
	cost = cost*cost;
	
	return new ControlResult(percentCharge,cost);
    }
    
    static class ControlResult {
	public final double pCharge;
	public final double cost;
	public ControlResult(double pc,double c){
	    pCharge = pc;
	    cost=c;
	}
    }

}
