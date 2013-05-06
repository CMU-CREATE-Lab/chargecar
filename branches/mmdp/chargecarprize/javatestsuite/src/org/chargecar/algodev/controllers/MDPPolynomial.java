package org.chargecar.algodev.controllers;

import java.util.List;

import org.chargecar.algodev.controllers.MDPValueGraph.ControlResult;
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
        
    public MDPPolynomial(int[] controls, int stateBuckets, int order, double discountRate){
	this.U = controls;
	this.X = stateBuckets;
	this.O = order+1; //2nd order = [0,1,2]
	this.lambda = discountRate;

    }
    
    public double[][] getCoefficients(List<PointFeatures> points, BatteryModel cap){
	int T = points.size()+1; //how many Time States we have
		
	final double[][] valueFunction = new double[T][X];
	
	final double[][] coefficients = new double[T][O];
	
	final BatteryModel[] xstates = new BatteryModel[X];
	
	final double states[][] = new double[X][O];
	
	for(int x=0;x<X;x++){
	    xstates[x]=new SimpleCapacitor(cap.getMaxWattHours(),(double)x*cap.getMaxWattHours()/X,cap.getVoltage());
	    double pCharge = (double) x / (double) X;
	
	    for(int o=0;o<O;o++){
		states[x][o] = Math.pow(pCharge,o);
	    }
	    
	    for(int t=0;t<T-1;t++){
		valueFunction[t][x] = Double.MAX_VALUE;
	    }
		
	    valueFunction[T-1][x] = 0;	    
	}
	
	//djikstra shortest path search
	for(int t=T-2;t>=1;t--){
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

                    double fVal = valueFunction[t+1][floor];

                    if(floor==ceil){
                        value += lambda*fVal;
                    }
                    else{
                        double cVal = valueFunction[t+1][ceil];
                        value += lambda*(fVal + ((cVal - fVal)*(chargeState - floor)/(ceil-floor) ));
                    }		    
		    
		    if(value < minValue){
			minValue = value;
		    }
		}
		valueFunction[t][x] = minValue;
	    }
	}
	
	Matrix stateMatrix = new Matrix(states);
	stateMatrix = stateMatrix.transpose().times(stateMatrix).inverse().times(stateMatrix.transpose());


	//djikstra shortest path search
	//TODO change to A*?
	for(int t=0;t<T;t++){
	    double[] values = valueFunction[t]; //values for each sampled state at this timestep
	    coefficients[t] = regression(stateMatrix, values);
	}
	
	return coefficients;
    }
    
    private double[] regression(Matrix x2, double[] values) {
	Matrix coeff = x2.times(new Matrix(values, values.length));
	return coeff.getColumnPackedCopy();
    }

    public static double calculateValue(double[] coeffs, double pCharge) {
	double value = 0;
	for(int o=0;o<coeffs.length;o++){
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
