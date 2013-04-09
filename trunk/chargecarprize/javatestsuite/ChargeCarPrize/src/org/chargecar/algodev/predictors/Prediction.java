package org.chargecar.algodev.predictors;

import java.util.ArrayList;
import java.util.List;

public class Prediction {
    private final double weight;
    private final List<Double> powers = new ArrayList<Double>();
 //   private final String driver;
    private final int tripID;
    private final int time;
    
   
//    public String getDriver() {
//        return driver;
//    }

    public int getTripID() {
        return tripID;
    }

    public int getTime() {
        return time;
    }

    public double getWeight(){
	return weight;
    }
    
    public List<Double> getPowers(){
	return this.powers;
    }
    
    public void setPowers(List<Double> powers){
	/*boolean over = false;
	for(Double d : powers){
	    if(d == null) over = true;
	    if(over) this.powers.add(0.0);
	    else this.powers.add(d);
	    }*/
	this.powers.addAll(powers);
    }

    public Prediction(double weight, int tripID, int time) {
	super();
	this.weight = weight;
//	this.driver = driver;
	this.tripID = tripID;
	this.time = time;
    }
    
    
}
