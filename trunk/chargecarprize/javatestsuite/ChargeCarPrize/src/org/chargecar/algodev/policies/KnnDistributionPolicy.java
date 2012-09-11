package org.chargecar.algodev.policies;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.chargecar.algodev.controllers.ApproximateAnalytic;
import org.chargecar.algodev.controllers.Controller;
import org.chargecar.algodev.predictors.FullFeatureSet;
import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.algodev.predictors.Predictor;
import org.chargecar.algodev.predictors.knn.KnnMeanPredictor;
import org.chargecar.algodev.predictors.knn.KnnTable;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;

import org.chargecar.prize.util.TripFeatures;

public class KnnDistributionPolicy implements Policy {
    
    private Predictor knnPredictor;
    private Controller appController;
    
    private PointFeatures means;
    private PointFeatures sdevs;    
    private final int lookahead; 
    private final int neighbors;
    private int pointsTested = 0;
    
    private String currentDriver;
    private BatteryModel modelCap;
    private BatteryModel modelBatt;
    private final String name = "KNN Mean Prediction Policy";
    private final String shortName = "knnmean";
    private final File knnFileFolderPath;
    
    public KnnDistributionPolicy(String knnFileFolderPath, int neighbors, int lookahead){
	this.knnFileFolderPath = new File(knnFileFolderPath);
	this.neighbors = neighbors;
	this.lookahead = lookahead;
	this.appController = new ApproximateAnalytic();
    }
    
    @Override
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
	    BatteryModel capacitorClone) {
	String driver = tripFeatures.getDriver();
	modelCap = capacitorClone;
	modelBatt = batteryClone;
	
	if(currentDriver == null || driver.compareTo(currentDriver) != 0){
	    try {
		File currentKnnTableFile = new File(this.knnFileFolderPath,driver+".knn");
		System.out.println("New driver: "+driver);
		currentDriver = driver;
		FileInputStream fis = new FileInputStream(currentKnnTableFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		KnnTable knnTable = (KnnTable)ois.readObject();
		System.out.println("Table loaded. "+knnTable.getKnnPoints().size()+" points. Building trees... ");
		means = knnTable.getKnnPoints().get(0).getFeatures();
		sdevs = knnTable.getKnnPoints().get(1).getFeatures();
		knnTable.getKnnPoints().remove(1);
		knnTable.getKnnPoints().remove(0);
		knnPredictor = new KnnMeanPredictor(knnTable.getKnnPoints(), knnTable.getPowers(), new FullFeatureSet(),neighbors,lookahead);
		System.out.println("Trees built.");
	    } catch (Exception e) {		
		e.printStackTrace();
	    }
	}
	
    }
    
    @Override
    public PowerFlows calculatePowerFlows(PointFeatures pf) {
	pointsTested++;
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
	return appController.getControl(predictedDuty, modelBatt,modelCap,spf.getPeriodMS());	
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
    private PointFeatures scaleFeatures(PointFeatures pf){
	return new PointFeatures(
		//scale(pf.getLatitude(),means.getLatitude(),sdevs.getLatitude()),
		//scale(pf.getLongitude(),means.getLongitude(),sdevs.getLongitude()),
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
    public void endTrip() {
	// TODO Auto-generated method stub
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
    
}
