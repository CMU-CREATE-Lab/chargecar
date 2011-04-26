package org.chargecar.algodev.knn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.TripBuilder;
import org.chargecar.prize.util.TripFeatures;

public class KnnPolicy implements Policy {
    KnnTable knnTable;
    String currentDriver;
    private BatteryModel modelCap;
    private BatteryModel modelBatt;
    private String name = "Nearest Neighbor Policy";
    private int lookahead = 90;
    
    @Override
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
	    BatteryModel capacitorClone) {
	String driver = tripFeatures.getDriver();
	modelCap = capacitorClone;
	modelBatt = batteryClone;
	
	if(currentDriver == null || driver.compareTo(currentDriver) != 0){
	    try {
		File currentKnnTableFile = new File("C:/Users/astyler/Desktop/My Dropbox/school/ACRL/finalproject/work/knn/"+driver+".knn");
		System.out.println("New driver: "+driver);
		currentDriver = driver;
		FileInputStream fis = new FileInputStream(currentKnnTableFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		knnTable = (KnnTable)ois.readObject();
		System.out.println("Table loaded.");
	    } catch (Exception e) {
		knnTable = new KnnTable();
		e.printStackTrace();
	    }
	}
	 	
    }
    
    @Override
    public PowerFlows calculatePowerFlows(PointFeatures pf) {
	double predictedFlow = getFlow(pf);
	double wattsDemanded = pf.getPowerDemand();
	int periodMS = pf.getPeriodMS();
	double minCapPower = modelCap.getMinPower(periodMS);
	double maxCapPower = modelCap.getMaxPower(periodMS);
	double capToMotorWatts = 0.0;
	double batteryToCapWatts = 0.0;
	double batteryToMotorWatts = 0.0;
	if (wattsDemanded < minCapPower) {
	    // drawing more than the cap has
	    // battery is already getting drawn, don't trickle cap
	    capToMotorWatts = minCapPower;
	    batteryToMotorWatts = wattsDemanded - capToMotorWatts;
	    batteryToCapWatts = predictedFlow - batteryToMotorWatts;
	} else if (wattsDemanded > maxCapPower) {
	    // overflowing cap with regen power
	    // cap is full, no need to trickle.
	    capToMotorWatts = maxCapPower;
	    batteryToMotorWatts = wattsDemanded - capToMotorWatts;
	    batteryToCapWatts = predictedFlow - batteryToMotorWatts;
	} else {
	    // capacitor can handle the demand
	    capToMotorWatts = wattsDemanded;
	    batteryToMotorWatts = 0;
	    batteryToCapWatts = predictedFlow;
	   
	}
	batteryToCapWatts = 0 < batteryToCapWatts ? 0 : batteryToCapWatts;
	
	if (capToMotorWatts - batteryToCapWatts > maxCapPower) {
		batteryToCapWatts = capToMotorWatts - maxCapPower;
	    } else if(capToMotorWatts - batteryToCapWatts < minCapPower){
		batteryToCapWatts = capToMotorWatts - minCapPower;
	    }

	try {
	    modelCap.drawPower(capToMotorWatts - batteryToCapWatts, pf);
	    modelBatt.drawPower(batteryToMotorWatts + batteryToCapWatts, pf);
	} catch (PowerFlowException e) {
	}

	return new PowerFlows(batteryToMotorWatts, capToMotorWatts,
		batteryToCapWatts);
    }
    
    public double getFlow(PointFeatures pf){
	double minDist = Double.POSITIVE_INFINITY;
	int powerIndex = 0;
	for(KnnPoint kp : knnTable.getKnnPoints()){
	    double dist = computeDistance(pf,kp);
	    if(dist < minDist){
		minDist = dist;
		powerIndex = kp.getGroundTruthIndex();
	    }
	}
	
	List<Double> cumulativeSum = new ArrayList<Double>(lookahead);
	List<Integer> timeStamps = new ArrayList<Integer>(lookahead);
	List<Double> rates = new ArrayList<Double>(lookahead);
	double sum = -modelCap.getMinPower(1000);
	int timesum = 0;
	int index = 0;
	Double powerD = knnTable.getPowers().get(powerIndex);
	while(index < lookahead && powerD != null){
	    index++;
	    sum += powerD;
	    timesum += 1000;
	    cumulativeSum.add(sum);
	    timeStamps.add(timesum);
	    rates.add(1000*sum/timesum);
	    powerD = knnTable.getPowers().get(powerIndex + index); 
	}
	
	double maxRate = Double.POSITIVE_INFINITY;
	for(int i = 0;i<rates.size();i++){
	    if(rates.get(i) < maxRate){
		maxRate = rates.get(i);
	    }
	}
	
	return maxRate;
	
    }
    
    public static double computeDistance(PointFeatures pf, KnnPoint kp){
	
	double latDiff = pf.getLatitude()-kp.getFeatures().getLatitude();
	double lonDiff = pf.getLongitude()-kp.getFeatures().getLongitude();
	
	return latDiff*latDiff+lonDiff*lonDiff;
	//TripBuilder.Haversine(pf.getLatitude(), pf.getLongitude(), kp.getFeatures().getLatitude(), kp.getFeatures().getLongitude());
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
