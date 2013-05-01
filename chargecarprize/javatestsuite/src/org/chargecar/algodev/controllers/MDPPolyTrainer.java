package org.chargecar.algodev.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripFeatures;

public class MDPPolyTrainer implements Policy {
    private String currentDriver;
    private final String optFileFolderPath;
    private final double discountFactor = 0.99;
    private final int[] controlsSet = new int[]{-512,-1024,0,512,1024,1536,2048,2516,3072,3524,4096,5122,5500,6134,6600,7124,7600,8192,9122,10020,12000};
    private final MDPPolynomial mmdpOpt;
    private Map<Integer, double[][]> tripMap;
    private final String shortName = "dppolyt";
    private final BatteryModel cap;
    
    public MDPPolyTrainer(String optFileFolderPath, BatteryModel cap, int order, int stateCount){
	tripMap = new HashMap<Integer,double[][]>();
	this.optFileFolderPath = optFileFolderPath+"/";
	this.cap = cap.createClone();
	mmdpOpt = new MDPPolynomial(controlsSet, stateCount, order, discountFactor);
    }    

    @Override
    public String getShortName() {
	return this.shortName;
    }
    
    public void parseTrip(Trip t){
	updateTripMap(t);
	System.out.println(tripMap.size());
    }
    
    private void updateTripMap(Trip trip){
	String driver = trip.getFeatures().getDriver();
	if(trip.getPoints().size() > 3600) return;
	if(currentDriver == null || driver.compareTo(currentDriver) != 0){
	    if(currentDriver != null) finishTraining();
	    System.out.println("New driver: "+driver);	    
	    currentDriver = driver;
	    tripMap = new HashMap<Integer,double[][]>();
	}
	
	double[][] coeffs = mmdpOpt.getCoefficients(trip.getPoints(), this.cap);
	tripMap.put(trip.hashCode(), coeffs);
	
	
    }
    
    public void writeTable(){
	System.out.println("Writing table for "+currentDriver);
	
	FileOutputStream fos;
	try {
	    
	    File knnTableFile = new File(this.optFileFolderPath+currentDriver+".opt");
	    knnTableFile.getParentFile().mkdirs();
	    knnTableFile.createNewFile();
	    fos = new FileOutputStream(knnTableFile);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(tripMap);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
   
    public void finishTraining()
    {
	writeTable();
    }
    
    private String name = "Poly Trainer";
    
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
	    BatteryModel capacitorClone) {}
    
    @Override
    public PowerFlows calculatePowerFlows(PointFeatures pf) {
	return new PowerFlows(0,0,0);
    }
    
    @Override
    public void endTrip() {}
    
    @Override
    public String getName() {
	return name;
    }
    
    @Override
    public void loadState() {}
    
     @Override
    public void clearState() {
	// TODO Auto-generated method stub
	
    }
    
}
