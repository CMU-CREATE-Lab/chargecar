package org.chargecar.experiments.hybridBMW;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripFeatures;

public class OptPolicyHybrid {
    
    protected DPOptControllerHybrid controller;
    private final int[] controlsSet;// = new
				    // int[]{0,5000,10000,15000,20000,25000,30000,35000,40000,45000,50000};
    private String currentDriver;
    
    protected BatteryModel modelBatt;
    private String name = "Optimal Policy";
    private String shortName = "optpoly";
    
    private File optFileFolderPath;
    private int tripID;
    
    private int lastControl;
    
    public OptPolicyHybrid(String optFileFolderPath, int[] controls) {
	this.optFileFolderPath = new File(optFileFolderPath);
	this.controlsSet = controls;
	this.lastControl = 0;
    }
    
    public void setShortName(String sn) {
	this.shortName = sn;
    }
    
    public void setOptPath(String optPath) {
	this.optFileFolderPath = new File(optPath);
	this.optFileFolderPath.mkdirs();
	this.currentDriver = null;
    }
    
    public void parseTrip(Trip t) {
	this.tripID = t.hashCode();
    }
    
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone) {
	String driver = tripFeatures.getDriver();
	modelBatt = batteryClone;
	
	if (currentDriver == null || driver.compareTo(currentDriver) != 0) {
	    ObjectInputStream ois;
	    try {
		this.controller = null;
		
		System.out.println("New driver: " + driver);
		currentDriver = driver;
		// load controller map
		File currentFile = new File(this.optFileFolderPath, driver
			+ ".opt");
		currentDriver = driver;
		this.controller = null;
		FileInputStream fis = new FileInputStream(currentFile);
		ois = new ObjectInputStream(fis);
		@SuppressWarnings("unchecked")
		Map<Integer, double[][]> optMap = (Map<Integer, double[][]>) ois
			.readObject();
		ois.close();
		System.out
			.println("Graph loaded. " + optMap.size() + " trips.");
		controller = new DPOptControllerHybrid(controlsSet, optMap,
			null);
		System.out.println("Controller loaded.");
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	
    }
    
    public PowerControls calculatePowerFlows(PointFeatures pf, int i) {
	int idealEngineWatts = getEnginePower(pf, i);
	int wattsDemanded = (int) pf.getPowerDemand();
	
	int periodMS = pf.getPeriodMS();
	
	int minBattPower = (int) modelBatt.getMinPowerDrawable(periodMS);
	int maxBattPower = (int) modelBatt.getMaxPowerDrawable(periodMS);
	
	int motorWatts = wattsDemanded - idealEngineWatts;
	
	motorWatts = motorWatts > maxBattPower ? maxBattPower : motorWatts;
	motorWatts = motorWatts < minBattPower ? minBattPower : motorWatts;
	
	int engineWatts = wattsDemanded - motorWatts;
	
	try {
	    modelBatt.drawPower(motorWatts, periodMS);
	} catch (PowerFlowException e) {
	    System.err.println("Battery Capacity violated in policy model");
	}
	
	this.lastControl = engineWatts;
	return new PowerControls(engineWatts, motorWatts);
    }
    
    public int getEnginePower(PointFeatures pf, int i) {
	
	Prediction cheater = new Prediction(1, tripID, i, null);
	
	List<Prediction> predictedDuty = new ArrayList<Prediction>();
	
	predictedDuty.add(cheater);
	
	return controller.getControl(predictedDuty, modelBatt, null,
		pf.getPeriodMS(), pf.getPowerDemand(), this.lastControl);
    }
    
    public void writePowers(List<Double> powers) {
	FileWriter fstream;
	try {
	    fstream = new FileWriter("C:/powersout.csv", true);
	    BufferedWriter out = new BufferedWriter(fstream);
	    for (Double p : powers) {
		out.write(p + ",");
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
