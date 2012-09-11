package org.chargecar.algodev.controllers;

import java.util.List;

import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.battery.SimpleBattery;
import org.chargecar.prize.battery.SimpleCapacitor;
import org.chargecar.prize.util.PowerFlowException;


public class ReceedingConstant extends Controller {   
    final double[] controls = new double[]{0,8,64,512,1024,2048,4096,8192,16384,32768,65536};
    @Override
    public double getControl(List<Prediction> predictions,
	    BatteryModel battery, BatteryModel cap, int periodMS) {
	
	double bestControl = 0;
	double bestCost = Double.MAX_VALUE;
	
	for(double control : this.controls){
	    double cost = 0.0;
	    for(Prediction prediction : predictions){
		cost += prediction.getWeight()*testControl(control,prediction,battery,cap);
	    }
	    if(cost < bestCost){
		bestCost = cost;
		bestControl = control;
	    }
	}
	return bestControl;
    }
    
    private double testControl(double control, Prediction dutyPrediction, BatteryModel batt, BatteryModel cap){
	BatteryModel modelBatt = new SimpleBattery(batt.getMaxWattHours(),batt.getWattHours(),batt.getVoltage());
	BatteryModel modelCap = new SimpleCapacitor(cap.getMaxWattHours(), cap.getWattHours(), cap.getVoltage());
	for(int i=0;i<dutyPrediction.getPowers().size();i++){
	    Double powerDemand = dutyPrediction.getPowers().get(i);
	    if(powerDemand == null)
		break;
	    else
		processFlows(control, powerDemand, modelBatt, modelCap);
	}
    	
	    
	
	double cost = 0.0;
	double discount = 0.99;
	double gamma = 1;
	for(double current : modelBatt.getCurrentDrawHistory()){
	    cost += gamma*Math.pow(current, 2.0);
	    gamma = gamma*discount;
	}
	
	return cost;
    }

    private void processFlows(double control, double powerDemand, BatteryModel modelBatt, BatteryModel modelCap ){

	double wattsDemanded = powerDemand;
	int periodMS = 1000;
	double minCapPower = modelCap.getMinPowerDrawable(periodMS);
	double maxCapPower = modelCap.getMaxPowerDrawable(periodMS);	
	
	double capToMotorWatts = wattsDemanded > maxCapPower ? maxCapPower : wattsDemanded;
	capToMotorWatts = capToMotorWatts < minCapPower ? minCapPower : capToMotorWatts;
	double batteryToMotorWatts = wattsDemanded - capToMotorWatts;
	double batteryToCapWatts = control - batteryToMotorWatts;	
	batteryToCapWatts = batteryToCapWatts  < 0 ? 0 : batteryToCapWatts;	
	
	if (capToMotorWatts - batteryToCapWatts < minCapPower) {
		batteryToCapWatts = capToMotorWatts - minCapPower;
	    } else if(capToMotorWatts - batteryToCapWatts > maxCapPower){
		batteryToCapWatts = capToMotorWatts - maxCapPower;
	    }

	try {
	    modelCap.drawPower(capToMotorWatts - batteryToCapWatts, periodMS);
	    modelBatt.drawPower(batteryToMotorWatts + batteryToCapWatts, periodMS);
	} catch (PowerFlowException e) {
	}

    }
    
}
