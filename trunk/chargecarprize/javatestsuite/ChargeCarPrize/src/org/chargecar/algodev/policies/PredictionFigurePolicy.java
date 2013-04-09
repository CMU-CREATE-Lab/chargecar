package org.chargecar.algodev.policies;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.Trip;

public class PredictionFigurePolicy extends KnnDistributionPolicy {
    private List<Double> powers;
    private int index;
    
    public PredictionFigurePolicy(String knnFileFolderPath, int neighbors,
	    int lookahead) {
	super(knnFileFolderPath, neighbors, lookahead);
    }
    
    public void parseTrip(Trip t){
	index = 0;
	List<PointFeatures> points = t.getPoints();
	powers = new ArrayList<Double>(points.size());
	for(PointFeatures pf : points){
	    powers.add(pf.getPowerDemand());	  
	} 
    }
    
    public double getFlow(PointFeatures pf){
	PointFeatures spf = scaleFeatures(pf);
	List<Prediction> predictedDuty = knnPredictor.predictDuty(spf);
	index++;
	if(index == 100){
	    writePredictions(predictedDuty);
	}
	return controller.getControl(predictedDuty, modelBatt,modelCap,spf.getPeriodMS());	
    }
    
    @Override
    public void clearState() {
	
    }
    
    public void writePredictions(List<Prediction> preds){
	FileWriter fstream;
	writeActual();
	try {
	    fstream = new FileWriter("/home/astyler/predictions.csv",false);
	    BufferedWriter out = new BufferedWriter(fstream);
	    for(Prediction p : preds){
		System.out.println(p.getWeight());
		boolean end = false;
		for(Double d : p.getPowers()){
		    if(d==null) end=true;
		    if(end)
			out.write("0.0,");
		    else
			out.write(d+",");
		}
		out.write("0.0\n");
	    }
	    out.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    public void writeActual(){
	FileWriter fstream;
	try {
	    
	    fstream = new FileWriter("/home/astyler/actual.csv",false);
	    BufferedWriter out = new BufferedWriter(fstream);
	    boolean end = false;
	    for(Double d : powers){
		if(d==null) end=true;
		if(end)
		    out.write("0.0,");
		else
		    out.write(d+",");
	    }
	    
	    out.write("0.0\n");	   
	    out.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
}

