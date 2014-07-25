package org.chargecar.algodev.controllers;

import java.util.List;

import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.battery.SimpleCapacitor;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;

//TODO TABULATE DYNAMICS!!!!!

public class MDPValueGraph {
    
    final int[] U;
    final int X; // how many charge states
    final double lambda;// discount factor
    final BatteryModel cap;
    
    public MDPValueGraph(int[] controls, int stateBuckets, double discountRate,
	    BatteryModel cap) {
	this.U = controls;
	this.X = stateBuckets;
	this.lambda = discountRate;
	this.cap = cap;
    }
    
    public double[][] getValues(List<PointFeatures> points) {
	int T = points.size() + 1; // how many Time States we have
	
	// look for null in case data overlaps new trip
	// there will be a null in the power set to signify a trip
	// break
	// for(int t=0;t<powers.size();t++){
	// if(powers.get(t) == null){
	// T = t+1;
	// break;
	// }
	// }
	
	// can decrease time resolution as t -> T
	
	final double[][] valueFunction = new double[X][T];
	final BatteryModel[] xstates = new BatteryModel[X];
	
	for (int x = 0; x < X; x++) {
	    xstates[x] = new SimpleCapacitor(cap.getMaxWattHours(), (double) x
		    * cap.getMaxWattHours() / X, cap.getVoltage());
	    for (int t = 0; t < T - 1; t++) {
		valueFunction[x][t] = Double.MAX_VALUE;
	    }
	    valueFunction[x][T - 1] = 0;
	}
	
	// djikstra shortest path search
	// TODO change to A*?
	for (int t = T - 2; t >= 0; t--) {
	    double power = 0;
	    power = points.get(t).getPowerDemand();
	    for (int x = 0; x < X; x++) {
		BatteryModel state = xstates[x];
		double minValue = Double.MAX_VALUE;
		for (int u = 0; u < U.length; u++) {
		    int control = U[u];
		    ControlResult result = testControl(state, power, control);
		    
		    double value = result.cost;
		    
		    double chargeState = result.pCharge * X;
		    int floor = (int) Math.floor(chargeState);
		    floor = Math.min(Math.max(floor, 0), X - 1);
		    int ceil = (int) Math.ceil(chargeState);
		    ceil = Math.max(Math.min(ceil, X - 1), 0);
		    
		    double fVal = valueFunction[floor][t + 1];
		    
		    if (floor == ceil) {
			value += lambda * fVal;
		    } else {
			double cVal = valueFunction[ceil][t + 1];
			value += lambda
				* (fVal + ((cVal - fVal)
					* (chargeState - floor) / (ceil - floor)));
		    }
		    
		    if (value < minValue) {
			minValue = value;
		    }
		}
		valueFunction[x][t] = minValue;
	    }
	}
	
	return valueFunction;
	/*
	 * double percentCharge = cap.getWattHours() / cap.getMaxWattHours();
	 * int index = (int)(percentCharge*X); if(index == X) index = X-1;
	 * 
	 * return controls[index][0];
	 */
    }
    
    public static ControlResult testControl(BatteryModel capacitorState,
	    double powerDraw, double control) {
	BatteryModel modelCap = capacitorState.createClone();
	
	double cost = 0.0;
	
	double wattsDemanded = powerDraw;
	int periodMS = 1000;
	double minCapPower = modelCap.getMinPowerDrawable(periodMS);
	double maxCapPower = modelCap.getMaxPowerDrawable(periodMS);
	
	double capToMotorWatts = wattsDemanded > maxCapPower ? maxCapPower
		: wattsDemanded;
	capToMotorWatts = capToMotorWatts < minCapPower ? minCapPower
		: capToMotorWatts;
	double batteryToMotorWatts = wattsDemanded - capToMotorWatts;
	double batteryToCapWatts = control;// - batteryToMotorWatts;
	batteryToCapWatts = batteryToCapWatts < 0 ? 0 : batteryToCapWatts;
	
	if (capToMotorWatts - batteryToCapWatts < minCapPower) {
	    batteryToCapWatts = capToMotorWatts - minCapPower;
	} else if (capToMotorWatts - batteryToCapWatts > maxCapPower) {
	    batteryToCapWatts = capToMotorWatts - maxCapPower;
	}
	
	try {
	    modelCap.drawPower(capToMotorWatts - batteryToCapWatts, periodMS);
	    
	} catch (PowerFlowException e) {
	    System.out.print('x');
	}
	
	double percentCharge = modelCap.getWattHours()
		/ modelCap.getMaxWattHours();
	
	cost = (batteryToCapWatts + batteryToMotorWatts);
	cost = cost * cost;
	
	return new ControlResult(percentCharge, cost);
    }
    
    static class ControlResult {
	public final double pCharge;
	public final double cost;
	
	public ControlResult(double pc, double c) {
	    pCharge = pc;
	    cost = c;
	}
    }
    
}
