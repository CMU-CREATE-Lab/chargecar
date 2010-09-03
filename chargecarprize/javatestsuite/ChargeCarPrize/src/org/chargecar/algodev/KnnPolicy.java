package org.chargecar.algodev;

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
import org.chargecar.prize.util.TripFeatures;

public class KnnPolicy implements Policy {
    List<KnnPoint> knnTable;
    String currentDriver;
    private BatteryModel modelCap;
    private BatteryModel modelBatt;
    private String name = "Nearest Neighbor Policy";
    
    @Override
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
	    BatteryModel capacitorClone) {
	String driver = tripFeatures.getDriver();
	modelCap = capacitorClone;
	modelBatt = batteryClone;
	
	if(currentDriver == null || driver.compareTo(currentDriver) != 0){
	    try {
		File currentKnnTableFile = new File("C:/Users/astyler/Desktop/My Dropbox/work/ccpbak/knn/"+driver+".knn");
		System.out.println("New driver: "+driver);
		currentDriver = driver;
		FileInputStream fis = new FileInputStream(currentKnnTableFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		knnTable = (ArrayList<KnnPoint>)ois.readObject();
	    } catch (Exception e) {
		knnTable = new ArrayList<KnnPoint>();
		e.printStackTrace();
	    }
	}
	 	
    }
    
    @Override
    public PowerFlows calculatePowerFlows(PointFeatures pf) {
	double oredictedFlow = getFlow(pf);
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
	    batteryToCapWatts = oredictedFlow - batteryToMotorWatts;
	} else if (wattsDemanded > maxCapCurrent) {
	    // overflowing cap with regen power
	    // cap is full, no need to trickle.
	    capToMotorWatts = maxCapCurrent;
	    batteryToMotorWatts = wattsDemanded - capToMotorWatts;
	    batteryToCapWatts = oredictedFlow - batteryToMotorWatts;
	} else {
	    // capacitor can handle the demand
	    capToMotorWatts = wattsDemanded;
	    batteryToMotorWatts = 0;
	    batteryToCapWatts = oredictedFlow;
	   
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
    
    public double getFlow(PointFeatures pfs){
	return 0.0;
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
