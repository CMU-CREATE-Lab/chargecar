package org.chargecar.algodev.policies;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.chargecar.algodev.controllers.Controller;
import org.chargecar.algodev.controllers.ReceedingConstant;
import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripFeatures;

public class OmniPowerFig implements Policy{
    private final List<Double> flows;
    private final List<Double> powers;
    private final int lookahead;
    private int index;
    private final Controller rhController;
    private BatteryModel modelCap;
    private BatteryModel modelBatt;
    
    public OmniPowerFig(int lookahead) {
	this.rhController = new ReceedingConstant();
	this.flows = new ArrayList<Double>();
	this.powers = new ArrayList<Double>();
	this.lookahead = lookahead;
    }    
   
    public double getFlow(){	
	List<Prediction> predictedDuty = new ArrayList<Prediction>();
	List<Double> powerPred;
	if(index+lookahead < powers.size())
	    powerPred = powers.subList(index, index+lookahead);
	else
	    powerPred = powers.subList(index, powers.size());
	
	Prediction actual = new Prediction(1.0, powerPred);
	predictedDuty.add(actual);
	double control = rhController.getControl(predictedDuty, modelBatt,modelCap,1000);
	flows.add(control);
	index++;
	return control;
    }

    @Override
    public void clearState() {
	
    }
    
    public void writeControls(){
	writeActual();
	FileWriter fstream;
	try {
	    fstream = new FileWriter("C:/controls.csv",false);
	    BufferedWriter out = new BufferedWriter(fstream);
	    for(Double p : flows){
		out.write(p+",");
	    }
	    out.write("0.0\n");		
	    out.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
	
    }

    public void writeActual(){
	FileWriter fstream;
	try {
	    fstream = new FileWriter("C:/actual.csv",false);
	    BufferedWriter out = new BufferedWriter(fstream);
	    boolean end = false;
	    for(Double d : powers){
		if(d==null) end=true;
		if(end)
		    out.write("0.0,");
		else
		    out.write(d+",");
	    }
	    
	    out.write("0.0\n");	   
	    out.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    @Override
    public String getName() {
	// TODO Auto-generated method stub
	return "Omnipower gen";
    }


    @Override
    public void loadState() {
	// TODO Auto-generated method stub
	
    }


    @Override
    public void endTrip() {
	// TODO Auto-generated method stub
	
    }


    public PowerFlows calculatePowerFlows(PointFeatures pf) {
	double idealFlow = getFlow();	
	double wattsDemanded = pf.getPowerDemand();
	int periodMS = pf.getPeriodMS();
	double minCapPower = modelCap.getMinPowerDrawable(periodMS);
	double maxCapPower = modelCap.getMaxPowerDrawable(periodMS);	
	
	double capToMotorWatts = wattsDemanded > maxCapPower ? maxCapPower : wattsDemanded;
	capToMotorWatts = capToMotorWatts < minCapPower ? minCapPower : capToMotorWatts;
	double batteryToMotorWatts = wattsDemanded - capToMotorWatts;
	double batteryToCapWatts = idealFlow - batteryToMotorWatts;	
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
	
	return new PowerFlows(batteryToMotorWatts, capToMotorWatts,
		batteryToCapWatts);
    }


    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
	    BatteryModel capacitorClone) {
	modelCap = capacitorClone;
	modelBatt = batteryClone;
	index = 0;
    }

    public void parseTrip(Trip t){
	List<PointFeatures> points = t.getPoints();
	powers.clear();
	flows.clear();
	
	for(PointFeatures pf : points){
	    powers.add(pf.getPowerDemand());
	} 
    }
    
    @Override
    public String getShortName() {
	return "opg";
    }
    
}
