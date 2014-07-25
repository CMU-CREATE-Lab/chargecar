package org.chargecar.experiments.hybridBMW;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.util.Trip;

public class MDPTrainerHybrid {
    private String currentDriver;
    private final String optFileFolderPath;
    private final double discountFactor = 1;
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
    
    public void parseTrip(Trip t, boolean debug_writeVGFile) {
	updateTripMap(t, debug_writeVGFile);
	System.out.println(tripMap.size());
    }
    
    private void updateTripMap(Trip trip, boolean debug_writeVGFile) {
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
	
	if (debug_writeVGFile)
	    writeValueGraph(trip, valueGraph, new File(optFileFolderPath
		    + trip.getFeatures().getFileName() + ".csv"));
	
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
    
    public void writeValueGraph(Trip t, double[][] vg, File location) {
	System.out.println("DEBUG: Printing ValueGraph file: "
		+ location.getAbsolutePath());
	final DecimalFormat vgd = new DecimalFormat("0.000");
	FileWriter fstream;
	try {
	    location.getParentFile().mkdirs();
	    // vgFile.createNewFile();
	    fstream = new FileWriter(location);
	    BufferedWriter out = new BufferedWriter(fstream);
	    for (int x = 0; x < vg.length; x++) {
		out.write(vgd.format(vg[x][0]));
		for (int y = 1; y < vg[x].length; y++) {
		    out.write("," + vgd.format(vg[x][y]));
		}
		out.write("\n");
	    }
	    out.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    public void finishTraining() {
	writeTable();
    }
    
}
