package org.chargecar.algodev.predictors;

import java.util.ArrayList;
import java.util.List;

public class Prediction {
    private final double weight;
    private final List<Double> powers = new ArrayList<Double>();
   // private final String driver;
    //private final 
    
    
    public Prediction(double weight, List<Double> powers){
	this.weight = weight;
	this.powers.addAll(powers);
    }
    
    public double getWeight(){
	return weight;
    }
    
    public List<Double> getPowers(){
	return this.powers;
    }
}
