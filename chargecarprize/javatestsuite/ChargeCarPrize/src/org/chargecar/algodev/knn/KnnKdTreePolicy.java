package org.chargecar.algodev.knn;

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
    
    //private KdTree gpsKdTree;
    private KdTree featKdTree;
    private PointFeatures means;
    private PointFeatures sdevs;    
    private final int lookahead = 90; 
    
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
	
	if(currentDriver == null || driver.compareTo(currentDriver) != 0){
	    try {
		File currentKnnTableFile = new File("C:/Users/astyler/Desktop/My Dropbox/school/ACRL/finalproject/work/knn2/"+driver+".knn");
		System.out.println("New driver: "+driver);
		currentDriver = driver;
		FileInputStream fis = new FileInputStream(currentKnnTableFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		KnnTable knnTable = (KnnTable)ois.readObject();
		System.out.println("Table loaded.  Building trees... ");
		means = knnTable.getKnnPoints().get(0).getFeatures();
		sdevs = knnTable.getKnnPoints().get(1).getFeatures();
		knnTable.getKnnPoints().remove(1);
		knnTable.getKnnPoints().remove(0);
		featKdTree = new KdTree(knnTable.getKnnPoints(), knnTable.getPowers(), new FullFeatureSet());
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
	double predictedFlow = getFlow(pf);
	
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
    
    @SuppressWarnings("unused")
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
    
    public double getFlow(PointFeatures pf){
	List<Double> powers = featKdTree.getBestEstimate(scaleFeatures(pf), 5, 90);

	List<Double> cumulativeSum = new ArrayList<Double>(lookahead);
	List<Integer> timeStamps = new ArrayList<Integer>(lookahead);
	List<Double> rates = new ArrayList<Double>(lookahead);
	double sum = -modelCap.getMinPower(1000);
	int timesum = 0;

	for(int i=0;i<lookahead;i++){	    
	    sum += powers.get(i);
	    timesum += 1000;
	    cumulativeSum.add(sum);
	    timeStamps.add(timesum);
	    rates.add(1000*sum/timesum);
	}
	
	double maxRate = Double.POSITIVE_INFINITY;
	for(int i = 0;i<rates.size();i++){
	    if(rates.get(i) < maxRate){
		maxRate = rates.get(i);
	    }
	}
	
	return maxRate;
    }
    
    private PointFeatures scaleFeatures(PointFeatures pf){
	return new PointFeatures(
		scale(pf.getLatitude(),means.getLatitude(),sdevs.getLatitude()),
		scale(pf.getLongitude(),means.getLongitude(),sdevs.getLongitude()),
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
    
}
