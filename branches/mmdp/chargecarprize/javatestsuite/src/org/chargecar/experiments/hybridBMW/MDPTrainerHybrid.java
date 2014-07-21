package org.chargecar.experiments.hybridBMW;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.util.Trip;

public class MDPTrainerHybrid {
    private String currentDriver;
    private final String optFileFolderPath;
    private final double discountFactor = 0.99;
    private final int[] controlsSet = new int[] { 0, 5000, 10000, 15000, 20000,
	    25000, 30000, 35000, 40000, 45000, 50000 };
    
    private final MDPValueGraphHybrid mmdpOpt;
    private Map<Integer, double[][]> tripMap;
    
    public MDPTrainerHybrid(String optFileFolderPath, BatteryModel batt,
	    int stateCount) {
	tripMap = new HashMap<Integer, double[][]>();
	this.optFileFolderPath = optFileFolderPath + "/";
	mmdpOpt = new MDPValueGraphHybrid(controlsSet, stateCount,
		discountFactor, batt.createClone());
    }
    
    public void parseTrip(Trip t) {
	updateTripMap(t);
	System.out.println(tripMap.size());
    }
    
    private void updateTripMap(Trip trip) {
	String driver = trip.getFeatures().getDriver();
	if (trip.getPoints().size() > 3600) return;
	if (currentDriver == null || driver.compareTo(currentDriver) != 0) {
	    if (currentDriver != null) finishTraining();
	    System.out.println("New driver: " + driver);
	    currentDriver = driver;
	    tripMap = new HashMap<Integer, double[][]>();
	}
	
	double[][] valueGraph = mmdpOpt.getValues(trip.getPoints());
	tripMap.put(trip.hashCode(), valueGraph);
    }
    
    public void writeTable() {
	System.out.println("Writing table for " + currentDriver);
	
	FileOutputStream fos;
	try {
	    File knnTableFile = new File(this.optFileFolderPath + currentDriver
		    + ".opt");
	    knnTableFile.getParentFile().mkdirs();
	    knnTableFile.createNewFile();
	    fos = new FileOutputStream(knnTableFile);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(tripMap);
	    oos.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    public void finishTraining() {
	writeTable();
    }
    
}
