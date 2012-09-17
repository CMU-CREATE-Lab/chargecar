package org.chargecar.algodev.policies;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.Trip;

public class AccuracyTesterPolicy extends KnnMeanPolicy {
    private final List<Double> accuracies;
    private List<Double> powers;
    private int index;
    
    public AccuracyTesterPolicy(String knnFileFolderPath, int neighbors,
	    int lookahead) {
	super(knnFileFolderPath, neighbors, lookahead);
	this.accuracies = new ArrayList<Double>();
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
	scoreAccuracy(predictedDuty.get(0));
	index++;
	return controller.getControl(predictedDuty, modelBatt,modelCap,spf.getPeriodMS());	
    }
    
    private void scoreAccuracy(Prediction pred){
	double diffsum = 0;
	double predsum = 0;
	double truthsum = 0;
	if(index+lookahead > this.powers.size()){
	    return;
	}
	    
	for(int i=0;i<lookahead;i++){
	    predsum += pred.getPowers().get(i);
	    truthsum += this.powers.get(index+i);
	    diffsum += Math.abs(predsum-truthsum);
	}
	this.accuracies.add(diffsum);
	//todo Write to CSV on output/
	
    }

    @Override
    public void clearState() {
	
    }
    
    public void writeAccuracy(){
	FileWriter fstream;
	try {
	    fstream = new FileWriter("C:/accuracies.csv",false);
	    BufferedWriter out = new BufferedWriter(fstream);
	    for(Double p : accuracies){
		out.write(p+",");
	    }
	    out.write("0.0\n");		
	    out.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }
    }
    
}
