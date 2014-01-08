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
import org.chargecar.algodev.controllers.DPPolyController;
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

public class KnnDistPolyPolicy implements Policy {
    
    protected Predictor knnPredictor;
    protected Controller controller;
    private final int[] controlsSet = new int[]{-512,-1024,0,512,1024,1536,2048,2516,3072,3524,4096,5122,5500,6134,6600,7124,7600,8192,9122,10020,12000};
        
    private PointFeatures means;
    private PointFeatures sdevs;    
    private final int neighbors;
    
    private String currentDriver;
    protected BatteryModel modelCap;
    protected BatteryModel modelBatt;
    private final String name = "KNN Distribution Polynomial Policy";
    private final String shortName = "knndist";
    private final File knnFileFolderPath;
    private final File optFileFolderPath;
    
    public KnnDistPolyPolicy(String knnFileFolderPath, String optFileFolderPath, int neighbors){
	this.knnFileFolderPath = new File(knnFileFolderPath);
	this.optFileFolderPath = new File(optFileFolderPath);
	this.neighbors = neighbors;
    }
    
    @Override
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
	    BatteryModel capacitorClone) {
	String driver = tripFeatures.getDriver();
	modelCap = capacitorClone;
	modelBatt = batteryClone;
	
	if(currentDriver == null || driver.compareTo(currentDriver) != 0){
	    try {
		this.controller = null;
		this.knnPredictor = null;
		//load knn tree
		File currentFile = new File(this.knnFileFolderPath,driver+".knn");
		System.out.println("New driver: "+driver);
		currentDriver = driver;
		
		FileInputStream fis = new FileInputStream(currentFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		List<KnnPoint> knnList = (ArrayList<KnnPoint>)ois.readObject();
		System.out.println("Table loaded. "+knnList.size()+" points. Building trees... ");
		means = knnList.get(0).getFeatures();
		sdevs = knnList.get(1).getFeatures();
		knnList.remove(1);
		knnList.remove(0);
		knnPredictor = new KnnDistPredictor(knnList, new FullFeatureSet(),neighbors, true);
		System.out.println("Trees built.");
		ois.close();
		fis.close();
		
		
		//load controller map		
		currentFile = new File(this.optFileFolderPath,driver+".opt");
		currentDriver = driver;
		this.controller = null;
		fis = new FileInputStream(currentFile);
		ois = new ObjectInputStream(fis);
		Map<Integer, double[][]> optMap = (Map<Integer, double[][]>)ois.readObject();
		System.out.println("Polynomial Coefficients loaded. "+optMap.size()+" trips.");
		controller = new DPPolyController(controlsSet, optMap); 
		System.out.println("Controller loaded.");		
	    } catch (Exception e) {		
		e.printStackTrace();
	    }
	}
	
    }
    
    @Override
    public PowerFlows calculatePowerFlows(PointFeatures pf) {
	double idealFlow = getFlow(pf);	
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
    
    public double getFlow(PointFeatures pf){
	PointFeatures spf = scaleFeatures(pf);
	List<Prediction> predictedDuty = knnPredictor.predictDuty(spf);
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
    protected PointFeatures scaleFeatures(PointFeatures pf){
	return new PointFeatures(		
		pf.getLatitude(),
		pf.getLongitude(),
		scale(pf.getElevation(),means.getElevation(),sdevs.getElevation()),
		scale(pf.getBearing(),means.getBearing(),sdevs.getBearing()),
		pf.getPlanarDist(),
		scale(pf.getAcceleration(),means.getAcceleration(),sdevs.getAcceleration()),
		scale(pf.getSpeed(),means.getSpeed(), sdevs.getSpeed()),
		scale(pf.getPowerDemand(),means.getPowerDemand(),sdevs.getPowerDemand()),
		scale(pf.getTotalPowerUsed(), means.getTotalPowerUsed(), sdevs.getTotalPowerUsed()),
		pf.getPeriodMS(), pf.getTime());
	
    }
    
    private double scale(double feature, double mean, double sdev){
	return (feature-mean)/sdev;
    }
    
    @Override
    public String getName() {
	return this.name;
    }
    
    @Override
    public void loadState() {
	// TODO Auto-generated method stub
	
    }

    @Override
    public String getShortName() {
	return this.shortName;
    }

    @Override
    public void clearState() {
	this.currentDriver = null;
	this.knnPredictor = null;
	this.controller = null;
	this.means = null;
	this.sdevs = null;
    }

    @Override
    public void endTrip(Trip t) {
	// TODO Auto-generated method stub
	
    }
    
}
