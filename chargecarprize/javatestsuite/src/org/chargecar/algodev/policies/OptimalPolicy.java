package org.chargecar.algodev.policies;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.chargecar.algodev.controllers.Controller;
import org.chargecar.algodev.controllers.DPOptController;
import org.chargecar.algodev.knn.FullFeatureSet;
import org.chargecar.algodev.knn.KnnPoint;
import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.algodev.predictors.Predictor;
import org.chargecar.algodev.predictors.knn.KnnDistPredictor;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.Trip;

import org.chargecar.prize.util.TripFeatures;

public class OptimalPolicy {
    
    protected Controller controller;
    private final int[] controlsSet = new int[]{-512,-1024,0,512,1024,1536,2048,2516,3072,3524,4096,5122,5500,6134,6600,7124,7600,8192,9122,10020,12000};
        
    private String currentDriver;
    protected BatteryModel modelCap;
    protected BatteryModel modelBatt;
    private final String name = "Optimal Policy";
    private final String shortName = "optpoly";

    private File optFileFolderPath;
    private int tripID;
    
    public OptimalPolicy(String optFileFolderPath){
	this.optFileFolderPath = new File(optFileFolderPath);

    }
    
    public void setOptPath(String optPath){
	this.optFileFolderPath = new File(optPath);
	this.optFileFolderPath.mkdirs();
	this.currentDriver = null;
    }
    
    public void parseTrip(Trip t){
	this.tripID = t.hashCode();	
    }
    
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
	    BatteryModel capacitorClone) {
	String driver = tripFeatures.getDriver();
	modelCap = capacitorClone;
	modelBatt = batteryClone;
	//TODO FIX BACK
	if(currentDriver == null || driver.compareTo(currentDriver) != 0){
	    try {
		this.controller = null;
		
		System.out.println("New driver: "+driver);
		currentDriver = driver;
		//load controller map		
		File currentFile = new File(this.optFileFolderPath,driver+".opt");
		currentDriver = driver;
		this.controller = null;
		FileInputStream fis = new FileInputStream(currentFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		Map<Integer, double[][]> optMap = (Map<Integer, double[][]>)ois.readObject();
		System.out.println("Graph loaded. "+optMap.size()+" trips.");
		controller = new DPOptController(controlsSet, optMap, null); 
		System.out.println("Controller loaded.");		
	    } catch (Exception e) {		
		e.printStackTrace();
	    }
	}
	
    }
    
    
    public PowerFlows calculatePowerFlows(PointFeatures pf, int i) {
	double idealFlow = getFlow(pf, i);	
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
    
    public double getFlow(PointFeatures pf, int i){
	
	KnnPoint kp = new KnnPoint(pf, tripID, i);
	
	Prediction cheater = new Prediction(1, tripID, i, null);
	
	List<Prediction> predictedDuty = new ArrayList<Prediction>();
	
	predictedDuty.add(cheater);
	
	//System.out.println(predictedDuty.size());
	return controller.getControl(predictedDuty, modelBatt,modelCap, pf.getPeriodMS(), pf.getPowerDemand());	
    }
    
    public void writePowers(List<Double> powers) {
	FileWriter fstream;
	try {
	    fstream = new FileWriter("C:/powersout.csv",true);
	    BufferedWriter out = new BufferedWriter(fstream);
	    for(Double p : powers){
		out.write(p+",");
	    }
	    out.write("0.0\n");		
	    out.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    public void endTrip(Trip t) {
	// TODO Auto-generated method stub
    }    
    

    public String getName() {
	return this.name;
    }
    
    
    public void loadState() {
	// TODO Auto-generated method stub
	
    }

    
    public String getShortName() {
	return this.shortName;
    }

    
    public void clearState() {
	this.currentDriver = null;
	this.controller = null;
    }
    
}
