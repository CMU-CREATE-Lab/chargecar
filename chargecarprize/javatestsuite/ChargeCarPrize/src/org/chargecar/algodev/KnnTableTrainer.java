package org.chargecar.algodev;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
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
    ArrayList<KnnPoint> table;
    String currentDriver = "";
    
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
	File knnTableFile = new File("C:/Users/astyler/Desktop/My Dropbox/work/ccpbak/knn/"+driver+".knn");
	
	if(driver.compareTo(currentDriver) != 0){
	    try {
		FileInputStream fis = new FileInputStream(knnTableFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		table = (ArrayList<KnnPoint>)ois.readObject();
	    } catch (Exception e) {
		table = new ArrayList<KnnPoint>();
		e.printStackTrace();
	    }
	}
	
	int i = 0;
	for(PointFeatures pf : trip.getPoints()){	    
	    table.add(new KnnPoint(pf, bcFlows.get(i)));
	    i++;
	}
	FileOutputStream fos;
	try {
	    fos = new FileOutputStream(knnTableFile);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(table);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    private String name = "Table Trainer";
    
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
	    BatteryModel capacitorClone) {}
    
    @Override
    public PowerFlows calculatePowerFlows(PointFeatures pf) {
	return new PowerFlows(0,0,0);
    }
    
    @Override
    public void endTrip() {
	// TODO Auto-generated method stub
	
    }
    
    @Override
    public String getName() {
	return name;
    }
    
    @Override
    public void loadState() {
	// TODO Auto-generated method stub	
    }
    
}
