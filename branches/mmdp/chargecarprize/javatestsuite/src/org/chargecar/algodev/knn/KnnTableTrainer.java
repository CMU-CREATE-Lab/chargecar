package org.chargecar.algodev.knn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripFeatures;

public class KnnTableTrainer implements Policy {
    List<KnnPoint> table;
    String currentDriver;
    final String knnFileFolderPath;
    
    private final String shortName = "knntt";
    
    public KnnTableTrainer(String knnFileFolderPath){
	this.knnFileFolderPath = knnFileFolderPath+"/";
    }    

    @Override
    public String getShortName() {
	return this.shortName;
    }
    
    public void parseTrip(Trip t){
	updateDriverTable(t);
	System.out.println(table.size());
    }
    
    private void updateDriverTable(Trip trip){
	String driver = trip.getFeatures().getDriver();
	if(trip.getPoints().size() > 3600) return;
	if(currentDriver == null || driver.compareTo(currentDriver) != 0){
	    if(currentDriver != null) finishTraining();
	    System.out.println("New driver: "+driver);	    
	    currentDriver = driver;
	    table = new ArrayList<KnnPoint>();
	}

	
	if(table.size() < 400000){	
	    for(int i = 0; i<trip.getPoints().size();i++){
		PointFeatures pf = trip.getPoints().get(i);
		KnnPoint p = new KnnPoint(pf,i, trip.hashCode());
		table.add(p);
	    }
	}
    }
    
    public void writeTable(){
	System.out.println("Writing table for "+currentDriver);
	
	FileOutputStream fos;
	try {
	    
	    File knnTableFile = new File(this.knnFileFolderPath+currentDriver+".knn");
	    knnTableFile.createNewFile();
	    fos = new FileOutputStream(knnTableFile);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(table);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    private void scaleFeatures(){
	double latMean = 0;
	double lonMean = 0;
	double eleMean = 0;
	double bearingMean = 0;
	double accMean = 0;
	double speedMean = 0;
	double powerMean = 0;
	double powerSumMean = 0;
	double latSD = 0;
	double lonSD = 0; 
	double eleSD = 0;
	double bearingSD = 0;
	double accSD = 0;
	double speedSD = 0;
	double powerSD = 0;
	double powerSumSD = 0;
	
	int size = table.size();
	for(KnnPoint kp : table){
	    accMean += kp.getFeatures().getAcceleration();
	    speedMean += kp.getFeatures().getSpeed();
	    powerMean += kp.getFeatures().getPowerDemand();
	    latMean += kp.getFeatures().getLatitude();
	    lonMean += kp.getFeatures().getLongitude();
	    eleMean += kp.getFeatures().getElevation();
	    bearingMean += kp.getFeatures().getBearing();    
	    powerSumMean += kp.getFeatures().getTotalPowerUsed();
	}
	accMean /= size;
	speedMean /= size;
	powerMean /= size;
	latMean /= size;
	lonMean /= size;
	eleMean /= size;
	bearingMean /= size;
	powerSumMean /= size;
	
	for(KnnPoint kp : table){
	    accSD += Math.pow(kp.getFeatures().getAcceleration() - accMean, 2.0);
	    speedSD += Math.pow(kp.getFeatures().getSpeed() - speedMean, 2.0);
	    powerSD += Math.pow(kp.getFeatures().getPowerDemand() - powerMean, 2.0);
	    latSD += Math.pow(kp.getFeatures().getLatitude() - latMean, 2.0);
	    lonSD += Math.pow(kp.getFeatures().getLongitude() - lonMean, 2.0);
	    eleSD +=Math.pow(kp.getFeatures().getElevation() - eleMean, 2.0);
	    bearingSD += Math.pow(kp.getFeatures().getBearing() - bearingMean, 2.0);
	    powerSumSD += Math.pow(kp.getFeatures().getTotalPowerUsed() - powerSumMean, 2.0);
	}
	
	powerSumSD /=size;
	accSD /= size;
	speedSD /= size;
	powerSD /= size;
	latSD /= size;
	lonSD /= size;
	eleSD /= size;
	bearingSD /= size;

	PointFeatures means = new PointFeatures(latMean,lonMean,eleMean,bearingMean,0,accMean,speedMean,powerMean, powerSumMean,0, null);
	PointFeatures sdevs = new PointFeatures(latSD,lonSD,eleSD,bearingSD,0,accSD,speedSD,powerSD, powerSumSD, 0, null);
	
	table.add(0, new KnnPoint(sdevs,0,0));
	table.add(0, new KnnPoint(means,0,0));

	for(int i = 2;i<table.size();i++){
	    KnnPoint rawPoint = table.get(i);
	    PointFeatures rawFeatures = rawPoint.getFeatures();
	    PointFeatures scaledFeatures = new PointFeatures(
		    //scale(rawFeatures.getLatitude(),latMean,latSD),
		    //scale(rawFeatures.getLongitude(),lonMean,lonSD),
		    rawFeatures.getLatitude(),
		    rawFeatures.getLongitude(),
		    scale(rawFeatures.getElevation(),eleMean, eleSD),
		    scale(rawFeatures.getBearing(),bearingMean,bearingSD),
		    rawFeatures.getPlanarDist(),
		    scale(rawFeatures.getAcceleration(),accMean,accSD),
		    scale(rawFeatures.getSpeed(),speedMean, speedSD),
		    scale(rawFeatures.getPowerDemand(),powerMean,powerSD),
		    scale(rawFeatures.getTotalPowerUsed(), powerSumMean, powerSumSD),
		    rawFeatures.getPeriodMS(),	rawFeatures.getTime());
	    table.get(i).setFeatures(scaledFeatures);
	}
    }
    
    private double scale(double feature, double mean, double sdev){
	return (feature-mean)/sdev;
    }
    
    public void finishTraining()
    {
	scaleFeatures();
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
    public void endTrip(Trip t) {}
    
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
	 
	 return sum/(list.size());	
   }

    @Override
    public void clearState() {
	// TODO Auto-generated method stub
	
    }
    
}
