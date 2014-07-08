package org.chargecar.experiments.hybridBMW;

    import java.io.BufferedWriter;
    import java.io.File;
    import java.io.FileInputStream;
    import java.io.FileWriter;
    import java.io.IOException;
    import java.io.ObjectInputStream;
    import java.util.HashMap;
    import java.util.List;
    import java.util.ArrayList;
    import java.util.Map;

    import org.chargecar.algodev.controllers.Controller;
    import org.chargecar.algodev.controllers.DPOptController;
    import org.chargecar.algodev.knn.FullFeatureSet;
    import org.chargecar.algodev.knn.KnnPoint;
    import org.chargecar.algodev.predictors.Prediction;
    import org.chargecar.algodev.predictors.Predictor;
    import org.chargecar.algodev.predictors.knn.KnnDistPredictor;
    import org.chargecar.prize.battery.BatteryModel;
    import org.chargecar.prize.policies.Policy;
    import org.chargecar.prize.util.PointFeatures;
    import org.chargecar.prize.util.PowerFlowException;
    import org.chargecar.prize.util.PowerFlows;
    import org.chargecar.prize.util.Trip;
    import org.chargecar.prize.util.TripFeatures;

    public class NaivePolicyHybrid extends OptPolicyHybrid {
        
	private String currentDriver;

        protected BatteryModel modelBatt;
        private final String name = "Naive Policy";
        private final String shortName = "naivepoly";

        private int tripID;
        
        public NaivePolicyHybrid(String folderPath){
            super(folderPath);
    	}

        public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone) {
            modelBatt = batteryClone;
    	}
        
        public PowerControls calculatePowerFlows(PointFeatures pf, int i) {
    	int idealEngineWatts = 0;	
    	int wattsDemanded = (int)pf.getPowerDemand();
    	
    	int periodMS = pf.getPeriodMS();
    	
    	int minBattPower = (int) modelBatt.getMinPowerDrawable(periodMS);
    	int maxBattPower = (int) modelBatt.getMaxPowerDrawable(periodMS);	
    	
    	int motorWatts = wattsDemanded-idealEngineWatts;
    	
    	motorWatts = motorWatts > maxBattPower ? maxBattPower : motorWatts;
    	motorWatts = motorWatts < minBattPower ? minBattPower : motorWatts;
    	
    	int engineWatts = wattsDemanded - motorWatts;
    	
    	try {
    	    modelBatt.drawPower(motorWatts, periodMS);
    	} catch (PowerFlowException e) {
    	    System.err.println("Battery Capacity violated in policy model");
    	}
    	

    	return new PowerControls(engineWatts, motorWatts);
        }
        


        public String getName() {
    	return this.name;
        }
       
        
        public String getShortName() {
    	return this.shortName;
        }

        
        public void clearState() {
    	this.currentDriver = null;
        }
        
    }


