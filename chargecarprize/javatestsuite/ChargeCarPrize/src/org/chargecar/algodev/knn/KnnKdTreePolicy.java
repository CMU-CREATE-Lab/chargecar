package org.chargecar.algodev.knn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.chargecar.algodev.ExtendedPointFeatures;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.TripBuilder;
import org.chargecar.prize.util.TripFeatures;

public class KnnKdTreePolicy implements Policy {
    
    private KdTree gpsKdTree;
    //private KdTree featKdTree;
    private ExtendedPointFeatures means;
    private ExtendedPointFeatures sdevs;
    
    private final List<Double> speedHist = new ArrayList<Double>();
    private final List<Double> accelHist = new ArrayList<Double>();
    private int histIndex = 0;
    private final int histLength = 20;
    
    private String currentDriver;
    private BatteryModel modelCap;
    private BatteryModel modelBatt;
    private final String name = "KNN ExtendedFeatures Policy";
    
    @Override
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
	    BatteryModel capacitorClone) {
	String driver = tripFeatures.getDriver();
	modelCap = capacitorClone;
	modelBatt = batteryClone;
	histIndex = 0;
	speedHist.clear();
	accelHist.clear();
	
	if(currentDriver == null || driver.compareTo(currentDriver) != 0){
	    try {
		File currentKnnTableFile = new File("C:/Users/astyler/Desktop/My Dropbox/work/ccpbak/knn/"+driver+".knn");
		System.out.println("New driver: "+driver);
		currentDriver = driver;
		FileInputStream fis = new FileInputStream(currentKnnTableFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		List<KnnPoint> knnTable = (ArrayList<KnnPoint>)ois.readObject();
		System.out.println("Table loaded.  Building trees... ");
		means = knnTable.get(0).getFeatures();
		sdevs = knnTable.get(1).getFeatures();
		knnTable.remove(1);
		knnTable.remove(0);
		gpsKdTree = new KdTree(knnTable, new GPSFeatureSet());
		//featKdTree = new KdTree(knnTable, new FullFeatureSet());
		System.out.println("Trees built.");
	    } catch (Exception e) {		
		e.printStackTrace();
	    }
	}
	
    }
    
    @Override
    public PowerFlows calculatePowerFlows(PointFeatures pf) {
	/*if(speedHist.size() > histIndex){
	    speedHist.set(histIndex, pf.getSpeed());
	    accelHist.set(histIndex, pf.getAcceleration());
	}
	else
	{
	    speedHist.add(pf.getSpeed());
	    accelHist.add(pf.getAcceleration());
	}
	double speedVar = calculateVariance(speedHist);
	double accelVar = calculateVariance(accelHist);
	histIndex = (histIndex +1)%histLength;
	double predictedFlow = getFlow(new ExtendedPointFeatures(pf,speedVar,accelVar));
	*/
	double predictedFlow = getFlow(new ExtendedPointFeatures(pf,0,0));
	
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
	for(int i = 0;i<list.size();i++)
	    sum += list.get(i);
	
	double mean = sum / list.size();
	
	sum = 0.0;
	
	for(int i = 0;i<list.size();i++){
	    double diff = (list.get(i) - mean); 
	    sum += diff*diff;
	}
	
	return sum/(list.size());	
    }
    
    public double getFlow(ExtendedPointFeatures epf){
	//epf = scaleFeatures(epf);
	//return featKdTree.getBestEstimate(epf, 5);
	return gpsKdTree.getBestEstimate(epf, 20);
    }
    
    private ExtendedPointFeatures scaleFeatures(ExtendedPointFeatures epf){
	return new ExtendedPointFeatures(
		epf.getLatitude(),epf.getLongitude(),
		epf.getElevation(),epf.getBearing(),epf.getPlanarDist(),
		scale(epf.getAcceleration(),means.getAcceleration(),sdevs.getAcceleration()),
		scale(epf.getSpeed(),means.getSpeed(), sdevs.getSpeed()),
		scale(epf.getPowerDemand(),means.getPowerDemand(),sdevs.getPowerDemand()),
		scale(epf.getSpeedHistVar(),means.getSpeedHistVar(), sdevs.getSpeedHistVar()),
		scale(epf.getAccelHistVar(),means.getAccelHistVar(), sdevs.getAccelHistVar()),
		epf.getPeriodMS(), epf.getTime());
	
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
    
}
