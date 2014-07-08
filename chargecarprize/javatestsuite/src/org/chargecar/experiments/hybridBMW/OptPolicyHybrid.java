package org.chargecar.experiments.hybridBMW;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
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

public class OptPolicyHybrid {
    
    protected Controller controller;
    private final int[] controlsSet = new int[]{0,5,10,15,20,25,30,35,40,45,50};
    private final double[] controlsCost = new double[]{0,1,2,3,4,5,6,7,8,9,10};
    private final Map<Integer,Double> costFunction = new HashMap<Integer,Double>(11);
    private String currentDriver;

    protected BatteryModel modelBatt;
    private final String name = "Optimal Policy";
    private final String shortName = "optpoly";

    private File optFileFolderPath;
    private int tripID;
    
    public OptPolicyHybrid(String optFileFolderPath){
	this.optFileFolderPath = new File(optFileFolderPath);
	for(int i=0;i<controlsSet.length;i++){
	    costFunction.put(controlsSet[i], controlsCost[i]);
	}

    }
    
    public void setOptPath(String optPath){
	this.optFileFolderPath = new File(optPath);
	this.optFileFolderPath.mkdirs();
	this.currentDriver = null;
    }
    
    public void parseTrip(Trip t){
	this.tripID = t.hashCode();	
    }
    
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone) {
	String driver = tripFeatures.getDriver();
	modelBatt = batteryClone;
	
	if(currentDriver == null || driver.compareTo(currentDriver) != 0){
	    ObjectInputStream ois;
	    try {
		this.controller = null;
		
		System.out.println("New driver: "+driver);
		currentDriver = driver;
		//load controller map		
		File currentFile = new File(this.optFileFolderPath,driver+".opt");
		currentDriver = driver;
		this.controller = null;
		FileInputStream fis = new FileInputStream(currentFile);
		ois = new ObjectInputStream(fis);
		@SuppressWarnings("unchecked")
		Map<Integer, double[][]> optMap = (Map<Integer, double[][]>)ois.readObject();
		ois.close();
		System.out.println("Graph loaded. "+optMap.size()+" trips.");
		controller = new DPOptControllerHybrid(controlsSet, costFunction, optMap, null); 
		System.out.println("Controller loaded.");		
	    } catch (Exception e) {		
		e.printStackTrace();
	    }
	}
	
    }
    
    
    public PowerFlows calculatePowerFlows(PointFeatures pf, int i) {
	double idealEngineWatts = getEnginePower(pf, i);	
	double wattsDemanded = pf.getPowerDemand();
	
	int periodMS = pf.getPeriodMS();
	
	double minBattPower = modelBatt.getMinPowerDrawable(periodMS);
	double maxBattPower = modelBatt.getMaxPowerDrawable(periodMS);	
	
	double motorWatts = wattsDemanded-idealEngineWatts;
	
	motorWatts = motorWatts > maxBattPower ? maxBattPower : motorWatts;
	motorWatts = motorWatts < minBattPower ? minBattPower : motorWatts;
	
	double engineWatts = wattsDemanded - motorWatts;
	
	try {
	    modelBatt.drawPower(motorWatts, periodMS);
	} catch (PowerFlowException e) {
	    System.err.println("Battery Capacity violated in policy model");
	}
	
	return new PowerFlows(engineWatts, motorWatts, 0);
    }
    
    public double getEnginePower(PointFeatures pf, int i){
	
	KnnPoint kp = new KnnPoint(pf, tripID, i);
	
	Prediction cheater = new Prediction(1, tripID, i, null);
	
	List<Prediction> predictedDuty = new ArrayList<Prediction>();
	
	predictedDuty.add(cheater);
	
	//System.out.println(predictedDuty.size());
	return controller.getControl(predictedDuty, modelBatt, null, pf.getPeriodMS(), pf.getPowerDemand());	
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

