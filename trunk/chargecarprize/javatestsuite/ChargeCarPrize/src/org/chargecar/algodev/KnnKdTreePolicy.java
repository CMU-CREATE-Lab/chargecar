package org.chargecar.algodev;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.TripBuilder;
import org.chargecar.prize.util.TripFeatures;

public class KnnKdTreePolicy implements Policy {
    
    
    private KdTree kdTree;
    private List<Double> speedHist;
    private List<Double> accelHist;
    private double speedVar;
    private double accelVar;
    private int histIndex = 0;
    final int histLength = 5;
    
    private String currentDriver;
    private BatteryModel modelCap;
    private BatteryModel modelBatt;
    private String name = "KNN ExtendedFeatures Policy";
    
    @Override
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
	    BatteryModel capacitorClone) {
	String driver = tripFeatures.getDriver();
	modelCap = capacitorClone;
	modelBatt = batteryClone;
	histIndex = 0;
	speedHist = new ArrayList<Double>();
	accelHist = new ArrayList<Double>();
	for(int i = 0;i<histLength;i++){
	    speedHist.add(0.0);
	    accelHist.add(0.0);
	}
	speedVar = 0;
	accelVar = 0;
	if(currentDriver == null || driver.compareTo(currentDriver) != 0){
	    try {
		File currentKnnTableFile = new File("C:/Users/astyler/Desktop/My Dropbox/work/ccpbak/knn/"+driver+".knn");
		System.out.println("New driver: "+driver);
		currentDriver = driver;
		FileInputStream fis = new FileInputStream(currentKnnTableFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		List<KnnPoint> knnTable = (ArrayList<KnnPoint>)ois.readObject();
		System.out.println("Table loaded.  Building tree... ");
		kdTree = new KdTree(knnTable);
		System.out.println("Tree loaded");
	    } catch (Exception e) {		
		e.printStackTrace();
	    }
	}
	
    }
    
    @Override
    public PowerFlows calculatePowerFlows(PointFeatures pf) {
	speedHist.set(histIndex, pf.getSpeed());
	accelHist.set(histIndex, pf.getAcceleration());
	speedVar = calculateVariance(speedHist);
	accelVar = calculateVariance(accelHist);
	histIndex = (histIndex +1)%histLength;
	double predictedFlow = getFlow(new ExtendedPointFeatures(pf,speedVar,accelVar));
	
	double wattsDemanded = pf.getPowerDemand();
	int periodMS = pf.getPeriodMS();
	double minCapCurrent = modelCap.getMinPower(periodMS);
	double maxCapCurrent = modelCap.getMaxPower(periodMS);
	double capToMotorWatts = 0.0;
	double batteryToCapWatts = 0.0;
	double batteryToMotorWatts = 0.0;
	if (wattsDemanded < minCapCurrent) {
	    // drawing more than the cap has
	    // battery is already getting drawn, don't trickle cap
	    capToMotorWatts = minCapCurrent;
	    batteryToMotorWatts = wattsDemanded - capToMotorWatts;
	    batteryToCapWatts = predictedFlow - batteryToMotorWatts;
	} else if (wattsDemanded > maxCapCurrent) {
	    // overflowing cap with regen power
	    // cap is full, no need to trickle.
	    capToMotorWatts = maxCapCurrent;
	    batteryToMotorWatts = wattsDemanded - capToMotorWatts;
	    batteryToCapWatts = predictedFlow - batteryToMotorWatts;
	} else {
	    // capacitor can handle the demand
	    capToMotorWatts = wattsDemanded;
	    batteryToMotorWatts = 0;
	    batteryToCapWatts = predictedFlow;
	    
	}
	batteryToCapWatts = 0 < batteryToCapWatts ? 0 : batteryToCapWatts;
	
	if (capToMotorWatts - batteryToCapWatts > maxCapCurrent) {
	    batteryToCapWatts = capToMotorWatts - maxCapCurrent;
	} else if(capToMotorWatts - batteryToCapWatts < minCapCurrent){
	    batteryToCapWatts = capToMotorWatts - minCapCurrent;
	}
	
	try {
	    modelCap.drawPower(capToMotorWatts - batteryToCapWatts, pf);
	    modelBatt.drawPower(batteryToMotorWatts + batteryToCapWatts, pf);
	} catch (PowerFlowException e) {
	}
	
	return new PowerFlows(batteryToMotorWatts, capToMotorWatts,
		batteryToCapWatts);
    }
    
    private double calculateVariance(List<Double> list){
	 double sum = 0.0;
	 for(int i = 0;i<histLength;i++)
	     sum += list.get(i);
	 
	 double mean = sum / histLength;
	 
	 sum = 0.0;
	 
	 for(int i = 0;i<histLength;i++){
	     double diff = (list.get(i) - mean); 
	     sum += diff*diff;
	 }
	 
	 return sum/(histLength-1);	
    }
    
    public double getFlow(ExtendedPointFeatures pf){
	return kdTree.getBestEstimate(pf, 32);
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
    
}
