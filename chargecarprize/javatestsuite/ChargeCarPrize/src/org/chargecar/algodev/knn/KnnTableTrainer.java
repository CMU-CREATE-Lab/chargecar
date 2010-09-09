package org.chargecar.algodev.knn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.chargecar.algodev.ExtendedPointFeatures;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripFeatures;

public class KnnTableTrainer implements Policy {
    ArrayList<KnnPoint> table;
    String currentDriver;
    File currentKnnTableFile;
    final int histLength = 20;
    
    public void parseTrip(Trip t){
	List<PointFeatures> points = t.getPoints();
	List<Double> optimalBCFlow = new ArrayList<Double>(points.size());
	List<Double> cumulativeSum = new ArrayList<Double>(points.size());
	List<Integer> timeStamps = new ArrayList<Integer>(points.size());
	List<Double> rates = new ArrayList<Double>(points.size());
	double sum = 0;
	int timesum = 0;
	for(PointFeatures pf : points){
	    sum += pf.getPowerDemand();
	    timesum += pf.getPeriodMS()/1000;
	    cumulativeSum.add(sum);
	    timeStamps.add(timesum);
	    rates.add(sum/timesum);
	}
	int startInd = 0;
	while(startInd < rates.size()){
	    double maxRate = Double.POSITIVE_INFINITY;
	    int endInd = startInd;
	    for(int i = startInd;i<rates.size();i++){
		if(rates.get(i) < maxRate){
		    endInd = i;
		    maxRate = rates.get(i);
		}
	    }
	    for(int i = startInd;i<=endInd;i++){
		rates.set(i, maxRate);
	    }
	    double sumsub = cumulativeSum.get(endInd);
	    int timesub = timeStamps.get(endInd);
	    for(int i = endInd+1;i<rates.size();i++){
		cumulativeSum.set(i, cumulativeSum.get(i)-sumsub);
		timeStamps.set(i, timeStamps.get(i)-timesub);
		rates.set(i,cumulativeSum.get(i)/timeStamps.get(i));
	    }
	    startInd = endInd +1;
	}
	optimalBCFlow = rates;
	updateDriverTable(optimalBCFlow, t);
    }
    
    private void updateDriverTable(List<Double> bcFlows, Trip trip){
	String driver = trip.getFeatures().getDriver();
	
	if(currentDriver == null || driver.compareTo(currentDriver) != 0){
	    try {
		if(currentDriver != null) writeTable();
		System.out.println("New driver: "+driver);
		currentKnnTableFile = new File("C:/Users/astyler/Desktop/My Dropbox/work/ccpbak/knn/"+driver+".knn");
		currentDriver = driver;
		FileInputStream fis = new FileInputStream(currentKnnTableFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		table = (ArrayList<KnnPoint>)ois.readObject();
	    } catch (Exception e) {
		table = new ArrayList<KnnPoint>();
	    }
	}
	List<Double> speedHist = new ArrayList<Double>();
	List<Double> accelHist = new ArrayList<Double>();
	double speedVar;
	double accelVar;
	int histIndex = 0;
	histIndex = 0;
	int i = 0;
	for(PointFeatures pf : trip.getPoints()){	
	    histIndex = i % histLength;
	    if(speedHist.size() > histIndex){
		speedHist.set(histIndex, pf.getSpeed());
		accelHist.set(histIndex, pf.getAcceleration());
	    }
	    else
	    {
		speedHist.add(pf.getSpeed());
		accelHist.add(pf.getAcceleration());
	    }
	    speedVar = calculateVariance(speedHist);
	    accelVar = calculateVariance(accelHist);
	    histIndex = (histIndex +1)%histLength;
	    table.add(new KnnPoint(new ExtendedPointFeatures(pf, speedVar, accelVar), bcFlows.get(i)));
	    i++;
	}
    }
    
    public void writeTable(){
	System.out.println("Writing table for "+currentDriver);
	scaleFeatures();
	
	FileOutputStream fos;
	try {
	    fos = new FileOutputStream(currentKnnTableFile);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(table);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    private void scaleFeatures(){
	double accMean = 0;
	double speedMean = 0;
	double powerMean = 0;
	double speedVarMean = 0;
	double accVarMean = 0;
	double accSD = 0;
	double speedSD = 0;
	double powerSD = 0;
	double speedVarSD = 0;
	double accVarSD = 0;
	
	for(KnnPoint kp : table){
	    accMean += kp.getFeatures().getAcceleration();
	    speedMean += kp.getFeatures().getSpeed();
	    powerMean += kp.getFeatures().getPowerDemand();
	    speedVarMean += kp.getFeatures().getSpeedHistVar();
	    accVarMean += kp.getFeatures().getAccelHistVar();
	}
	accMean /= table.size();
	speedMean /= table.size();
	powerMean /= table.size();
	speedVarMean /= table.size();
	accVarMean /= table.size();
	
	for(KnnPoint kp : table){
	    accSD += Math.pow(kp.getFeatures().getAcceleration() - accMean, 2.0);
	    speedSD += Math.pow(kp.getFeatures().getSpeed() - speedMean, 2.0);
	    powerSD += Math.pow(kp.getFeatures().getPowerDemand(), 2.0);
	    speedVarSD += Math.pow(kp.getFeatures().getSpeedHistVar(), 2.0);
	    accVarSD += Math.pow(kp.getFeatures().getAccelHistVar(), 2.0);
	}
	
	accSD /= (table.size() - 1);
	speedSD /= (table.size() - 1);
	powerSD /= (table.size() - 1);
	speedVarSD /= (table.size() - 1);
	accVarSD /= (table.size() - 1);
	ExtendedPointFeatures means = new ExtendedPointFeatures(0,0,0,0,accMean,speedMean,powerMean,speedVarMean, accVarMean, 0, null);
	ExtendedPointFeatures sdevs = new ExtendedPointFeatures(0,0,0,0,accSD,speedSD,powerSD,speedVarSD,accVarSD, 0, null);
	
	for(int i = 0;i<table.size();i++){
	    KnnPoint rawPoint = table.get(i);
	    ExtendedPointFeatures rawFeatures = rawPoint.getFeatures();
	    ExtendedPointFeatures scaledFeatures = new ExtendedPointFeatures(
		    rawFeatures.getLatitude(),rawFeatures.getLongitude(),
		    rawFeatures.getElevation(),rawFeatures.getPlanarDist(),
		    scale(rawFeatures.getAcceleration(),accMean,accSD),
		    scale(rawFeatures.getSpeed(),speedMean, speedSD),
		    scale(rawFeatures.getPowerDemand(),powerMean,powerSD),
		    scale(rawFeatures.getSpeedHistVar(),speedVarMean, speedVarSD),
		    scale(rawFeatures.getAccelHistVar(),accVarMean, accVarSD),
		    rawFeatures.getPeriodMS(),	rawFeatures.getTime());
	    table.set(i, new KnnPoint(scaledFeatures, rawPoint.getGroundTruth()));
	}
	table.add(0, new KnnPoint(means, 0));
	table.add(1, new KnnPoint(sdevs, 0));
    }
    
    private double scale(double feature, double mean, double sdev){
	return (feature-mean)/sdev;
    }
    
    public void finishTraining()
    {
	writeTable();
    }
    
    private String name = "Table Trainer";
    
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
	 
	 return sum/(list.size()-1);	
   }
    
}
