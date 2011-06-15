package org.chargecar.algodev;

import java.util.ArrayList;
import java.util.List;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripFeatures;

public class OmniscientPolicy implements Policy {
    List<Double> optimalBCFlow;
    public OmniscientPolicy(int lookAheadSeconds) {
	super();
	this.lookAheadSeconds = lookAheadSeconds;
    }
    int currentIndex;
    final int lookAheadSeconds;
    
    public void parseTrip(Trip t){
	List<PointFeatures> points = t.getPoints();
	optimalBCFlow = new ArrayList<Double>(points.size());
	List<Double> cumulativeSum = new ArrayList<Double>(points.size());
	List<Integer> timeStamps = new ArrayList<Integer>(points.size());
	List<Double> rates = new ArrayList<Double>(points.size());
	double sum = 0;
	int timesum = 0;
	
	for(PointFeatures pf : points){
	    sum += pf.getPowerDemand();
	    timesum += pf.getPeriodMS();
	    cumulativeSum.add(sum);
	    timeStamps.add(timesum);
	    rates.add(1000*sum/timesum);
	}
	
	int startInd = 0;
	while(startInd < rates.size()){
	    double maxRate = Double.POSITIVE_INFINITY;
	    int endInd = startInd;
	    for(int i = startInd;(i < startInd + lookAheadSeconds && i<rates.size());i++){
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
		rates.set(i,1000*cumulativeSum.get(i)/timeStamps.get(i));
	    }
	    startInd = endInd +1;
	}
	optimalBCFlow = rates;

    }
    private BatteryModel modelCap;
    private BatteryModel modelBatt;
    private String name = "Omniscient Policy";
    
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
	    BatteryModel capacitorClone) {
	modelCap = capacitorClone;
	modelBatt = batteryClone;
	currentIndex = 0;
    }
    
    @Override
    public PowerFlows calculatePowerFlows(PointFeatures pf) {
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
	    batteryToCapWatts = optimalBCFlow.get(currentIndex) - batteryToMotorWatts;
	} else if (wattsDemanded > maxCapPower) {
	    // overflowing cap with regen power
	    // cap is full, no need to trickle.
	    capToMotorWatts = maxCapPower;
	    batteryToMotorWatts = wattsDemanded - capToMotorWatts;
	    batteryToCapWatts = optimalBCFlow.get(currentIndex) - batteryToMotorWatts;
	} else {
	    // capacitor can handle the demand
	    capToMotorWatts = wattsDemanded;
	    batteryToMotorWatts = 0;
	    batteryToCapWatts = optimalBCFlow.get(currentIndex);
	   
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
	currentIndex ++;
	return new PowerFlows(batteryToMotorWatts, capToMotorWatts,
		batteryToCapWatts);
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
