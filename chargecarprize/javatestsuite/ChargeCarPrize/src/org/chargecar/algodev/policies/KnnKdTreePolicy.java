package org.chargecar.algodev.policies;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.chargecar.algodev.predictors.FullFeatureSet;
import org.chargecar.algodev.predictors.knn.KdTree;
import org.chargecar.algodev.predictors.knn.KnnTable;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;

import org.chargecar.prize.util.TripFeatures;

public class KnnKdTreePolicy implements Policy {
    
    //private KdTree gpsKdTree;
    private KdTree featKdTree;
    private PointFeatures means;
    private PointFeatures sdevs;    
    private final int lookahead; 
    private final int neighbors;
    private int pointsTested = 0;
    
    private String currentDriver;
    private BatteryModel modelCap;
    private BatteryModel modelBatt;
    private final String name = "KNN Average Prediction Policy";
    private final String shortName = "knnmean";
    private final File knnFileFolderPath;
    
    public KnnKdTreePolicy(String knnFileFolderPath, int neighbors, int lookahead){
	this.knnFileFolderPath = new File(knnFileFolderPath);
	this.neighbors = neighbors;
	this.lookahead = lookahead;
    }
    
    @Override
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
	    BatteryModel capacitorClone) {
	String driver = tripFeatures.getDriver();
	modelCap = capacitorClone;
	modelBatt = batteryClone;
	
	if(currentDriver == null || driver.compareTo(currentDriver) != 0){
	    try {
		featKdTree = null;
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
		featKdTree = new KdTree(knnTable.getKnnPoints(), knnTable.getPowers(), new FullFeatureSet());
		System.out.println("Trees built.. "+featKdTree.countNodes()+" nodes.");
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
	PointFeatures spf = scaleFeatures(pf);
	
	List<Double> powers = featKdTree.getAverageEstimate(spf, neighbors, lookahead);
	List<Double> cumulativeSum = new ArrayList<Double>(lookahead);
	List<Integer> timeStamps = new ArrayList<Integer>(lookahead);
	List<Double> rates = new ArrayList<Double>(lookahead);
	
	//double sum = -modelCap.getMinPowerDrawable(pf.getPeriodMS());
	double sum = -modelCap.getMaxPowerDrawable(spf.getPeriodMS());
	int timesum = 0;

	for(int i=0;i<lookahead;i++){	    
	    sum += powers.get(i);
	    timesum += 1000;
	    cumulativeSum.add(sum);
	    timeStamps.add(timesum);
	    rates.add(1000*sum/timesum);
	}
	
	double maxRate = Double.NEGATIVE_INFINITY;
	for(int i = 0;i<rates.size();i++){
	    if(rates.get(i) > maxRate){
		maxRate = rates.get(i);
	    }
	}
	
	return maxRate;
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
