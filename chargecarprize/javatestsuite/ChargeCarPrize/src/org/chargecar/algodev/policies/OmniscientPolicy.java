package org.chargecar.algodev.policies;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chargecar.algodev.controllers.ApproximateAnalytic;
import org.chargecar.algodev.controllers.Controller;
import org.chargecar.algodev.controllers.ReceedingConstant;
import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripFeatures;

public class OmniscientPolicy implements Policy {
    List<Double> optimalBatteryDraw;
    
    public OmniscientPolicy(int lookAheadSeconds) {
	super();
	this.lookAheadSeconds = lookAheadSeconds;
    }
    int currentIndex;
    final int lookAheadSeconds;
    private List<Double> powers;
    private List<Integer> periods;
    private Map<Calendar,Integer> map;
    private Controller appController = new ApproximateAnalytic();//new ReceedingConstant();
    
    public void parseTrip(Trip t){
	List<PointFeatures> points = t.getPoints();
	powers = new ArrayList<Double>(points.size());
	periods = new ArrayList<Integer>(points.size());
	map = new HashMap<Calendar,Integer>(points.size());
	int index = 0;
	for(PointFeatures pf : points){
	    powers.add(pf.getPowerDemand());
	    periods.add(pf.getPeriodMS());
	    map.put(pf.getTime(), index);
	    index++;
	} 
    }
    
    public double getFlow(PointFeatures pf){
	
	int index = map.get(pf.getTime());
	Prediction p = new Prediction(1.0,powers.subList(index, Math.min(powers.size(),index+lookAheadSeconds)));
	List<Prediction> ps = new ArrayList<Prediction>();
	ps.add(p);
	return appController.getControl(ps, modelBatt, modelCap, pf.getPeriodMS());
//	List<Double> joulesCumuSum = new ArrayList<Double>();
//	List<Double> rates = new ArrayList<Double>();
//	
//	double sum = -modelCap.getMaxPowerDrawable(pf.getPeriodMS());
//	//double sum=0;
//	int timesum = 0;
//
//	for(int i=index;timesum<lookAheadSeconds*1000 && i<powers.size();i++){	    
//	    sum += powers.get(i)*(periods.get(i)/1000.0);
//	    timesum += periods.get(i);
//	    joulesCumuSum.add(sum);
//	    rates.add(1000*sum/timesum);
//	}
//	
//	double maxRate = 0;//Double.NEGATIVE_INFINITY;
//	for(int i = 0;i<rates.size();i++){
//	    if(rates.get(i) > maxRate){
//		maxRate = rates.get(i);
//	    }
//	}
//	
//	return maxRate;
    }
    
  /*  public void parseTrip(Trip t){
	List<PointFeatures> points = t.getPoints();
	optimalBatteryDraw = new ArrayList<Double>(points.size());
	List<Double> cumulativeSumJoules = new ArrayList<Double>(points.size());
	List<Integer> timeStamps = new ArrayList<Integer>(points.size());
	List<Double> rates = new ArrayList<Double>(points.size());
	double sumJoules = 0;
	int timesum = 0;
	
	for(PointFeatures pf : points){
	    sumJoules += (pf.getPowerDemand()*pf.getPeriodMS())/1000.0;
	    timesum += pf.getPeriodMS();
	    cumulativeSumJoules.add(sumJoules);
	    timeStamps.add(timesum);
	    rates.add(1000*sumJoules/timesum);
	}
	
	
	for(int startInd = 0; startInd < rates.size(); startInd++){
	    double maxRate = Double.NEGATIVE_INFINITY;	    
	    for(int i = startInd;(i < startInd + lookAheadSeconds && i<rates.size());i++){
		if(rates.get(i) >= maxRate){
		    maxRate = rates.get(i);
		}
	    }
	 
	    rates.set(startInd, maxRate);

	    int timesub = timeStamps.get(startInd);
	    for(int i = startInd+1;i<rates.size();i++){
		cumulativeSumJoules.set(i, cumulativeSumJoules.get(i)-(maxRate*timesub)/1000.0);
		timeStamps.set(i, timeStamps.get(i)-timesub);
		rates.set(i,1000.0*cumulativeSumJoules.get(i)/timeStamps.get(i));
	    }
	}
	optimalBatteryDraw = rates;
    }
    */
    
    private BatteryModel modelCap;
    private BatteryModel modelBatt;
    private String name = "Omniscient Policy";
    private String shortName = "omni";
    
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
	    BatteryModel capacitorClone) {
	modelCap = capacitorClone;
	modelBatt = batteryClone;
	currentIndex = 0;
    }
    @Override
    public String getShortName() {
	return this.shortName;
    }
    
   /* @Override
    public PowerFlows calculatePowerFlows(PointFeatures pf) {
	double wattsDemanded = pf.getPowerDemand();
	int periodMS = pf.getPeriodMS();
	double minCapPower = modelCap.getMinPowerDrawable(periodMS);
	double maxCapPower = modelCap.getMaxPowerDrawable(periodMS);
	
	double capToMotorWatts = wattsDemanded > maxCapPower ? maxCapPower : wattsDemanded;
	capToMotorWatts = capToMotorWatts < minCapPower ? minCapPower : capToMotorWatts;
	double batteryToMotorWatts = wattsDemanded - capToMotorWatts;
	double batteryToCapWatts = optimalBatteryDraw.get(currentIndex) - batteryToMotorWatts;
	
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
	currentIndex ++;
	return new PowerFlows(batteryToMotorWatts, capToMotorWatts,
		batteryToCapWatts);
    }*/
    
    @Override
    public PowerFlows calculatePowerFlows(PointFeatures pf) {
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
	    modelCap.drawPower(capToMotorWatts - batteryToCapWatts, periodMS);
	    modelBatt.drawPower(batteryToMotorWatts + batteryToCapWatts, periodMS);
	} catch (PowerFlowException e) {
	}
	
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
