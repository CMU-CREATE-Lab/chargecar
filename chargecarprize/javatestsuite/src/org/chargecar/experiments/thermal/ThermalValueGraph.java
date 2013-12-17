package org.chargecar.experiments.thermal;

import java.util.ArrayList;
import java.util.List;

import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.battery.SimpleCapacitor;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;

//TODO TABULATE DYNAMICS!!!!!

public class ThermalValueGraph
{    
    final double[] U;
    final double[] temps;
    final double lambda;//discount factor
    final ThermalBattery batt;  
    final int MULTIPLIER;
    
    public ThermalValueGraph(double[] temps, double[] massFlows, double discountRate, ThermalBattery batt, int multi){
	this.U = massFlows;
	this.temps = temps;
	this.lambda = discountRate;
	this.batt = batt;
	this.MULTIPLIER = multi;
	
	
    }
    
    public double[][] getValues(List<Double> powers){
	int T = powers.size()+1; //how many Time States we have
	int X = temps.length;
	//look for null in case data overlaps new trip
	//there will be a null in the power set to signify a trip
	//break
//	for(int t=0;t<powers.size();t++){
//	    if(powers.get(t) == null){
//		T = t+1;
//		break;
//	    }
//	}
	
	//can decrease time resolution as t -> T
	
	
	final double[][] valueFunction = new double[X][T];
	final ThermalBattery[] xstates = new ThermalBattery[X];
	
	
	for(int x=0;x<X;x++){
	    xstates[x]=batt.createClone();
	    xstates[x].temp = temps[x];
	    for(int t=0;t<T-1;t++){
		valueFunction[x][t] = Double.MAX_VALUE;
	    }
	    valueFunction[x][T-1] = 0;
	}
	
	//djikstra shortest path search
	//TODO change to A*?
	for(int t=T-2;t>=0;t--){
	    double power = 0;
	    power = powers.get(t);
	    for(int x=0;x<X;x++){
		ThermalBattery state = xstates[x];
		double minValue = Double.MAX_VALUE;
		for(int u=0;u<U.length;u++){
		    double massFlow = U[u];
		    ControlResult result = testControl(state, power, massFlow);
		    
		    double value = result.cost;
		    
		    int floor = temps.length-1;
		    int ceil = floor;			
	    	    for(int i=0;i < temps.length; i++){
	    		if(result.temp < temps[i]){
	    			floor = i-1;
	    			ceil = i;
	    			break;
	    		    }
	    		}
	    		if(floor < 0){
	    		    ceil = floor = 0;
	    		}	
	    		
	    		//System.out.println("Resulting temp: "+result.temp+" between ("+temps[floor]+", "+temps[ceil]+").");
                    
                    double fVal = valueFunction[floor][t+1];

                    if(floor==ceil){
                        value += lambda*fVal;
                    }
                    else{
                        double cVal = valueFunction[ceil][t+1];
                        value += lambda*(fVal + ((cVal - fVal)*(result.temp - temps[floor])/(temps[ceil]-temps[floor]) ));
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
    
    public double[][] getValuesP(List<PointFeatures> points){
	List<Double> powers = new ArrayList<Double>(points.size());
	for(PointFeatures pf : points)
	    powers.add(this.MULTIPLIER*pf.getPowerDemand());
	return getValues(powers);    
	}
    
    public static ControlResult testControl(ThermalBattery batteryState, double powerDraw, double control){
	ThermalBattery batt = batteryState.createClone();
	batt.drawPower(powerDraw, control);
	
	double cost = 0.0;
	
	//cost = 10000000*Math.pow(control,3); 
	cost = 100*control;//penalize control linearly
	
	//penalize excess of 35C
	double temp = batt.temp;
	/*if(temp > 40){
	    cost = cost + 400;
	}
	else*/ if(temp > 35){
	    cost = cost + Math.pow((temp - 35),2);
	}	
	
	return new ControlResult(batt.temp,cost);
    }
    
    static class ControlResult {
	public final double temp;
	public final double cost;
	public ControlResult(double t,double c){
	    temp = t;
	    cost=c;
	}
    }

}
