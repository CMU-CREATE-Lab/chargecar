package org.chargecar.algodev.policies;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.util.PointFeatures;

public class PowerCountPolicy extends KnnDistributionPolicy {
    private final List<Double> flows;
    
    public PowerCountPolicy(String knnFileFolderPath, int neighbors,
	    int lookahead) {
	super(knnFileFolderPath, neighbors, lookahead);
	this.flows = new ArrayList<Double>();
    }
    
   
    public double getFlow(PointFeatures pf){
	PointFeatures spf = scaleFeatures(pf);
	List<Prediction> predictedDuty = knnPredictor.predictDuty(spf);	
	double control = rhController.getControl(predictedDuty, modelBatt,modelCap,spf.getPeriodMS());
	flows.add(control);
	return control;
    }

    @Override
    public void clearState() {
	
    }
    
    public void writeControls(){
	FileWriter fstream;
	try {
	    fstream = new FileWriter("C:/flows.csv",false);
	    BufferedWriter out = new BufferedWriter(fstream);
	    for(Double p : flows){
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
